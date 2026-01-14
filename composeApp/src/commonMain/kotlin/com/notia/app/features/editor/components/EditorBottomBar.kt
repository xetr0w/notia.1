package com.notia.app.features.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notia.app.ui.theme.AccentBlack
import com.notia.app.ui.theme.AccentBlue
import com.notia.app.ui.theme.AccentGreen
import com.notia.app.ui.theme.ToolbarBackground

@Composable
fun EditorBottomBar() {
    Box(
        modifier = Modifier
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.1f))
                .background(ToolbarBackground, RoundedCornerShape(32.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Colors
            ColorDot(AccentBlack, true)
            ColorDot(AccentBlue, false)
            ColorDot(Color(0xFF2196F3), false) // Light Blue
            ColorDot(AccentGreen, false)
            
            // Add Color
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Gray.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 Icon(Icons.Default.Add, "Add Color", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }

            VerticalDivider()

            // Slider Placeholder
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(4.dp)
                    .background(Color.LightGray, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.CenterStart)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                )
            }

            VerticalDivider()
            
            // Text Tool
            Text("AA", fontWeight = FontWeight.Bold, color = AccentBlack, fontSize = 14.sp)
            
            VerticalDivider()
            
            // Pen/Tools
             Icon(Icons.Default.Create, "Pen", tint = AccentBlack.copy(alpha=0.6f))
        }
    }
}

@Composable
fun ColorDot(color: Color, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color, CircleShape)
            .border(if (isSelected) 2.dp else 0.dp, if(isSelected) Color.LightGray else Color.Transparent, CircleShape)
    )
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(Color.LightGray.copy(alpha = 0.5f))
    )
}
