package wykwey.yiclass.data.ai

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import wykwey.yiclass.domain.model.AiConfig

object AiService {
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private class AiServiceException(message: String) : Exception(message)

    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<UserMessage>,
        @SerialName("max_tokens") val maxTokens: Int
    )

    @Serializable
    private data class UserMessage(val role: String, val content: JsonElement)

    @Serializable
    private data class ChatResponse(val choices: List<Choice>)

    @Serializable
    private data class Choice(val message: Reply)

    @Serializable
    private data class Reply(val content: String?)

    suspend fun analyzeImage(config: AiConfig, imageUrl: String): String {
        val content = buildJsonArray {
            add(buildJsonObject {
                put("type", "text")
                put("text", imagePrompt())
            })
            add(buildJsonObject {
                put("type", "image_url")
                putJsonObject("image_url") { put("url", imageUrl) }
            })
        }
        return extractJson(call(config, config.visionModel, content))
    }

    suspend fun analyzeTable(config: AiConfig, table: String): String =
        extractJson(call(config, config.textModel, JsonPrimitive(tablePrompt(table))))

    suspend fun analyzeText(config: AiConfig, text: String): String =
        extractJson(call(config, config.textModel, JsonPrimitive(textPrompt(text))))

    suspend fun testConfig(config: AiConfig) {
        call(config, config.textModel, JsonPrimitive("请简短回复测试成功"), maxTokens = 256)
    }

    private suspend fun call(
        config: AiConfig,
        model: String,
        content: JsonElement,
        maxTokens: Int = 4_000
    ): String = withContext(Dispatchers.IO) {
        val payload = ChatRequest(model, listOf(UserMessage("user", content)), maxTokens)
        val request = Request.Builder()
            .url(completionsUrl(config.endpoint))
            .header("Authorization", "Bearer ${config.apiKey}")
            .post(json.encodeToString(payload).toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw AiServiceException("AI 接口返回 HTTP ${response.code}")
            json.decodeFromString<ChatResponse>(response.body.string()).choices.firstOrNull()?.message?.content
                ?: throw AiServiceException("AI 接口没有返回文本")
        }
    }

    internal fun completionsUrl(endpoint: String): HttpUrl {
        val url = endpoint.toHttpUrl()
        val path = url.encodedPath.trimEnd('/')
        val completionsPath = if (path.endsWith("/chat/completions")) path else "$path/chat/completions"
        return url.newBuilder().encodedPath(completionsPath).build()
    }

    private fun extractJson(content: String): String {
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        if (start < 0 || end <= start) throw AiServiceException("AI 回复没有包含课表 JSON")
        return content.substring(start, end + 1)
    }

    private fun imagePrompt(): String = """
你是课表抽取助手。请从这张课表图片中提取完整课程数据，并仅输出符合以下严格模式的 JSON（不要输出任何说明/Markdown/多余文本）：

输出 JSON 严格结构：
{
  "name": "不超过6个字的课表名",
  "courses": [
    {
      "name": "课程名称",
      "location": "上课地点",
      "teacher": "教师姓名",
      "schedules": [
        {
          "weekday": 1,
          "periods": [1,2],
          "weekPattern": [1,2,3,4,5,6]
        }
      ]
    }
  ]
}

规范要求：
- 仅输出 JSON；不要输出任何多余文字。
- 将周范围（如"1-16周/双周/单周"）展开为具体数字数组 weekPattern。
- weekday(星期几) 取值 1..7（1代表星期一，7代表星期日）；periods(第几节) 取值 1..16；数字均为整数。
- 同一课程同一上课时段仅保留一条 schedule（去重合并）。
- 若同名课程出现在不同时间，应合并到同一课程对象的不同 schedules。
""".trimIndent()

    private fun tablePrompt(tableText: String): String = """
你是课表抽取助手。请解析以下"表格/CSV/Excel文本化内容"，输出符合严格模式的 JSON（不要输出任何说明/Markdown/多余文本）：

源数据：
$tableText

输出 JSON 严格结构：
{
  "name": "不超过6个字的课表名",
  "courses": [
    {
      "name": "课程名称",
      "location": "上课地点",
      "teacher": "教师姓名",
      "schedules": [
        {
          "weekday": 1,
          "periods": [1,2],
          "weekPattern": [1,2,3,4,5,6]
        }
      ]
    }
  ]
}

规范要求：
- 仅输出 JSON；不要输出任何多余文字。
- 将"1-16周/单周/双周"等转换为具体 weekPattern 数组。
- weekday 1..7（1代表星期一，7代表星期日）；periods 1..16；整数。
- 合并同一课程的不同时间到同一课程对象的多个 schedules；去重相同时间的重复记录。
""".trimIndent()

    private fun textPrompt(text: String): String = """
你是课表抽取助手。请从以下"自然语言文字描述"中抽取课程数据，并仅输出符合严格模式的 JSON（不要输出任何说明/Markdown/多余文本）：

原始描述：
$text

输出 JSON 严格结构：
{
  "name": "不超过6个字的课表名",
  "courses": [
    {
      "name": "课程名称",
      "location": "上课地点",
      "teacher": "教师姓名",
      "schedules": [
        {
          "weekday": 1,
          "periods": [1,2],
          "weekPattern": [1,2,3,4,5,6]
        }
      ]
    }
  ]
}

规范要求：
- 仅输出 JSON；不要输出任何多余文字。
- 将"1-16周/单周/双周"等文字规则转换为具体 weekPattern 数组。
- weekday 1..7（1代表星期一，7代表星期日）；periods 1..16；整数。
- 合并同名课程，不同上课时间放入同一课程对象的 schedules；去重重复时间。
""".trimIndent()
}
