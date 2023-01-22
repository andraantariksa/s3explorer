package com.gadostudio.common.ui.signin

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ListObjectsRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
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

object SignInSideEffect

data class SignInState(
    val accessKey: String = "",
    val secretKey: String = "",
    val region: String = "",
    val endpoint: String = "",
    val bucket: String = "",
    val signInState: NetworkState<Unit>? = null
) {
    fun toCredential(): Credential = Credential(
        accessKey = accessKey, secretKey = secretKey, region = region, endpoint = endpoint, bucket = bucket
    )
}

sealed class SignInAction {
    class ChangeAccessKey(val value: String) : SignInAction()
    class ChangeSecretKey(val value: String) : SignInAction()
    class ChangeRegion(val value: String) : SignInAction()
    class ChangeEndpoint(val value: String) : SignInAction()
    class ChangeBucket(val value: String) : SignInAction()
    object SignIn : SignInAction()
}

class SignInScreenModel : ScreenModel {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    private val _sideEffect = MutableSharedFlow<SignInSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    fun doAction(action: SignInAction) {
        _state.value = reducer(state.value, action)
    }

    private fun reducer(state: SignInState, action: SignInAction): SignInState {
        return when (action) {
            is SignInAction.ChangeAccessKey -> state.copy(
                accessKey = action.value
            )

            is SignInAction.ChangeBucket -> state.copy(
                bucket = action.value
            )

            is SignInAction.ChangeEndpoint -> state.copy(
                endpoint = action.value
            )

            is SignInAction.ChangeRegion -> state.copy(
                region = action.value
            )

            is SignInAction.ChangeSecretKey -> state.copy(
                secretKey = action.value
            )

            SignInAction.SignIn -> {
                coroutineScope.launch {
                    signIn(state.toCredential())
                }
                state
            }
        }
    }

    private suspend fun signIn(credential: Credential) {
        _state.value = state.value.copy(signInState = NetworkState.Loading())

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
            client.listObjects(ListObjectsRequest {
                bucket = credential.bucket
                maxKeys = 1
            })
            _state.value = state.value.copy(signInState = NetworkState.Loaded(Unit))
        } catch (exception: Exception) {
            _state.value = state.value.copy(signInState = NetworkState.Error(exception))
        }
    }
}