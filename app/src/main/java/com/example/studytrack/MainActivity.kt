package com.example.studytrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.studytrack.ui.theme.StudyTrackTheme
import com.example.studytrack.utils.Notificaciones
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize notification channel for reminders (RF17)
        Notificaciones.crearCanalNotificaciones(this)

        setContent {
            StudyTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppPrincipal()
                }
            }
        }
    }
}

@Composable
fun AppPrincipal() {
    val auth = FirebaseAuth.getInstance()
    var usuarioLogueado by remember { mutableStateOf(auth.currentUser != null) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            usuarioLogueado = firebaseAuth.currentUser != null
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (!usuarioLogueado) {
        PantallaAutenticacion(onLoginExitoso = { usuarioLogueado = true })
    } else {
        MenuPrincipal(onCerrarSesion = {
            auth.signOut()
            usuarioLogueado = false
        })
    }
}