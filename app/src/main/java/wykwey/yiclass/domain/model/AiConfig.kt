package wykwey.yiclass.domain.model

data class AiConfig(
    val apiKey: String = "",
    val endpoint: String = "",
    val visionModel: String = DEFAULT_MODEL,
    val textModel: String = DEFAULT_MODEL,
    val enableImageImport: Boolean = false,
    val enableTableImport: Boolean = false,
    val enableTextImport: Boolean = false,
    val enableWebImport: Boolean = false
) {
    val hasImportOptions: Boolean
        get() = enableImageImport || enableTableImport || enableTextImport || enableWebImport

    val isValid: Boolean
        get() = apiKey.isNotBlank() && endpoint.isNotBlank() && textModel.isNotBlank() &&
            (!enableImageImport || visionModel.isNotBlank())

    companion object {
        const val DEFAULT_MODEL = "gpt-4.1-mini"
    }
}

enum class AiProvider(val label: String, val endpoint: String, val model: String) {
    OPEN_AI("OpenAI", "https://api.openai.com/v1", AiConfig.DEFAULT_MODEL),
    CLAUDE("Claude", "https://api.anthropic.com/v1", "claude-haiku-5-5"),
    GEMINI("Gemini", "https://generativelanguage.googleapis.com/v1beta/openai", "gemini-3.6-flash"),
    DEEP_SEEK("DeepSeek", "https://api.deepseek.com", "deepseek-v4-flash")
}
