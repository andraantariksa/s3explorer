package com.gadostudio.common

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import com.gadostudio.common.ui.signin.SignInScreen
import java.awt.FileDialog
import java.awt.Frame

@Composable
fun App() {
    Navigator(SignInScreen())
}
