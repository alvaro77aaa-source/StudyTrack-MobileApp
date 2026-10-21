package com.example.studytrack.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Proyecto(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val idUsuario: String = "",
    val idCurso: String = "",
    val nombreCurso: String = "",
    val fechaEntrega: String = "",
    val estado: String = "Pendiente", // "Pendiente", "En Proceso", "Entregado"
    val prioridad: String = "Media",  // "Alta", "Media", "Baja"
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaProgramada: Long = 0L,   // Timestamp en milisegundos para la alarma local exacta
    val enviada: Boolean = false       // Estado de envio de la notificacion en Firestore
)