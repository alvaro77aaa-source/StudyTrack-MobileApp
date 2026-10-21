package com.example.studytrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytrack.model.Curso
import com.example.studytrack.model.Proyecto
import com.example.studytrack.ui.components.DialogoEditarCurso
import com.example.studytrack.ui.components.DialogoRegistrarProyecto
import com.example.studytrack.ui.components.StudyTrackHeader
import com.example.studytrack.ui.theme.HeaderSlateBorder
import com.example.studytrack.utils.Notificaciones
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun obtenerPrimerNombre(nombreCompleto: String?): String {
    if (nombreCompleto.isNullOrBlank()) return "Estudiante"
    return nombreCompleto.trim().split(" ").firstOrNull() ?: "Estudiante"
}

@Composable
fun PantallaInicio(
    onNavegarACursos: () -> Unit = {},
    onNavegarAProyectos: () -> Unit = {},
    onNavegarAPerfil: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid.orEmpty()

    val nombreUsuario = auth.currentUser?.displayName
    val primerNombre = obtenerPrimerNombre(nombreUsuario)

    var listaCursos by remember { mutableStateOf<List<Curso>>(emptyList()) }
    var listaProyectos by remember { mutableStateOf<List<Proyecto>>(emptyList()) }
    var cargandoDatos by remember { mutableStateOf(true) }

    var mostrarDialogoCurso by remember { mutableStateOf(false) }
    var mostrarDialogoProyecto by remember { mutableStateOf(false) }

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
                    cargandoDatos = false
                    if (error == null && snapshot != null) {
                        listaProyectos = snapshot.toObjects(Proyecto::class.java)
                    }
                }
        } else {
            cargandoDatos = false
        }
    }

    val totalCursos = listaCursos.size
    val totalProyectos = listaProyectos.size
    val proyectosEntregados = listaProyectos.count { it.estado == "Entregado" }
    val proyectosPendientes = totalProyectos - proyectosEntregados
    val porcentajeCompletado = if (totalProyectos > 0) {
        ((proyectosEntregados.toFloat() / totalProyectos.toFloat()) * 100).toInt()
    } else {
        100
    }

    val proximosProyectos = listaProyectos.filter { it.estado != "Entregado" }.take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StudyTrackHeader(
            nombreUsuario = nombreUsuario,
            onPerfilClick = onNavegarAPerfil
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Hola, $primerNombre! 👋",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Aquí tienes un resumen de tu avance académico hoy.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RENDIMIENTO ACADÉMICO",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$proyectosEntregados / $totalProyectos entregas",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$porcentajeCompletado%",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Completado",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { porcentajeCompletado / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CardMetrica(
                    titulo = "Cursos",
                    valor = totalCursos.toString(),
                    subtitulo = "Inscritos",
                    icono = Icons.Default.Book,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onNavegarACursos,
                    modifier = Modifier.weight(1f)
                )

                CardMetrica(
                    titulo = "Pendientes",
                    valor = proyectosPendientes.toString(),
                    subtitulo = "Por entregar",
                    icono = Icons.Default.PendingActions,
                    color = if (proyectosPendientes > 0) Color(0xFFF59E0B) else Color(0xFF10B981),
                    onClick = onNavegarAProyectos,
                    modifier = Modifier.weight(1f)
                )

                CardMetrica(
                    titulo = "Entregados",
                    valor = proyectosEntregados.toString(),
                    subtitulo = "Completados",
                    icono = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981),
                    onClick = onNavegarAProyectos,
                    modifier = Modifier.weight(1f)
                )
            }

            Column {
                Text(
                    text = "ACCIONES RÁPIDAS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { mostrarDialogoCurso = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Curso", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { mostrarDialogoProyecto = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Proyecto", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            Notificaciones.enviarRecordatorio(
                                context = context,
                                titulo = "🔔 Recordatorio de Estudio - StudyTrack",
                                mensaje = "Tienes $proyectosPendientes proyectos pendientes por entregar esta semana. ¡Sigue adelante!"
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Notificar", fontSize = 11.sp)
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRÓXIMAS ENTREGAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    TextButton(onClick = onNavegarAProyectos) {
                        Text("Ver todos", fontSize = 12.sp)
                    }
                }

                if (cargandoDatos) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (proximosProyectos.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HeaderSlateBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "¡Estás al día! No tienes entregables pendientes.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        proximosProyectos.forEach { proj ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavegarAProyectos() },
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, HeaderSlateBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = proj.titulo,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = proj.nombreCurso,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
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
                                            text = proj.fechaEntrega.ifBlank { proj.prioridad },
                                            color = when (proj.prioridad) {
                                                "Alta" -> Color(0xFFDC2626)
                                                "Media" -> Color(0xFFD97706)
                                                else -> Color(0xFF0284C7)
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (mostrarDialogoCurso) {
        DialogoEditarCurso(
            onCerrar = { mostrarDialogoCurso = false },
            onGuardadoExitoso = { mostrarDialogoCurso = false }
        )
    }

    if (mostrarDialogoProyecto) {
        DialogoRegistrarProyecto(
            listaCursos = listaCursos,
            onCerrar = { mostrarDialogoProyecto = false },
            onGuardadoExitoso = { mostrarDialogoProyecto = false }
        )
    }
}

@Composable
fun CardMetrica(
    titulo: String,
    valor: String,
    subtitulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HeaderSlateBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = valor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitulo,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}