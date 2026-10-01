package com.example.easyespanol.data

/** A run of phrase text. chunk = 1..9 for a colour-linked chunk, 0 for neutral text. */
data class Segment(val text: String, val chunk: Int)

/**
 * Reads the markup used in the CSV files (see docs/DATA_GUIDE.md):
 *   {2:me llamo}  a numbered chunk, drawn in the same colour as {2:...} in the English
 *   cansad[o|a]   speaker-gender forms: masculine before the bar, feminine after
 */
object Markup {
    private val chunkRegex = Regex("""\{(\d):([^}]*)\}""")
    private val genderRegex = Regex("""\[([^|\]]*)\|([^\]]*)\]""")

    fun applyGender(text: String, gender: SpeakerGender): String =
        genderRegex.replace(text) { m ->
            if (gender == SpeakerGender.MALE) m.groupValues[1] else m.groupValues[2]
        }

    fun segments(text: String, gender: SpeakerGender): List<Segment> {
        val resolved = applyGender(text, gender)
        val out = mutableListOf<Segment>()
        var last = 0
        for (m in chunkRegex.findAll(resolved)) {
            if (m.range.first > last) out.add(Segment(resolved.substring(last, m.range.first), 0))
            out.add(Segment(m.groupValues[2], m.groupValues[1].toInt()))
            last = m.range.last + 1
        }
        if (last < resolved.length) out.add(Segment(resolved.substring(last), 0))
        return out
    }

    /** The phrase with all markup removed, e.g. for text-to-speech. */
    fun plain(text: String, gender: SpeakerGender): String =
        segments(text, gender).joinToString("") { it.text }
}
