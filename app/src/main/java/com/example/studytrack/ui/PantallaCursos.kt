package com.example.studytrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytrack.model.Curso
import com.example.studytrack.model.Proyecto
import com.example.studytrack.ui.components.DialogoConfirmacion
import com.example.studytrack.ui.components.DialogoEditarCurso
import com.example.studytrack.ui.components.DialogoRegistrarProyecto
import com.example.studytrack.ui.components.StudyTrackHeader
import com.example.studytrack.ui.theme.HeaderSlateBorder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaCursos(
    onNavegarAPerfil: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid.orEmpty()
    val nombreUsuario = auth.currentUser?.displayName

    var listaCursos by remember { mutableStateOf<List<Curso>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var busqueda by remember { mutableStateOf("") }

    var cursoSeleccionado by remember { mutableStateOf<Curso?>(null) }
    var cursoAEditar by remember { mutableStateOf<Curso?>(null) }
    var cursoAEliminar by remember { mutableStateOf<Curso?>(null) }
    var mostrarFormularioNuevoCurso by remember { mutableStateOf(false) }
    var eliminandoCurso by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            db.collection("cursos")
                .whereEqualTo("idUsuario", uid)
                .addSnapshotListener { snapshot, error ->
                    cargando = false
                    if (error == null && snapshot != null) {
                        listaCursos = snapshot.toObjects(Curso::class.java)
                        if (cursoSeleccionado != null) {
                            cursoSeleccionado = listaCursos.find { it.id == cursoSeleccionado!!.id }
                        }
                    }
                }
        } else {
            cargando = false
        }
    }

    val cursosFiltrados = listaCursos.filter {
        it.nombre.contains(busqueda, ignoreCase = true) ||
                it.codigo.contains(busqueda, ignoreCase = true) ||
                it.profesor.contains(busqueda, ignoreCase = true)
    }

    if (cursoSeleccionado != null) {
        PantallaDetalleCurso(
            curso = cursoSeleccionado!!,
            todosLosCursos = listaCursos,
            onVolver = { cursoSeleccionado = null },
            onEditar = { cursoAEditar = cursoSeleccionado },
            onEliminar = { cursoAEliminar = cursoSeleccionado }
        )
    } else {
        Scaffold(
            topBar = {
                StudyTrackHeader(
                    subtitulo = "Mis Cursos Registrados",
                    nombreUsuario = nombreUsuario,
                    onPerfilClick = onNavegarAPerfil
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mostrarFormularioNuevoCurso = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Curso")
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

                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar por nombre, código o profesor...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (cargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (cursosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (busqueda.isNotBlank()) "No se encontraron cursos con '$busqueda'."
                                else "No tienes cursos registrados aún.",
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
                        items(cursosFiltrados, key = { it.id }) { curso ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { cursoSeleccionado = curso },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, HeaderSlateBorder),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Book,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = curso.nombre,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = curso.codigo,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "${curso.creditos} Créditos",
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            if (curso.profesor.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Prof: ${curso.profesor}",
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { cursoAEditar = curso }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar Curso",
                                            tint = MaterialTheme.colorScheme.primary
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

    if (mostrarFormularioNuevoCurso) {
        DialogoEditarCurso(
            onCerrar = { mostrarFormularioNuevoCurso = false },
            onGuardadoExitoso = { mostrarFormularioNuevoCurso = false }
        )
    }

    if (cursoAEditar != null) {
        DialogoEditarCurso(
            cursoEditar = cursoAEditar,
            onCerrar = { cursoAEditar = null },
            onGuardadoExitoso = { cursoAEditar = null }
        )
    }

    if (cursoAEliminar != null) {
        DialogoConfirmacion(
            titulo = "Eliminar Curso",
            mensaje = "¿Estás seguro de que deseas eliminar el curso '${cursoAEliminar!!.nombre}'? Se borrarán sus datos y entregables asociados.",
            cargando = eliminandoCurso,
            onConfirmar = {
                val cursoId = cursoAEliminar!!.id
                eliminandoCurso = true

                db.collection("cursos").document(cursoId).delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        db.collection("proyectos").whereEqualTo("idCurso", cursoId).get()
                            .addOnSuccessListener { projSnapshot ->
                                for (doc in projSnapshot.documents) {
                                    doc.reference.delete()
                                }
                                eliminandoCurso = false
                                cursoAEliminar = null
                                cursoSeleccionado = null
                            }
                            .addOnFailureListener {
                                eliminandoCurso = false
                                cursoAEliminar = null
                                cursoSeleccionado = null
                            }
                    } else {
                        eliminandoCurso = false
                        cursoAEliminar = null
                    }
                }
            },
            onCerrar = { cursoAEliminar = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleCurso(
    curso: Curso,
    todosLosCursos: List<Curso>,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var proyectosCurso by remember { mutableStateOf<List<Proyecto>>(emptyList()) }
    var cargandoProyectos by remember { mutableStateOf(true) }
    var mostrarNuevoProyecto by remember { mutableStateOf(false) }

    LaunchedEffect(curso.id) {
        db.collection("proyectos")
            .whereEqualTo("idCurso", curso.id)
            .addSnapshotListener { snapshot, error ->
                cargandoProyectos = false
                if (error == null && snapshot != null) {
                    proyectosCurso = snapshot.toObjects(Proyecto::class.java)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = curso.codigo, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { mostrarNuevoProyecto = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Entregable") },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = curso.nombre,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${curso.creditos} Créditos", fontSize = 13.sp)
                        }

                        if (curso.aula.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Room, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aula: ${curso.aula}", fontSize = 13.sp)
                            }
                        }
                    }

                    if (curso.profesor.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Profesor: ${curso.profesor}", fontSize = 13.sp)
                        }
                    }

                    if (curso.descripcion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = curso.descripcion,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ENTREGABLES Y PROYECTOS DEL CURSO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (cargandoProyectos) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (proyectosCurso.isEmpty()) {
                Text("No hay entregables registrados para este curso aún.", color = Color.Gray, fontSize = 13.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(proyectosCurso, key = { it.id }) { p ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HeaderSlateBorder),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(p.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Entrega: ${p.fechaEntrega.ifBlank { "Sin fecha" }} | ${p.estado}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarNuevoProyecto) {
        DialogoRegistrarProyecto(
            cursoPreseleccionado = curso,
            listaCursos = todosLosCursos,
            onCerrar = { mostrarNuevoProyecto = false },
            onGuardadoExitoso = { mostrarNuevoProyecto = false }
        )
    }
}