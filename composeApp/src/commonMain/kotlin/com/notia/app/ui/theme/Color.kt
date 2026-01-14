package com.notia.app.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Clean Dark Palette
val PremiumBackground = Color(0xFF0F0F0F) // Deepest Black
val PremiumSurface = Color(0xFF1C1C1E) // iOS System Gray 6 Dark
val PremiumSurfaceVariant = Color(0xFF2C2C2E) // iOS System Gray 5 Dark

// Neon Accents
val NeonBlue = Color(0xFF0A84FF)
val NeonIndigo = Color(0xFF5E5CE6)
val NeonGreen = Color(0xFF30D158)
val NeonRed = Color(0xFFFF453A)
val NeonOrange = Color(0xFFFF9F0A)

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0x99EBEBF5) // 60%
val TextTertiary = Color(0x4DEBEBF5) // 30%

// Notebook Cover Colors from Old Design
val CoverOnyx = Color(0xFF2d3436)
val CoverElectronBlue = Color(0xFF0984e3)
val CoverChiGongRed = Color(0xFFd63031)
val CoverMintLeaf = Color(0xFF00b894)
val CoverExodarPurple = Color(0xFF6c5ce7)
val CoverLightOrange = Color(0xFFe17055)
val CoverBrightYarrow = Color(0xFFfdcb6e)
val CoverPrunusAviumPink = Color(0xFFe84393)
val CoverRobinsEggBlue = Color(0xFF00cec9)

val NotebookCoverColors = listOf(
    CoverOnyx, CoverElectronBlue, CoverChiGongRed, 
    CoverMintLeaf, CoverExodarPurple, CoverLightOrange,
    CoverBrightYarrow, CoverPrunusAviumPink, CoverRobinsEggBlue
)

// Legacy / Fallback Light Theme (Optional)
val PrimaryLight = NeonBlue
val OnPrimaryLight = Color.White
val PrimaryContainerLight = Color(0xFFD1E4FF)
val OnPrimaryContainerLight = Color(0xFF004880)
val SecondaryLight = NeonIndigo
val OnSecondaryLight = Color.White
val SecondaryContainerLight = Color(0xFFE0E0FF)
val OnSecondaryContainerLight = Color(0xFF1D0061)
val TertiaryLight = NeonOrange
val OnTertiaryLight = Color.White
val TertiaryContainerLight = Color(0xFFFFDCC2)
val OnTertiaryContainerLight = Color(0xFF3E1C00)
val ErrorLight = NeonRed
val OnErrorLight = Color.White
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)
val BackgroundLight = Color(0xFFF2F2F7) // iOS System Gray 6 Light
val OnBackgroundLight = Color(0xFF1C1C1E)
val SurfaceLight = Color.White
val OnSurfaceLight = Color(0xFF1C1C1E)
val SurfaceVariantLight = Color(0xFFE1E2EC)
val OnSurfaceVariantLight = Color(0xFF44474F)
val OutlineLight = Color(0xFF74777F)
val OutlineVariantLight = Color(0xFFC4C7D0)

// Clean Paper Palette (Replication)
val PaperBackground = Color(0xFFFDFDFD) // Off-white clean paper
val PaperLine = Color(0xFFE3E9F2) // Soft blue-grey line
val ToolbarBackground = Color(0xFFFFFFFF)
val ToolbarShadow = Color(0x1A000000) // Soft shadow
val AccentBlue = Color(0xFF2979FF)
val AccentGreen = Color(0xFF00E676)
val AccentBlack = Color(0xFF202124)
