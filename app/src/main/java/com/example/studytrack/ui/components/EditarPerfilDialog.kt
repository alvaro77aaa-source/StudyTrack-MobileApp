package com.example.studytrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytrack.model.Usuario
import com.example.studytrack.utils.FirebaseErrorHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun DialogoEditarPerfil(
    usuarioActual: Usuario? = null,
    onCerrar: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var nombreCompleto by remember {
        mutableStateOf(usuarioActual?.nombreCompleto ?: auth.currentUser?.displayName.orEmpty())
    }
    var carrera by remember { mutableStateOf(usuarioActual?.carrera.orEmpty()) }
    var telefono by remember { mutableStateOf(usuarioActual?.telefono.orEmpty()) }

    var mensajeError by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!guardando) onCerrar() },
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Editar Perfil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (mensajeError.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = mensajeError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = nombreCompleto,
                    onValueChange = { nombreCompleto = it; mensajeError = "" },
                    label = { Text("Nombre Completo *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = carrera,
                    onValueChange = { carrera = it },
                    label = { Text("Carrera / Programa de Estudio") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono de Contacto") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val user = auth.currentUser
                    if (user == null) {
                        mensajeError = "No se encontró la sesión activa del usuario."
                        return@Button
                    }
                    if (nombreCompleto.isBlank()) {
                        mensajeError = "Por favor, ingresa tu nombre completo."
                        return@Button
                    }

                    guardando = true
                    mensajeError = ""

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombreCompleto.trim())
                        .build()

                    user.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                        if (!profileTask.isSuccessful) {
                            guardando = false
                            mensajeError = FirebaseErrorHandler.obtenerMensajeError(profileTask.exception)
                            return@addOnCompleteListener
                        }

                        val usuarioData = mapOf(
                            "uid" to user.uid,
                            "nombreCompleto" to nombreCompleto.trim(),
                            "correo" to (user.email ?: ""),
                            "carrera" to carrera.trim(),
                            "telefono" to telefono.trim(),
                            "fechaRegistro" to (usuarioActual?.fechaRegistro ?: System.currentTimeMillis())
                        )

                        db.collection("usuarios").document(user.uid)
                            .set(usuarioData)
                            .addOnSuccessListener {
                                guardando = false
                                onGuardadoExitoso()
                            }
                            .addOnFailureListener { e ->
                                guardando = false
                                mensajeError = FirebaseErrorHandler.obtenerMensajeError(e)
                            }
                    }
                },
                enabled = !guardando,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Text("Guardar Cambios")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCerrar,
                enabled = !guardando
            ) {
                Text("Cancelar")
            }
        }
    )
}