package ca.qolt.ui.statistics

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ca.qolt.R

class FocusTimeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FocusTimeGlanceWidget()
}

class FocusTimeGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            FocusTimeGlanceContent(
                totalHours = 38.4f,
                period = "this week"
            )
        }
    }
}

@Composable
private fun FocusTimeGlanceContent(
    totalHours: Float,
    period: String
) {
    // We use a single Column to represent the card
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.focus_orange))
            .cornerRadius(16.dp)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        // Top Label
        Text(
            text = "Total Focus Time",
            style = TextStyle(
                color = ColorProvider(R.color.white),
                fontSize = 16.sp
            )
        )

        Spacer(GlanceModifier.height(4.dp))

        // Big Number and "hrs" unit
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = String.format("%.1f", totalHours),
                style = TextStyle(
                    color = ColorProvider(R.color.white),
                    fontSize = 52.sp, // Large size for the number
                )
            )

            Spacer(GlanceModifier.width(6.dp))

            // "hrs" sits slightly lower or at baseline
            Text(
                text = "hrs",
                style = TextStyle(
                    color = ColorProvider(R.color.white_opacity_80), // Slightly dimmer
                    fontSize = 20.sp
                ),
                modifier = GlanceModifier.padding(bottom = 8.dp) // Visual alignment
            )
        }

        Spacer(GlanceModifier.height(4.dp))

        // Bottom Label
        Text(
            text = period,
            style = TextStyle(
                color = ColorProvider(R.color.white),
                fontSize = 18.sp
            )
        )
    }
}