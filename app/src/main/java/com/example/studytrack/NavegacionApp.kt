package com.example.studytrack

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class Seccion { INICIO, CURSOS }

// Función auxiliar para obtener el primer nombre
fun obtenerPrimerNombre(nombreCompleto: String?): String {
    if (nombreCompleto.isNullOrBlank()) return "Estudiante"
    return nombreCompleto.trim().split(" ").firstOrNull() ?: "Estudiante"
}

// Pantalla principal tras ingresar (Con barra de navegación inferior)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPrincipal(onCerrarSesion: () -> Unit) {
    var seccionActual by remember { mutableStateOf(Seccion.INICIO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "STUDYTRACK", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = if (seccionActual == Seccion.INICIO) "Inicio" else "Cursos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    TextButton(onClick = onCerrarSesion) {
                        Text("Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
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
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (seccionActual) {
                Seccion.INICIO -> PantallaInicio()
                Seccion.CURSOS -> PantallaCursos()
            }
        }
    }
}

// Pantalla de Inicio con Saludo Personalizado
@Composable
fun PantallaInicio() {
    val auth = FirebaseAuth.getInstance()
    val nombreUsuario = auth.currentUser?.displayName
    val primerNombre = obtenerPrimerNombre(nombreUsuario)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Hola, $primerNombre 👋", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3F51B5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "RENDIMIENTO ACADÉMICO", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "100%", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Completado del mes", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

// Pantalla de Cursos con Firestore
@Composable
fun PantallaCursos() {
    var mostrarFormulario by remember { mutableStateOf(false) }
    var cursoSeleccionado by remember { mutableStateOf<Curso?>(null) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var listaCursos by remember { mutableStateOf<List<Curso>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val idUsuario = auth.currentUser?.uid ?: return@LaunchedEffect
        db.collection("cursos")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener { snapshot, error ->
                cargando = false
                if (error == null && snapshot != null) {
                    listaCursos = snapshot.toObjects(Curso::class.java)
                }
            }
    }

    if (cursoSeleccionado != null) {
        PantallaDetalleCurso(curso = cursoSeleccionado!!, onVolver = { cursoSeleccionado = null })
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { mostrarFormulario = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Curso")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(text = "Mis Cursos Registrados", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (listaCursos.isEmpty()) {
                    Text("No hay cursos registrados. Toca el botón + para agregar uno.", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listaCursos) { curso ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { cursoSeleccionado = curso },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = curso.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(text = "Código: ${curso.codigo} | Créditos: ${curso.creditos}", fontSize = 14.sp)
                                    if (curso.profesor.isNotBlank()) {
                                        Text(text = "Profesor: ${curso.profesor}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        DialogoAgregarCurso(
            onCerrar = { mostrarFormulario = false },
            onGuardado = { mostrarFormulario = false }
        )
    }
}

// Pantalla Detalle de un Curso Seleccionado
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleCurso(curso: Curso, onVolver: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Detalle del Curso", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = curso.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = "Código: ${curso.codigo}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Créditos: ${curso.creditos}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            if (curso.profesor.isNotBlank()) {
                Text(text = "Profesor: ${curso.profesor}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = "Descripción", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (curso.descripcion.isNotBlank()) curso.descripcion else "Sin descripción.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
        }
    }
}

// Diálogo para Agregar Curso
@Composable
fun DialogoAgregarCurso(onCerrar: () -> Unit, onGuardado: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var creditos by remember { mutableStateOf("") }
    var profesor by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Registrar Nuevo Curso") },
        text = {
            Column {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del Curso") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("Código de Curso") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = creditos, onValueChange = { creditos = it }, label = { Text("Créditos") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = profesor, onValueChange = { profesor = it }, label = { Text("Profesor") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val idUsuario = auth.currentUser?.uid ?: return@Button
                    val creditosNum = creditos.toIntOrNull() ?: 0
                    if (nombre.isBlank() || codigo.isBlank()) return@Button

                    guardando = true
                    val docRef = db.collection("cursos").document()
                    val nuevoCurso = Curso(
                        id = docRef.id,
                        nombre = nombre,
                        codigo = codigo,
                        creditos = creditosNum,
                        profesor = profesor,
                        idUsuario = idUsuario,
                        descripcion = descripcion
                    )
                    docRef.set(nuevoCurso).addOnSuccessListener {
                        guardando = false
                        onGuardado()
                    }
                },
                enabled = !guardando
            ) {
                Text(if (guardando) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        }
    )
}