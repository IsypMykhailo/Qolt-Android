package ca.qolt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatisticsScreen() {
    val orange = Color(0xFFFF6A1A)
    val lightGray = Color(0xFF888888)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Statistics",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatCard(
                title = "Total Blocks",
                value = "12",
                subtitle = "This month",
                backgroundColor = orange
            )

            StatCard(
                title = "Time Saved",
                value = "45h",
                subtitle = "Focused time",
                backgroundColor = Color(0xFF4CAF50)
            )

            StatCard(
                title = "Apps Blocked",
                value = "8",
                subtitle = "Most blocked",
                backgroundColor = Color(0xFF2196F3)
            )

            StatCard(
                title = "Streak",
                value = "7 days",
                subtitle = "Current streak",
                backgroundColor = Color(0xFF9C27B0)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "More detailed stats coming soon!",
                color = lightGray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                backgroundColor.copy(alpha = 0.15f),
                RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF888888),
                fontSize = 14.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = backgroundColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
