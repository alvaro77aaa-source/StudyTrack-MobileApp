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
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

// Controla si se muestra la pantalla de Login o la Navegación Principal
@Composable
fun AppPrincipal() {
    val auth = FirebaseAuth.getInstance()
    var usuarioLogueado by remember { mutableStateOf(auth.currentUser != null) }

    if (!usuarioLogueado) {
        // Llama a la pantalla de Autenticación definida en Login.kt
        PantallaAutenticacion(onLoginExitoso = { usuarioLogueado = true })
    } else {
        // Llama al menú principal definido en NavegacionApp.kt
        MenuPrincipal(onCerrarSesion = {
            auth.signOut()
            usuarioLogueado = false
        })
    }
}