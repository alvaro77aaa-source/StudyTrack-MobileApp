package com.example.studytrack

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

// Pantalla  Iniciar Sesión y Registrarsee
@Composable
fun PantallaAutenticacion(onLoginExitoso: () -> Unit) {
    var esModoLogin by remember { mutableStateOf(true) }

    var nombreCompleto by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var repetirClave by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "STUDYTRACK",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Gestión Académica",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (esModoLogin) "Iniciar Sesión" else "Crear Cuenta",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // El campo Nombre completo solo se muestra si es MODO REGISTRO
        if (!esModoLogin) {
            OutlinedTextField(
                value = nombreCompleto,
                onValueChange = { nombreCompleto = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // El campo Repetir contraseña solo se muestra si es MODO REGISTRO
        if (!esModoLogin) {
            OutlinedTextField(
                value = repetirClave,
                onValueChange = { repetirClave = it },
                label = { Text("Repetir contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (mensajeError.isNotEmpty()) {
            Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (correo.isBlank() || clave.isBlank() || (!esModoLogin && (nombreCompleto.isBlank() || repetirClave.isBlank()))) {
                    mensajeError = "Por favor, completa todos los campos"
                    return@Button
                }
                if (!esModoLogin && clave != repetirClave) {
                    mensajeError = "Las contraseñas no coinciden"
                    return@Button
                }
                cargando = true
                mensajeError = ""

                val auth = FirebaseAuth.getInstance()

                if (esModoLogin) {
                    // INICIAR SESIÓN CON USUARIO EXISTENTE
                    auth.signInWithEmailAndPassword(correo.trim(), clave)
                        .addOnCompleteListener { task ->
                            cargando = false
                            if (task.isSuccessful) {
                                onLoginExitoso()
                            } else {
                                mensajeError = task.exception?.localizedMessage ?: "Error al iniciar sesión"
                            }
                        }
                } else {
                    // REGISTRAR NUEVO USUARIO
                    auth.createUserWithEmailAndPassword(correo.trim(), clave)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val updates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombreCompleto.trim())
                                    .build()

                                user?.updateProfile(updates)?.addOnCompleteListener {
                                    cargando = false
                                    onLoginExitoso()
                                }
                            } else {
                                cargando = false
                                mensajeError = task.exception?.localizedMessage ?: "Error al registrarse"
                            }
                        }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (cargando) "Procesando..."
                else if (esModoLogin) "Ingresar"
                else "Registrar e Ingresar"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón para cambiar entre Iniciar Sesión y Registrarse
        TextButton(
            onClick = {
                esModoLogin = !esModoLogin
                mensajeError = ""
                repetirClave = ""
            }
        ) {
            Text(
                if (esModoLogin) "¿No tienes cuenta? Regístrate aquí"
                else "¿Ya tienes cuenta? Inicia sesión aquí"
            )
        }
    }
}