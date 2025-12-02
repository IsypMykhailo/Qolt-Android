package ca.qolt.ui.registration.qolttag

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.qolt.R

@Composable
fun QoltTag(modifier: Modifier = Modifier, viewModel: QoltTagViewModel) {
    BackHandler { viewModel.navigator.finish() }
    QoltTagScreen(viewModel::onHaveTag, viewModel::onNoTag)
}

@Composable
fun QoltTagScreen(
    onHaveTag: () -> Unit = {},
    onNoTag: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter = painterResource(R.drawable.qolt_tag_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "QOLT TAG is required",
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "QOLT",
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.microgramma)),
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Focus made effortless",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 17.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onHaveTag,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6A1A)
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                Text(
                    text = "I have a QOLT TAG",
                    color = Color.White,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = onNoTag,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                Text(
                    text = "I don't have a QOLT TAG yet",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}
