package com.neuronova.mimomento.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.neuronova.mimomento.data.model.MiMomentoThemeDefinition

@Composable
fun ThemedBackground(
    theme: MiMomentoThemeDefinition,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val isHighContrast = LocalHighContrast.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isHighContrast) Color.Black else Color.Transparent),
    ) {
        if (!isHighContrast) {
            Image(
                painter = painterResource(id = theme.backgroundRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme.visual.scrimColor.copy(alpha = theme.visual.overlayAlpha)),
            )
        }

        content()
    }
}
