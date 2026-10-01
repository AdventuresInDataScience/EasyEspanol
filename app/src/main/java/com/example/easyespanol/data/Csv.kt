package com.example.easyespanol.data

/**
 * A small CSV reader that understands quoted fields, so commas and quotes
 * inside a phrase or note don't break it (the old split(",") approach did).
 */
object Csv {

    /** Returns one map per row, keyed by the column names in the header line. */
    fun parse(text: String): List<Map<String, String>> {
        val rows = parseRows(text.removePrefix("\uFEFF"))
        if (rows.isEmpty()) return emptyList()
        val header = rows.first().map { it.trim() }
        return rows.drop(1)
            .filter { row -> row.any { it.isNotBlank() } }
            .map { row -> header.indices.associate { i -> header[i] to row.getOrElse(i) { "" } } }
    }

    private fun parseRows(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length && text[i + 1] == '"') {
                        field.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    field.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    ',' -> {
                        row.add(field.toString())
                        field.setLength(0)
                    }
                    '\r' -> Unit
                    '\n' -> {
                        row.add(field.toString())
                        field.setLength(0)
                        rows.add(row)
                        row = mutableListOf()
                    }
                    else -> field.append(c)
                }
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            rows.add(row)
        }
        return rows
    }
}
