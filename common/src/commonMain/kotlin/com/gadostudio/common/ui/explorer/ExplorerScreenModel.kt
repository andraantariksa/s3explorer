package com.gadostudio.common.ui.explorer

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.ListObjectsRequest
import aws.sdk.kotlin.services.s3.model.Object
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.toByteArray
import aws.smithy.kotlin.runtime.http.Url
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import com.gadostudio.common.domain.Credential
import com.gadostudio.common.domain.NetworkState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object ExplorerSideEffect

data class ExplorerState(
    val objects: NetworkState<List<Object>> = NetworkState.Loading()
)

sealed class ExplorerAction {
    object LoadObjects : ExplorerAction()
    class SaveObject(val key: String) : ExplorerAction()
}

class ExplorerScreenModel(private val credential: Credential) : ScreenModel {
    private val _state = MutableStateFlow(ExplorerState())
    val state = _state.asStateFlow()

    private val _sideEffect = MutableSharedFlow<ExplorerSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    fun doAction(action: ExplorerAction) {
        _state.value = reducer(state.value, action)
    }

    private fun reducer(state: ExplorerState, action: ExplorerAction): ExplorerState {
        return when (action) {
            ExplorerAction.LoadObjects -> {
                coroutineScope.launch {
                    loadObjects()
                }
                state
            }

            is ExplorerAction.SaveObject -> {
                coroutineScope.launch {
                    saveObject(action.key)
                }
                state
            }
        }
    }

    private suspend fun saveObject(key: String) {
        val defaultFileName = File(key).name
        val destination = FileDialog(null as Frame?, "Select file destination").apply {
            mode = FileDialog.SAVE
            file = defaultFileName
            isVisible = true
        }

        if (!destination.isValid) {
            return
        }

        val client = S3Client {
            credentialsProvider = StaticCredentialsProvider(
                Credentials(
                    credential.accessKey, credential.secretKey
                )
            )
            region = credential.region
            endpointUrl = Url.parse(credential.endpoint)
        }

        val file = File(Paths.get(destination.directory, destination.file).toString())

        client.getObject(GetObjectRequest {
            bucket = credential.bucket
            this.key = key
        }) {
            it.body?.let {
                file.writeBytes(it.toByteArray())
            }
        }
    }

    private suspend fun loadObjects() {
        _state.value = state.value.copy(objects = NetworkState.Loading())

        val client = S3Client {
            credentialsProvider = StaticCredentialsProvider(
                Credentials(
                    credential.accessKey, credential.secretKey
                )
            )
            region = credential.region
            endpointUrl = Url.parse(credential.endpoint)
        }
        try {
            val listObjectsResponse = client.listObjects(ListObjectsRequest {
                bucket = credential.bucket
            })
            _state.value = state.value.copy(objects = NetworkState.Loaded(listObjectsResponse.contents ?: listOf()))
        } catch (exception: Exception) {
            _state.value = state.value.copy(objects = NetworkState.Error(exception))
        }
    }
}