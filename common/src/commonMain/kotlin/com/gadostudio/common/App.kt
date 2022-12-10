package com.gadostudio.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ListObjectsRequest
import aws.sdk.kotlin.services.s3.model.Object
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.http.Url
import java.awt.FileDialog
import java.awt.Frame

data class Credential(
    val accessKey: String,
    val secretKey: String,
    val region: String,
    val endpoint: String,
    val bucket: String
)

sealed class Screens {
    object SignIn : Screens()
    class Explorer(val credential: Credential) : Screens()
}

val LocalScreens = compositionLocalOf<MutableState<Screens>> {
    error("You should use this directly")
}

@Composable
fun SignIn() {
    var credential by remember { mutableStateOf(Credential(
        accessKey = "",
        secretKey = "",
        region = "",
        endpoint = "",
        bucket = ""
    )) }

    val (_, setScreen) = LocalScreens.current

    Column {
        TextField(
            value = credential.accessKey,
            onValueChange = {
                credential = credential.copy(accessKey = it)
            }
        )
        TextField(
            value = credential.secretKey,
            onValueChange = {
                credential = credential.copy(secretKey = it)
            }
        )
        TextField(
            value = credential.endpoint,
            onValueChange = {
                credential = credential.copy(endpoint = it)
            }
        )
        TextField(
            value = credential.region,
            onValueChange = {
                credential = credential.copy(region = it)
            }
        )
        TextField(
            value = credential.bucket,
            onValueChange = {
                credential = credential.copy(bucket = it)
            }
        )
        Button(
            onClick = {
                setScreen(Screens.Explorer(credential))
            },
            content = {
                Text("Sign In")
            }
        )
    }
}

sealed class NetworkState<T> {
    class Loading<T>() : NetworkState<T>()
    class Loaded<T>(val data: T) : NetworkState<T>()
}

fun saveFile(defaultFileName: String) {
    FileDialog(null as Frame?, "Select file destination").apply {
        mode = FileDialog.SAVE
        file = defaultFileName
        isVisible = true
    }
}

@Composable
fun Explorer() {
    val (screen, _) = LocalScreens.current
    if (screen !is Screens.Explorer) {
        error("Unreachable")
    }

    var objects by remember { mutableStateOf<NetworkState<List<Object>>>(NetworkState.Loading()) }

    LaunchedEffect(Unit) {
        val credential = screen.credential
        val client = S3Client {
            credentialsProvider = StaticCredentialsProvider(
                Credentials(
                    credential.accessKey,
                    credential.secretKey
                )
            )
            region = credential.region
            endpointUrl = Url.parse(credential.endpoint)
        }
        val listObjects = client.listObjects(ListObjectsRequest {
            bucket = credential.bucket
        })
        val objectsContent = listObjects.contents
        objectsContent?.let { b ->
            objects = NetworkState.Loaded(b)
        }
    }

    when (objects) {
        is NetworkState.Loading -> {
            Text("Loading...")
        }
        is NetworkState.Loaded -> {
            LazyColumn {
                val data = (objects as NetworkState.Loaded<List<Object>>).data
                items(data) { obj ->
                    Text("Key: " + obj.key, modifier = Modifier.clickable {
                        saveFile("pepe.jpg")
                    })
                }
            }
        }
    }
}

@Composable
fun App() {
    val screen = remember { mutableStateOf<Screens>(Screens.SignIn) }

    CompositionLocalProvider(LocalScreens provides screen) {
        when (LocalScreens.current.component1()) {
            Screens.SignIn -> {
                SignIn()
            }

            is Screens.Explorer -> {
                Explorer()
            }
        }
    }
}
