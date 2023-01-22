package com.gadostudio.common.ui.explorer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.gadostudio.common.domain.Credential
import com.gadostudio.common.domain.NetworkState
import com.gadostudio.common.formatAsFileSize

class ExplorerScreen(private val credential: Credential) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { ExplorerScreenModel(credential) }
        val state = screenModel.state.collectAsState()

        LaunchedEffect(Unit) {
            screenModel.doAction(ExplorerAction.LoadObjects)
        }

        when (val objects = state.value.objects) {
            is NetworkState.Loading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Loading...", modifier = Modifier.align(Alignment.Center))
                }
            }

            is NetworkState.Loaded -> {
                LazyColumn {
                    items(objects.data) { obj ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.wrapContentHeight().padding(horizontal = 16.dp).fillMaxWidth()
                                .drawBehind {
                                    val strokeWidth = 1.0F
                                    val y = size.height - strokeWidth / 2

                                    drawLine(
                                        Color.LightGray, Offset(0f, y), Offset(size.width, y), strokeWidth
                                    )
                                }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(obj.key ?: "-")
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(obj.size.formatAsFileSize)
                                Button(onClick = {
                                    screenModel.doAction(ExplorerAction.SaveObject(obj.key!!))
                                }) {
                                    Text("Download")
                                }
                            }
                        }
                    }
                }
            }

            is NetworkState.Error -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Error: ${objects.error.message}", color = Color.Red)
                }
            }
        }
    }
}
