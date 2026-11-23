package wykwey.yiclass.domain.util

/**
 * 周次/节次字符串解析与格式化，对齐原 Flutter 版 ParseUtils。
 *
 * 支持格式：`1` | `2-4` | `6-8[单]` | `1,3-5` | `all`
 * 归一化全角数字、破折号变体、中文逗号/方括号。
 */
object NumberPatternParser {

    class PatternFormatException(message: String) : Exception(message)

    private val fullWidthDigits = mapOf(
        '０' to '0', '１' to '1', '２' to '2', '３' to '3', '４' to '4',
        '５' to '5', '６' to '6', '７' to '7', '８' to '8', '９' to '9',
    )
    private val dashVariants = listOf('－', '—', '–', '―')

    /** 解析为升序去重的整数列表；失败抛 [PatternFormatException]。 */
    fun parse(input: String, defaultMax: Int = 30): List<Int> {
        if (input.isEmpty()) throw PatternFormatException("输入不能为空")
        if (input == "all") return (1..defaultMax).toList()

        val normalized = normalize(input)
        val numbers = sortedSetOf<Int>()

        for (part in normalized.replace(' ', ',').split(',')) {
            val trimmed = part.trim()
            if (trimmed.isEmpty()) continue
            if (trimmed.contains('-')) {
                parseRange(trimmed, numbers)
            } else {
                val n = trimmed.toIntOrNull() ?: throw PatternFormatException("无法解析: \"$trimmed\"")
                numbers.add(n)
            }
        }
        if (numbers.isEmpty()) throw PatternFormatException("无法从 \"$input\" 解析出有效数字")
        return numbers.toList()
    }

    /** 格式化为紧凑串，如 [1,2,3,5] → "1-3, 5"。 */
    fun format(numbers: List<Int>): String {
        if (numbers.isEmpty()) return ""
        val sorted = numbers.toSortedSet().toList()
        val result = mutableListOf<String>()
        var start = sorted[0]
        var end = sorted[0]
        for (i in 1..sorted.size) {
            if (i < sorted.size && sorted[i] == end + 1) {
                end = sorted[i]
            } else {
                result.add(if (start == end) "$start" else "$start-$end")
                if (i < sorted.size) {
                    start = sorted[i]
                    end = sorted[i]
                }
            }
        }
        return result.joinToString(", ")
    }

    /** 校验格式，返回错误信息或 null。 */
    fun validate(pattern: String): String? {
        if (pattern.isEmpty()) return "模式不能为空"
        val normalized = normalize(pattern)
        val regex = Regex("""^(\d+(-\d+)?(\[单]|\[双])?)(,\s*\d+(-\d+)?(\[单]|\[双])?)*$""")
        return if (!regex.matches(normalized)) "格式错误，示例：1-16、1,3,5、1-3,5,7-9" else null
    }

    private fun normalize(input: String): String {
        var result = input
        fullWidthDigits.forEach { (k, v) -> result = result.replace(k, v) }
        dashVariants.forEach { dash -> result = result.replace(dash, '-') }
        return result
            .replace('，', ',')
            .replace('【', '[')
            .replace('】', ']')
    }

    private fun parseRange(trimmed: String, numbers: MutableSet<Int>) {
        val parts = trimmed.split('-')
        if (parts.size != 2) throw PatternFormatException("范围格式错误: \"$trimmed\"")
        val start = parts[0].trim().toIntOrNull()
        val endStr = parts[1].replace(Regex("""\[.*]"""), "").trim()
        val end = endStr.toIntOrNull()
        if (start == null || end == null) throw PatternFormatException("范围格式错误: \"$trimmed\"")
        if (start > end) throw PatternFormatException("范围起始值不能大于结束值: \"$trimmed\"")

        val isOdd = trimmed.contains("[单]")
        val isEven = trimmed.contains("[双]")
        for (i in start..end) {
            if (isOdd && i % 2 == 0) continue
            if (isEven && i % 2 != 0) continue
            numbers.add(i)
        }
    }
}
