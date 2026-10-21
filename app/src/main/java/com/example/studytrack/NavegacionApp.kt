package com.example.studytrack

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studytrack.ui.PantallaCursos
import com.example.studytrack.ui.PantallaInicio
import com.example.studytrack.ui.PantallaPerfil
import com.example.studytrack.ui.PantallaProyectos

enum class Seccion { INICIO, CURSOS, PROYECTOS, PERFIL }

@Composable
fun MenuPrincipal(onCerrarSesion: () -> Unit) {
    var seccionActual by remember { mutableStateOf(Seccion.INICIO) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = seccionActual == Seccion.INICIO,
                    onClick = { seccionActual = Seccion.INICIO },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )

                NavigationBarItem(
                    selected = seccionActual == Seccion.CURSOS,
                    onClick = { seccionActual = Seccion.CURSOS },
                    icon = { Icon(Icons.Default.Book, contentDescription = "Cursos") },
                    label = { Text("Cursos") }
                )

                NavigationBarItem(
                    selected = seccionActual == Seccion.PROYECTOS,
                    onClick = { seccionActual = Seccion.PROYECTOS },
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Proyectos") },
                    label = { Text("Proyectos") }
                )

                NavigationBarItem(
                    selected = seccionActual == Seccion.PERFIL,
                    onClick = { seccionActual = Seccion.PERFIL },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (seccionActual) {
                Seccion.INICIO -> PantallaInicio(
                    onNavegarACursos = { seccionActual = Seccion.CURSOS },
                    onNavegarAProyectos = { seccionActual = Seccion.PROYECTOS },
                    onNavegarAPerfil = { seccionActual = Seccion.PERFIL }
                )
                Seccion.CURSOS -> PantallaCursos(
                    onNavegarAPerfil = { seccionActual = Seccion.PERFIL }
                )
                Seccion.PROYECTOS -> PantallaProyectos(
                    onNavegarAPerfil = { seccionActual = Seccion.PERFIL }
                )
                Seccion.PERFIL -> PantallaPerfil(
                    onCerrarSesion = onCerrarSesion
                )
            }
        }
    }
}