package com.example.studytrack.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytrack.model.Curso
import com.example.studytrack.model.Proyecto
import com.example.studytrack.ui.components.DialogoConfirmacion
import com.example.studytrack.ui.components.DialogoProgramarNotificacion
import com.example.studytrack.ui.components.DialogoRegistrarProyecto
import com.example.studytrack.ui.components.StudyTrackHeader
import com.example.studytrack.ui.theme.HeaderSlateBorder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaProyectos(
    onNavegarAPerfil: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid.orEmpty()
    val nombreUsuario = auth.currentUser?.displayName

    var listaCursos by remember { mutableStateOf<List<Curso>>(emptyList()) }
    var listaProyectos by remember { mutableStateOf<List<Proyecto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    var filtroEstado by remember { mutableStateOf("Todos") }
    val filtros = listOf("Todos", "Pendientes", "En Proceso", "Entregados")

    var proyectoAEditar by remember { mutableStateOf<Proyecto?>(null) }
    var proyectoAEliminar by remember { mutableStateOf<Proyecto?>(null) }
    var proyectoAProgramarNotificacion by remember { mutableStateOf<Proyecto?>(null) }

    var mostrarDialogoNuevoProyecto by remember { mutableStateOf(false) }
    var eliminandoProyecto by remember { mutableStateOf(false) }

    val formatoNotifChip = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            db.collection("cursos")
                .whereEqualTo("idUsuario", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        listaCursos = snapshot.toObjects(Curso::class.java)
                    }
                }

            db.collection("proyectos")
                .whereEqualTo("idUsuario", uid)
                .addSnapshotListener { snapshot, error ->
                    cargando = false
                    if (error == null && snapshot != null) {
                        listaProyectos = snapshot.toObjects(Proyecto::class.java)
                    }
                }
        } else {
            cargando = false
        }
    }

    val proyectosFiltrados = listaProyectos.filter { proj ->
        when (filtroEstado) {
            "Pendientes" -> proj.estado == "Pendiente"
            "En Proceso" -> proj.estado == "En Proceso"
            "Entregados" -> proj.estado == "Entregado"
            else -> true
        }
    }

    Scaffold(
        topBar = {
            StudyTrackHeader(
                subtitulo = "Proyectos y Entregables",
                nombreUsuario = nombreUsuario,
                onPerfilClick = onNavegarAPerfil
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoNuevoProyecto = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Proyecto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filtros) { f ->
                    FilterChip(
                        selected = filtroEstado == f,
                        onClick = { filtroEstado = f },
                        label = { Text(f, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (proyectosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay entregables registrados en la categoría '$filtroEstado'.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(proyectosFiltrados, key = { it.id }) { proj ->
                        val esCompletado = proj.estado == "Entregado"
                        val tieneNotificacionProgramada = proj.fechaProgramada > System.currentTimeMillis() && !proj.enviada

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HeaderSlateBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val nuevoEstado = if (esCompletado) "Pendiente" else "Entregado"
                                                db.collection("proyectos").document(proj.id)
                                                    .update("estado", nuevoEstado)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (esCompletado) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = "Cambiar Estado",
                                                tint = if (esCompletado) Color(0xFF10B981) else Color.Gray
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = proj.titulo,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (esCompletado) Color.Gray else MaterialTheme.colorScheme.onSurface
                                            )

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = proj.nombreCurso,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }

                                                if (proj.fechaEntrega.isNotBlank()) {
                                                    Text(
                                                        text = "• ${proj.fechaEntrega}",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Surface(
                                        color = when (proj.prioridad) {
                                            "Alta" -> Color(0xFFFEE2E2)
                                            "Media" -> Color(0xFFFEF3C7)
                                            else -> Color(0xFFE0F2FE)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = proj.prioridad,
                                            color = when (proj.prioridad) {
                                                "Alta" -> Color(0xFFDC2626)
                                                "Media" -> Color(0xFFD97706)
                                                else -> Color(0xFF0284C7)
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (proj.descripcion.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = proj.descripcion,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        maxLines = 2
                                    )
                                }

                                // Scheduled Alarm Badge Chip
                                if (tieneNotificacionProgramada) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable { proyectoAProgramarNotificacion = proj }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Alarm,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Alarma: ${formatoNotifChip.format(Date(proj.fechaProgramada))}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (esCompletado) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = proj.estado,
                                            color = if (esCompletado) Color(0xFF047857) else Color(0xFFB45309),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Open Date/Time Notification Picker Dialog
                                        IconButton(
                                            onClick = { proyectoAProgramarNotificacion = proj },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = "Programar Notificación Fecha y Hora",
                                                tint = if (tieneNotificacionProgramada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { proyectoAEditar = proj },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar Proyecto",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { proyectoAEliminar = proj },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar Proyecto",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (proyectoAProgramarNotificacion != null) {
        DialogoProgramarNotificacion(
            proyecto = proyectoAProgramarNotificacion!!,
            onCerrar = { proyectoAProgramarNotificacion = null }
        )
    }

    if (mostrarDialogoNuevoProyecto) {
        DialogoRegistrarProyecto(
            listaCursos = listaCursos,
            onCerrar = { mostrarDialogoNuevoProyecto = false },
            onGuardadoExitoso = { mostrarDialogoNuevoProyecto = false }
        )
    }

    if (proyectoAEditar != null) {
        DialogoRegistrarProyecto(
            proyectoEditar = proyectoAEditar,
            listaCursos = listaCursos,
            onCerrar = { proyectoAEditar = null },
            onGuardadoExitoso = { proyectoAEditar = null }
        )
    }

    if (proyectoAEliminar != null) {
        DialogoConfirmacion(
            titulo = "Eliminar Entregable",
            mensaje = "¿Estás seguro de que deseas eliminar el proyecto '${proyectoAEliminar!!.titulo}'?",
            cargando = eliminandoProyecto,
            onConfirmar = {
                eliminandoProyecto = true
                db.collection("proyectos").document(proyectoAEliminar!!.id).delete().addOnCompleteListener {
                    eliminandoProyecto = false
                    proyectoAEliminar = null
                }
            },
            onCerrar = { proyectoAEliminar = null }
        )
    }
}