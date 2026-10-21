package com.example.studytrack.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Usuario(
    val uid: String = "",
    val nombreCompleto: String = "",
    val correo: String = "",
    val carrera: String = "",
    val telefono: String = "",
    val fotoUrl: String = "",
    val fechaRegistro: Long = System.currentTimeMillis()
)