package com.github.codebydusk.quotd_quoteoftheday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuotdLandingScreen()
        }
    }
}

@Composable
fun QuotdLandingScreen() {
    val bgDark = Color(0xFF0D0D0D)
    val surfaceColor = Color(0xFF1A1A1A)
    val textPrimary = Color(0xFFF5F5F5)
    val textSecondary = Color(0xFF777777)
    val textMuted = Color(0xFF555555)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(96.dp).padding(bottom = 16.dp)
            )

            // App name
            Text(
                text = "quotd",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = textPrimary,
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Quote of the Day",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = textSecondary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Tagline
            Text(
                text = "A widget full of reasons, wisdom,\nchaos, and emotional damage.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No accounts. No internet. No tracking.\nJust words.",
                fontSize = 13.sp,
                color = textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Instruction card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "↓",
                        fontSize = 24.sp,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Long-press your home screen\nand add a quotd widget",
                        fontSize = 14.sp,
                        color = textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            // Footer
            Text(
                text = "From love letters to sarcastic horoscopes—\none widget, endless moods.",
                fontSize = 11.sp,
                color = textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuotdLandingScreenPreview() {
    QuotdLandingScreen()
}