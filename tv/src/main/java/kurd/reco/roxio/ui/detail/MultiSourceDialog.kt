package kurd.reco.roxio.ui.detail

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.RadioButton
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import kurd.reco.core.api.model.PlayDataModel
import kurd.reco.roxio.PlayerActivity

@Composable
fun MultiSourceDialog(
    playDataModel: PlayDataModel?,
    context: Context,
    updatePlayDataModel: (PlayDataModel) -> Unit
) {
    val urls = playDataModel?.urls ?: return

    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn {
            item {
                Text(
                    text = "Select a source",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                
                Box(modifier = Modifier.padding(8.dp).height(2.dp).fillMaxWidth().background(MaterialTheme.colorScheme.onSurface))
            }

            items(urls) { item ->
                val interaction = remember { MutableInteractionSource() }

                Surface(
                    onClick = {
                        val updatedList = mutableListOf<Pair<String, String>>().apply {
                            add(item)
                            playDataModel.urls.forEach { if (it != item) add(it) }
                        }
                        updatePlayDataModel(playDataModel.copy(urls = updatedList))
                        context.startActivity(Intent(context, PlayerActivity::class.java))
                    },
                    interactionSource = interaction,
                    modifier = Modifier.padding(8.dp)
                ) {
                    val isFocused by interaction.collectIsFocusedAsState()

                    Text(
                        text = item.first,
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

    }

}