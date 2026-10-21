package com.example.studytrack.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "Recordatorio StudyTrack"
        val mensaje = intent.getStringExtra("mensaje") ?: "Tienes un proyecto o tarea programada."
        val notificacionId = intent.getIntExtra("notificacionId", (System.currentTimeMillis() % 10000).toInt())
        val proyectoId = intent.getStringExtra("proyectoId").orEmpty()

        // Build and send high priority heads-up notification with sound & vibration
        Notificaciones.enviarRecordatorio(
            context = context,
            titulo = titulo,
            mensaje = mensaje,
            notificacionId = notificacionId
        )

        // Sync Firestore status: enviada = true
        if (proyectoId.isNotBlank()) {
            FirebaseFirestore.getInstance()
                .collection("proyectos")
                .document(proyectoId)
                .update("enviada", true)
        }
    }
}