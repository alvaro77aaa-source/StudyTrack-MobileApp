package com.example.studytrack.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Curso(
    val id: String = "",
    val nombre: String = "",
    val codigo: String = "",
    val creditos: Int = 0,
    val profesor: String = "",
    val idUsuario: String = "",
    val descripcion: String = "",
    val aula: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
)