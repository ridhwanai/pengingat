package id.pengingat.app.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.pengingat.app.ui.theme.Gold
import id.pengingat.app.ui.theme.NavyBg
import kotlin.math.min

/**
 * Latar pemandangan anime + tirai gelap agar teks terbaca + cahaya emas
 * bernapas sangat pelan (satu animasi alpha murah, tetap ringan & smooth).
 */
@Composable
fun SceneryBackground(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "glow")
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(4_000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            NavyBg.copy(alpha = 0.55f),
                            NavyBg.copy(alpha = 0.78f),
                            NavyBg.copy(alpha = 0.94f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(
                            Gold.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}

/**
 * Cincin streak animasi: busur emas mengisi halus menuju target 30 hari
 * (setelah 30, cincin penuh), angka di tengah menghitung naik.
 */
@Composable
fun StreakRing(streak: Int, modifier: Modifier = Modifier) {
    val target = min(streak / 30f, 1f)
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(1_200),
        label = "streakProgress"
    )
    val display by animateFloatAsState(
        targetValue = streak.toFloat(),
        animationSpec = tween(900),
        label = "streakCount"
    )
    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Color.White.copy(alpha = 0.14f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(Gold, Color(0xFFE8C874), Gold)),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = display.toInt().toString(),
                color = Gold,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "hari bersih",
                color = Color(0xFFF5EFE0),
                fontSize = 14.sp
            )
        }
    }
}
