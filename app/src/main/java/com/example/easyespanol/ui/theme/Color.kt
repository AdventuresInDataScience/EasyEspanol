package com.example.easyespanol.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Colours for the numbered chunks that link the Spanish to the English.
 * Item 0 is chunk {1:...}, item 1 is chunk {2:...}, and so on (nine in total).
 * Chosen to stay distinct from each other, and readable on their background.
 */
val ChunkColoursLight = listOf(
    Color(0xFFD1263F), // 1 red
    Color(0xFF2D4BD8), // 2 cobalt
    Color(0xFF13866A), // 3 green
    Color(0xFFB85A00), // 4 orange
    Color(0xFF8A3FD1), // 5 violet
    Color(0xFF0A7FA3), // 6 sea blue
    Color(0xFFC2287A), // 7 pink
    Color(0xFF6B5A3E), // 8 olive brown
    Color(0xFF5C7A0E), // 9 leaf
)

val ChunkColoursDark = listOf(
    Color(0xFFFF8A9A),
    Color(0xFF9AAAFF),
    Color(0xFF5FD9B4),
    Color(0xFFFFB866),
    Color(0xFFC9A2FF),
    Color(0xFF6ED0F0),
    Color(0xFFFF8CC6),
    Color(0xFFD9C29A),
    Color(0xFFB7DD6A),
)

// "I know this" and "Still learning" keep the same meaning in every palette.
val KnownLight = Color(0xFF0F7F5C)
val KnownDark = Color(0xFF4FD1A5)
val LearningLight = Color(0xFFE07A00)
val LearningDark = Color(0xFFFFB547)
