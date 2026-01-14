package com.notia.app.features.noteslist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notia.app.ui.theme.NotebookCoverColors

@Composable
fun NotebookCover(
    title: String,
    coverIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Elegant randomized gradient logic
    val baseColor = NotebookCoverColors[coverIndex % NotebookCoverColors.size]
    val gradientColors = listOf(
        baseColor.copy(alpha = 0.8f),
        baseColor.copy(alpha = 0.4f)
    )

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        // "Glow" Layer & Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.80f) // Slightly wider than before for "Digital Card" look
        ) {
            // Glow Effect (Simulation using Gradient behind)
            // This works cross-platform without platform-specific Paint
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp) // Shrink content relative to bounds to allow glow
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(baseColor.copy(alpha = 0.6f), Color.Transparent),
                            radius = 300f
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            // Main Card Surface
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp) // Maintain margin for glow
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = gradientColors
                        )
                    )
            ) {
                // Glass Overlay (Highlight)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    
                    // Top Icon / Badge area 
                    Box(
                        modifier = Modifier
                           .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                           .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                         Text(
                            text = "NOTE", 
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))

                    // Huge Typographic Title
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
