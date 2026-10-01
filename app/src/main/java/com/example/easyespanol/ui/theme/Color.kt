package com.example.easyespanol.ui.theme

import androidx.compose.ui.graphics.Color

// Spanish flag colours, shared by the app icon and the flag on the home screen.
val FlagRed = Color(0xFFAA151B)
val FlagGold = Color(0xFFF1BF00)

/**
 * Colours for the numbered chunks that link the Spanish to the English.
 * Item 0 is chunk {1:...}, item 1 is chunk {2:...}, and so on (nine in total).
 * The dark-mode set is lighter so it stays readable on a dark background.
 */
val ChunkColoursLight = listOf(
    Color(0xFFC62828), // 1 red
    Color(0xFF1565C0), // 2 blue
    Color(0xFF2E7D32), // 3 green
    Color(0xFFE65100), // 4 orange
    Color(0xFF6A1B9A), // 5 purple
    Color(0xFF00838F), // 6 teal
    Color(0xFFAD1457), // 7 pink
    Color(0xFF5D4037), // 8 brown
    Color(0xFF827717), // 9 olive
)

val ChunkColoursDark = listOf(
    Color(0xFFEF9A9A),
    Color(0xFF90CAF9),
    Color(0xFFA5D6A7),
    Color(0xFFFFCC80),
    Color(0xFFCE93D8),
    Color(0xFF80DEEA),
    Color(0xFFF48FB1),
    Color(0xFFBCAAA4),
    Color(0xFFE6EE9C),
)
