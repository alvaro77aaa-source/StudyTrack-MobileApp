package com.example.studytrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytrack.model.Curso
import com.example.studytrack.model.Proyecto
import com.example.studytrack.utils.FirebaseErrorHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoRegistrarProyecto(
    proyectoEditar: Proyecto? = null,
    cursoPreseleccionado: Curso? = null,
    listaCursos: List<Curso> = emptyList(),
    onCerrar: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    var titulo by remember { mutableStateOf(proyectoEditar?.titulo.orEmpty()) }
    var descripcion by remember { mutableStateOf(proyectoEditar?.descripcion.orEmpty()) }
    var fechaEntrega by remember { mutableStateOf(proyectoEditar?.fechaEntrega.orEmpty()) }
    var prioridad by remember { mutableStateOf(proyectoEditar?.prioridad ?: "Media") }
    var estado by remember { mutableStateOf(proyectoEditar?.estado ?: "Pendiente") }

    var cursoSeleccionado by remember {
        mutableStateOf(
            listaCursos.find { it.id == proyectoEditar?.idCurso }
                ?: cursoPreseleccionado
                ?: listaCursos.firstOrNull()
        )
    }
    var menuCursoExpandido by remember { mutableStateOf(false) }

    var mensajeError by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val esEdicion = proyectoEditar != null

    val prioridades = listOf("Alta", "Media", "Baja")
    val estados = listOf("Pendiente", "En Proceso", "Entregado")

    AlertDialog(
        onDismissRequest = { if (!guardando) onCerrar() },
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (esEdicion) "Editar Proyecto" else "Registrar Entregable",
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
                    value = titulo,
                    onValueChange = { titulo = it; mensajeError = "" },
                    label = { Text("Título del Proyecto / Entregable *") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = menuCursoExpandido,
                    onExpandedChange = { menuCursoExpandido = !menuCursoExpandido }
                ) {
                    OutlinedTextField(
                        value = cursoSeleccionado?.nombre ?: "Seleccionar Curso",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Curso Asociado *") },
                        leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCursoExpandido) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = menuCursoExpandido,
                        onDismissRequest = { menuCursoExpandido = false }
                    ) {
                        if (listaCursos.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay cursos disponibles") },
                                onClick = { menuCursoExpandido = false }
                            )
                        } else {
                            listaCursos.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.nombre} (${c.codigo})") },
                                    onClick = {
                                        cursoSeleccionado = c
                                        menuCursoExpandido = false
                                        mensajeError = ""
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = fechaEntrega,
                    onValueChange = { fechaEntrega = it },
                    label = { Text("Fecha de Entrega (Ej. 25/11/2026)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column {
                    Text("Prioridad", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prioridades.forEach { p ->
                            FilterChip(
                                selected = prioridad == p,
                                onClick = { prioridad = p },
                                label = { Text(p) },
                                leadingIcon = {
                                    if (prioridad == p) Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Column {
                    Text("Estado", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        estados.forEach { e ->
                            FilterChip(
                                selected = estado == e,
                                onClick = { estado = e },
                                label = { Text(e, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción / Instrucciones") },
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
                    if (titulo.isBlank()) {
                        mensajeError = "Por favor, ingresa el título del proyecto."
                        return@Button
                    }
                    if (cursoSeleccionado == null) {
                        mensajeError = "Por favor, selecciona un curso asociado."
                        return@Button
                    }

                    guardando = true
                    mensajeError = ""

                    val docRef = if (esEdicion && proyectoEditar != null && proyectoEditar.id.isNotBlank()) {
                        db.collection("proyectos").document(proyectoEditar.id)
                    } else {
                        db.collection("proyectos").document()
                    }

                    val proyectoParaGuardar = Proyecto(
                        id = docRef.id,
                        titulo = titulo.trim(),
                        descripcion = descripcion.trim(),
                        idUsuario = uid,
                        idCurso = cursoSeleccionado!!.id,
                        nombreCurso = cursoSeleccionado!!.nombre,
                        fechaEntrega = fechaEntrega.trim(),
                        estado = estado,
                        prioridad = prioridad,
                        fechaCreacion = proyectoEditar?.fechaCreacion ?: System.currentTimeMillis()
                    )

                    docRef.set(proyectoParaGuardar)
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
                    Text(if (esEdicion) "Actualizar" else "Registrar Proyecto")
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