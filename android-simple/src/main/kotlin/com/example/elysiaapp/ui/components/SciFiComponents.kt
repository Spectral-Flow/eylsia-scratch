package com.example.elysiaapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.elysiaapp.ui.theme.SciFiColors

@Composable
fun NeonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = SciFiColors.NeonCyan,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = SciFiColors.TextSecondary
    ),
    content: @Composable RowScope.() -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "button_scale"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (enabled) 0.8f else 0.3f,
        animationSpec = tween(300),
        label = "glow_alpha"
    )

    Button(
        onClick = {
            pressed = true
            onClick()
            pressed = false
        },
        modifier = modifier
            .scale(animatedScale)
            .drawBehind {
                if (enabled) {
                    drawNeonGlow(
                        color = SciFiColors.NeonCyan,
                        alpha = glowAlpha,
                        blurRadius = 12.dp.toPx()
                    )
                }
            }
            .border(
                width = 1.dp,
                color = if (enabled) SciFiColors.NeonCyan else SciFiColors.TextSecondary,
                shape = RoundedCornerShape(8.dp)
            ),
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        content = content
    )
}

@Composable
fun HolographicCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "holographic_transition")
    val borderOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "border_animation"
    )

    Card(
        modifier = modifier
            .drawBehind {
                drawHolographicBorder(borderOffset)
            },
        colors = CardDefaults.cardColors(
            containerColor = SciFiColors.DarkSurface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun StatusIndicator(
    isActive: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    val color = if (isActive) SciFiColors.SuccessGreen else SciFiColors.ErrorRed
    val infiniteTransition = rememberInfiniteTransition(label = "status_transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_alpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = color.copy(alpha = alpha),
                    shape = RoundedCornerShape(6.dp)
                )
                .drawBehind {
                    drawCircle(
                        color = color,
                        radius = 6.dp.toPx(),
                        alpha = alpha * 0.3f
                    )
                }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                text = label,
                color = SciFiColors.NeonPurple
            )
        },
        modifier = modifier
            .drawBehind {
                if (enabled) {
                    drawNeonGlow(
                        color = SciFiColors.NeonPurple,
                        alpha = 0.3f,
                        blurRadius = 8.dp.toPx()
                    )
                }
            },
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SciFiColors.NeonPurple,
            unfocusedBorderColor = SciFiColors.BorderGlow,
            focusedTextColor = SciFiColors.TextPrimary,
            unfocusedTextColor = SciFiColors.TextSecondary,
            cursorColor = SciFiColors.NeonCyan,
            disabledBorderColor = SciFiColors.TextSecondary.copy(alpha = 0.3f),
            disabledTextColor = SciFiColors.TextSecondary.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun GlowingDivider(
    modifier: Modifier = Modifier,
    color: Color = SciFiColors.NeonCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "divider_transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "divider_alpha"
    )

    Box(
        modifier = modifier
            .height(1.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = alpha),
                        Color.Transparent
                    )
                )
            )
    )
}

private fun DrawScope.drawNeonGlow(
    color: Color,
    alpha: Float,
    blurRadius: Float
) {
    repeat(3) { i ->
        drawRect(
            color = color,
            alpha = alpha / (i + 1),
            size = size
        )
    }
}

private fun DrawScope.drawHolographicBorder(offset: Float) {
    val borderColors = listOf(
        SciFiColors.NeonCyan,
        SciFiColors.NeonPurple,
        SciFiColors.NeonGreen,
        SciFiColors.NeonCyan
    )
    
    val gradientWidth = size.width / 3
    val startX = (offset * (size.width + gradientWidth)) - gradientWidth
    
    drawRect(
        brush = Brush.horizontalGradient(
            colors = borderColors,
            startX = startX,
            endX = startX + gradientWidth
        ),
        size = size,
        alpha = 0.6f
    )
}