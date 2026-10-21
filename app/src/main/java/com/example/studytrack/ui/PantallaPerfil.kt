package com.example.studytrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytrack.model.Usuario
import com.example.studytrack.ui.components.DialogoEditarPerfil
import com.example.studytrack.ui.components.StudyTrackHeader
import com.example.studytrack.ui.theme.HeaderSlateBorder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaPerfil(
    onCerrarSesion: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val uid = user?.uid.orEmpty()

    var usuarioState by remember { mutableStateOf<Usuario?>(null) }
    var totalCursos by remember { mutableIntStateOf(0) }
    var totalProyectos by remember { mutableIntStateOf(0) }

    var mostrarEditarPerfil by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            db.collection("usuarios").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        usuarioState = snapshot.toObject(Usuario::class.java)
                    }
                }

            db.collection("cursos").whereEqualTo("idUsuario", uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) totalCursos = snapshot.size()
                }

            db.collection("proyectos").whereEqualTo("idUsuario", uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) totalProyectos = snapshot.size()
                }
        }
    }

    val nombreMostrar = usuarioState?.nombreCompleto.takeIf { !it.isNullOrBlank() }
        ?: user?.displayName.takeIf { !it.isNullOrBlank() }
        ?: "Estudiante"

    val correoMostrar = user?.email.orEmpty()
    val carreraMostrar = usuarioState?.carrera.takeIf { !it.isNullOrBlank() } ?: "No especificada"
    val telefonoMostrar = usuarioState?.telefono.takeIf { !it.isNullOrBlank() } ?: "No especificado"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StudyTrackHeader(
            subtitulo = "Perfil del Estudiante",
            nombreUsuario = nombreMostrar
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, HeaderSlateBorder),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nombreMostrar.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = nombreMostrar,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = correoMostrar,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { mostrarEditarPerfil = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar Perfil")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("$totalCursos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Cursos", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Column {
                            Text("$totalProyectos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Entregables", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, HeaderSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INFORMACIÓN PERSONAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ElementoInformacion(
                        icono = Icons.Default.Person,
                        label = "Nombre Completo",
                        valor = nombreMostrar
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ElementoInformacion(
                        icono = Icons.Default.Email,
                        label = "Correo Electrónico",
                        valor = correoMostrar
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ElementoInformacion(
                        icono = Icons.Default.School,
                        label = "Carrera / Programa",
                        valor = carreraMostrar
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ElementoInformacion(
                        icono = Icons.Default.Phone,
                        label = "Teléfono de Contacto",
                        valor = telefonoMostrar
                    )
                }
            }

            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (mostrarEditarPerfil) {
        DialogoEditarPerfil(
            usuarioActual = usuarioState,
            onCerrar = { mostrarEditarPerfil = false },
            onGuardadoExitoso = { mostrarEditarPerfil = false }
        )
    }
}

@Composable
fun ElementoInformacion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    valor: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
            Text(text = valor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}