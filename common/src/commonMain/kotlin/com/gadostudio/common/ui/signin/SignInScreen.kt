package com.gadostudio.common.ui.signin

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.gadostudio.common.domain.Credential
import com.gadostudio.common.domain.NetworkState
import com.gadostudio.common.ui.explorer.ExplorerScreen
import kotlinx.coroutines.flow.collectLatest

class SignInScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { SignInScreenModel() }
        val state = screenModel.state.collectAsState()

        LaunchedEffect(state.value.signInState) {
            if (state.value.signInState is NetworkState.Loaded) {
                navigator.push(ExplorerScreen(state.value.toCredential()))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("S3Explorer", fontSize = 30.sp)
            Spacer(Modifier.height(50.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(value = state.value.accessKey, onValueChange = {
                    screenModel.doAction(SignInAction.ChangeAccessKey(it))
                }, label = {
                    Text("Access Key")
                }, readOnly = state.value.signInState is NetworkState.Loading)
                TextField(value = state.value.secretKey, onValueChange = {
                    screenModel.doAction(SignInAction.ChangeSecretKey(it))
                }, label = {
                    Text("Secret Key")
                }, readOnly = state.value.signInState is NetworkState.Loading)
                TextField(value = state.value.endpoint, onValueChange = {
                    screenModel.doAction(SignInAction.ChangeEndpoint(it))
                }, label = {
                    Text("Endpoint")
                }, readOnly = state.value.signInState is NetworkState.Loading)
                TextField(value = state.value.region, onValueChange = {
                    screenModel.doAction(SignInAction.ChangeRegion(it))
                }, label = {
                    Text("Region")
                }, readOnly = state.value.signInState is NetworkState.Loading)
                TextField(value = state.value.bucket, onValueChange = {
                    screenModel.doAction(SignInAction.ChangeBucket(it))
                }, label = {
                    Text("Bucket Name")
                }, readOnly = state.value.signInState is NetworkState.Loading)
                Button(onClick = {
                    screenModel.doAction(SignInAction.SignIn)
                }, content = {
                    Text("Sign In")
                }, enabled = state.value.signInState !is NetworkState.Loading)
                state.value.signInState?.let {
                    when (it) {
                        is NetworkState.Error -> {
                            Text("Error: ${it.error.message}", color = Color.Red)
                        }

                        is NetworkState.Loaded -> {
                            Text("Success", color = Color.Green)
                        }

                        is NetworkState.Loading -> {
                            Text("Loading...")
                        }
                    }
                }
            }
        }
    }
}
