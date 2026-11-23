package wykwey.yiclass.data.ai

import org.junit.Assert.assertEquals
import org.junit.Test
import wykwey.yiclass.domain.model.AiProvider

class AiServiceTest {
    @Test
    fun keepsProviderBasePathAndDoesNotDuplicateCompletions() {
        assertEquals("https://api.openai.com/v1/chat/completions", AiService.completionsUrl(AiProvider.OPEN_AI.endpoint).toString())
        assertEquals("https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
            AiService.completionsUrl(AiProvider.GEMINI.endpoint).toString())
        assertEquals("https://api.example.test/v1/chat/completions?tenant=school",
            AiService.completionsUrl("https://api.example.test/v1/chat/completions/?tenant=school").toString())
    }
}
