package com.example.yksaisinavkocu.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// === FRUTIGER AERO RENK PALETİ ===

// Birincil — Gökyüzü & Su
val AeroSkyBlue = Color(0xFF4FC3F7)
val AeroDeepBlue = Color(0xFF0288D1)
val AeroOcean = Color(0xFF006064)
val AeroCyan = Color(0xFF00BCD4)

// İkincil — Doğa & Yeşil
val AeroLeafGreen = Color(0xFF66BB6A)
val AeroForest = Color(0xFF2E7D32)
val AeroMint = Color(0xFFB2DFDB)
val AeroLime = Color(0xFFC6FF00)

// Vurgu — Gün Batımı & Enerji
val AeroSunset = Color(0xFFFFB74D)
val AeroCoral = Color(0xFFFF7043)
val AeroRose = Color(0xFFE91E63)
val AeroPurple = Color(0xFF7C4DFF)
val AeroGold = Color(0xFFFFD54F)

// Cam & Yüzey
val GlassWhite = Color(0x33FFFFFF)
val GlassBorder = Color(0x80FFFFFF)
val GlassDark = Color(0x1A000000)
val GlassDarkBorder = Color(0x33FFFFFF)

// Mat Yüzeyler & Kontrastlı Bloklar (Açık Tema — Göz Yormayan Yumuşak Açık Gri)
const val AeroBlockAlpha = 0.70f                    // %30 şeffaf (30% transparent / 70% opacity)
val AeroMatteLightGrey = Color(0xFFE8ECEF)          // Ana kart ve blok mat açık gri (gözü yormayan dengeli yumuşak ton)
val AeroMatteLightGreySecondary = Color(0xFFDFE4E8) // Alt bloklar, satırlar, çip ve seçici zeminleri
val AeroMatteBorderLight = Color(0xFFCAD3DC)        // Yumuşak ve net sınır çizgisi
val AeroMatteInputBg = Color(0xFFF0F3F6)            // Metin giriş alanları zemin rengi

// Metin
val TextPrimaryLight = Color(0xFF1A237E)
val TextSecondaryLight = Color(0xFF455A64)
val TextPrimaryDark = Color(0xFFE3F2FD)
val TextSecondaryDark = Color(0xFFB0BEC5)

// Arka Plan
val AeroLightBg = Color(0xFFDDE5EB) // -8% parlaklık (önceki: 0xFFF0F9FF)
val AeroDarkBg = Color(0xFF0A1929)
val AeroDarkSurface = Color(0xFF132F4C)
val AeroDarkCard = Color(0xFF1A3A5C)

// Durum Renkleri
val SuccessGreen = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFF9800)
val ErrorRed = Color(0xFFF44336)
val InfoBlue = Color(0xFF2196F3)

// === GRADIENT TANIMLARI ===

val SkyGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF87CEEB), Color(0xFF4FC3F7), Color(0xFF29B6F6))
)

val OceanGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF006064), Color(0xFF00838F), Color(0xFF0097A7))
)

val SunsetGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF7043), Color(0xFFFFB74D), Color(0xFFFFF176))
)

val ForestGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2E7D32), Color(0xFF66BB6A), Color(0xFFA5D6A7))
)

val NightGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0A1929), Color(0xFF132F4C), Color(0xFF1A3A5C))
)

// Ders renkleri (grafiklerde kullanılır)
val SubjectColors = mapOf(
    "Türkçe" to Color(0xFF42A5F5),
    "Sosyal Bilimler" to Color(0xFFAB47BC),
    "Temel Matematik" to Color(0xFFEF5350),
    "Fen Bilimleri" to Color(0xFF66BB6A),
    "Matematik" to Color(0xFFEF5350),
    "Fizik" to Color(0xFF29B6F6),
    "Kimya" to Color(0xFFFFCA28),
    "Biyoloji" to Color(0xFF66BB6A),
    "Genel" to Color(0xFF7E57C2)
)
