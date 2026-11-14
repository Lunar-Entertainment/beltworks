package com.microfactory.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.microfactory.render.FactoryColors

private val IndustrialDarkScheme = darkColorScheme(
    primary = FactoryColors.BeltChevron,
    onPrimary = FactoryColors.Background,
    secondary = FactoryColors.HatchAccent,
    onSecondary = FactoryColors.Background,
    tertiary = FactoryColors.SpawnerAccent,
    background = FactoryColors.Background,
    onBackground = FactoryColors.TextPrimary,
    surface = FactoryColors.SurfaceElevated,
    onSurface = FactoryColors.TextPrimary,
    surfaceVariant = FactoryColors.GridCell,
    onSurfaceVariant = FactoryColors.TextSecondary,
    error = FactoryColors.BulldozeAccent
)

@Composable
fun MicroFactoryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IndustrialDarkScheme,
        content = content
    )
}
