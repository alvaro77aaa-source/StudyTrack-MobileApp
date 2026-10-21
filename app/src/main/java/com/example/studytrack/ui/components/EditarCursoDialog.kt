package com.example.studytrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytrack.model.Curso
import com.example.studytrack.utils.FirebaseErrorHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun DialogoEditarCurso(
    cursoEditar: Curso? = null,
    onCerrar: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    var nombre by remember { mutableStateOf(cursoEditar?.nombre.orEmpty()) }
    var codigo by remember { mutableStateOf(cursoEditar?.codigo.orEmpty()) }
    var creditos by remember { mutableStateOf(cursoEditar?.creditos?.let { if (it > 0) it.toString() else "" }.orEmpty()) }
    var profesor by remember { mutableStateOf(cursoEditar?.profesor.orEmpty()) }
    var aula by remember { mutableStateOf(cursoEditar?.aula.orEmpty()) }
    var descripcion by remember { mutableStateOf(cursoEditar?.descripcion.orEmpty()) }

    var mensajeError by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val esEdicion = cursoEditar != null

    AlertDialog(
        onDismissRequest = { if (!guardando) onCerrar() },
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (esEdicion) "Editar Curso" else "Registrar Nuevo Curso",
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
                    value = nombre,
                    onValueChange = { nombre = it; mensajeError = "" },
                    label = { Text("Nombre del Curso *") },
                    leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it; mensajeError = "" },
                    label = { Text("Código de Curso *") },
                    leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = creditos,
                        onValueChange = { creditos = it; mensajeError = "" },
                        label = { Text("Créditos") },
                        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = aula,
                        onValueChange = { aula = it; mensajeError = "" },
                        label = { Text("Aula / Salón") },
                        leadingIcon = { Icon(Icons.Default.Room, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = profesor,
                    onValueChange = { profesor = it },
                    label = { Text("Profesor Encargado") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción / Notas") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uid = auth.currentUser?.uid
                    if (uid.isNullOrBlank()) {
                        mensajeError = "Debes iniciar sesión para realizar esta acción."
                        return@Button
                    }
                    if (nombre.isBlank() || codigo.isBlank()) {
                        mensajeError = "Por favor, completa todos los campos requeridos (*)."
                        return@Button
                    }

                    val creditosNum = creditos.trim().toIntOrNull() ?: 0
                    guardando = true
                    mensajeError = ""

                    val docRef = if (esEdicion && cursoEditar != null) {
                        db.collection("cursos").document(cursoEditar.id)
                    } else {
                        db.collection("cursos").document()
                    }

                    val cursoParaGuardar = Curso(
                        id = docRef.id,
                        nombre = nombre.trim(),
                        codigo = codigo.trim().uppercase(),
                        creditos = creditosNum,
                        profesor = profesor.trim(),
                        idUsuario = uid,
                        descripcion = descripcion.trim(),
                        aula = aula.trim(),
                        fechaCreacion = cursoEditar?.fechaCreacion ?: System.currentTimeMillis()
                    )

                    docRef.set(cursoParaGuardar)
                        .addOnSuccessListener {
                            guardando = false
                            onGuardadoExitoso()
                        }
                        .addOnFailureListener { e ->
                            guardando = false
                            mensajeError = FirebaseErrorHandler.obtenerMensajeError(e)
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
                    Text(if (esEdicion) "Actualizar" else "Guardar Curso")
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