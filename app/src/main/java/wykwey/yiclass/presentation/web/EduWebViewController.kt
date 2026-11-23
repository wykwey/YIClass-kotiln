package wykwey.yiclass.presentation.web

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val DESKTOP_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"
private const val MOBILE_UA =
    "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"

/**
 * 从当前页面提取表格化文本的注入脚本，对齐原版 ai_edu_import_page.dart 的 _extractScript：
 * 优先提取 <table>，没有表格时回退为 body 全文本。
 */
private const val PAGE_TEXT_EXTRACT_SCRIPT = """
(function() {
  var result = [];
  document.querySelectorAll('table').forEach(function(table, idx) {
    var rows = [];
    table.querySelectorAll('tr').forEach(function(row) {
      var cells = [];
      row.querySelectorAll('td, th').forEach(function(cell) {
        cells.push(cell.innerText.trim());
      });
      if (cells.length > 0) rows.push(cells.join('\t'));
    });
    if (rows.length > 0) result.push('=== 表格 ' + (idx + 1) + ' ===\n' + rows.join('\n'));
  });
  return result.length > 0 ? result.join('\n\n') : document.body.innerText;
})();
"""

/**
 * 教务 WebView 控制器，封装 URL 归一化 / 桌面UA切换 / 页面文本提取 / JS 回调通道，
 * 供 [wykwey.yiclass.presentation.ui.ai.AiEduImportScreen]（AI 分析）
 * 和 [wykwey.yiclass.presentation.ui.repository.EduImportScreen]（脚本执行）共用。
 */
class EduWebViewController(val webView: WebView) {

    private val _currentUrl = MutableStateFlow("about:blank")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private var scriptResult: CompletableDeferred<String>? = null

    fun normalizeUrl(url: String): String {
        if (url.isBlank()) return "about:blank"
        val trimmed = url.trim()
        return if (trimmed.startsWith("http")) trimmed else "https://$trimmed"
    }

    fun loadUrl(url: String) {
        val normalized = normalizeUrl(url)
        if (normalized != "about:blank") webView.loadUrl(normalized)
    }

    fun reload() = webView.reload()

    fun setDesktopMode(desktop: Boolean) {
        webView.settings.userAgentString = if (desktop) DESKTOP_UA else MOBILE_UA
        webView.reload()
    }

    suspend fun runScript(script: String): String {
        val result = CompletableDeferred<String>()
        scriptResult = result
        return try {
            webView.evaluateJavascript("""
                try {
                    $script
                } catch (error) {
                    window.YiClassChannel.postMessage(JSON.stringify({ error: String(error) }));
                }
            """.trimIndent(), null)
            withTimeoutOrNull(20_000) { result.await() } ?: error("脚本没有返回课表，请确认已打开课表页面后重试")
        } finally {
            scriptResult = null
        }
    }

    internal fun receiveScriptResult(message: String) {
        webView.post { scriptResult?.complete(message) }
    }

    fun extractPageText(onResult: (String) -> Unit) {
        webView.evaluateJavascript(PAGE_TEXT_EXTRACT_SCRIPT) { raw ->
            onResult(Json.decodeFromString<String?>(raw ?: "null").orEmpty())
        }
    }

    internal fun updateCurrentUrl(url: String) {
        _currentUrl.value = url
    }
}

/**
 * 创建并记住一个配置好的 [EduWebViewController]：开启 JS、注入 YiClassChannel、跟踪当前 URL。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun rememberEduWebView(initialUrl: String = "", scriptBridge: Boolean = false): EduWebViewController {
    val context = LocalContext.current
    val controller = remember(context, initialUrl, scriptBridge) {
        val webView = WebView(context)
        val ctrl = EduWebViewController(webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        if (scriptBridge) {
            webView.addJavascriptInterface(object {
                @JavascriptInterface
                fun postMessage(message: String) = ctrl.receiveScriptResult(message)
            }, "YiClassChannelBridge")
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                if (url != null) ctrl.updateCurrentUrl(url)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url != null) ctrl.updateCurrentUrl(url)
                if (scriptBridge) {
                    view?.evaluateJavascript(
                        "window.YiClassChannel = { postMessage: msg => YiClassChannelBridge.postMessage(typeof msg === 'string' ? msg : JSON.stringify(msg)) };",
                        null
                    )
                }
            }
        }
        val start = if (initialUrl.isNotBlank()) ctrl.normalizeUrl(initialUrl) else "about:blank"
        ctrl.updateCurrentUrl(start)
        if (start != "about:blank") webView.loadUrl(start)
        ctrl
    }
    return controller
}
