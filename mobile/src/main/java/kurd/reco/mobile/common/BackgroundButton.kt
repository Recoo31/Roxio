package kurd.reco.mobile.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun TransparentButton() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(Color.Cyan)
            .clip(RoundedCornerShape(0.dp)), // Köşe yarıçapı için
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { /* Tıklama işlemi */ },
            modifier = Modifier.size(80.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor  = Color.White.copy(alpha = 0.8f)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Pause", color = Color.Black)
        }
    }
}

