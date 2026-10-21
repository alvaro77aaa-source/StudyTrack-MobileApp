package com.example.studytrack.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytrack.model.Proyecto
import com.example.studytrack.utils.Notificaciones
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DialogoProgramarNotificacion(
    proyecto: Proyecto,
    onCerrar: () -> Unit,
    onProgramadoExitoso: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    val calendarBase = remember {
        Calendar.getInstance().apply {
            if (proyecto.fechaProgramada > System.currentTimeMillis()) {
                timeInMillis = proyecto.fechaProgramada
            } else {
                add(Calendar.MINUTE, 5)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
    }

    var selectedYear by remember { mutableIntStateOf(calendarBase.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendarBase.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(calendarBase.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableIntStateOf(calendarBase.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendarBase.get(Calendar.MINUTE)) }

    var mensajeError by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    val localeEs = remember { Locale.forLanguageTag("es-ES") }
    val formatoFechaCompleta = remember { SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", localeEs) }
    val formatoHora = remember { SimpleDateFormat("HH:mm 'hs'", localeEs) }

    fun calcularMillisSeleccionados(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, selectedYear)
        cal.set(Calendar.MONTH, selectedMonth)
        cal.set(Calendar.DAY_OF_MONTH, selectedDay)
        cal.set(Calendar.HOUR_OF_DAY, selectedHour)
        cal.set(Calendar.MINUTE, selectedMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedYear = year
                selectedMonth = month
                selectedDay = dayOfMonth
                mensajeError = ""
            },
            selectedYear,
            selectedMonth,
            selectedDay
        ).apply {
            val inicioDeHoy = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            datePicker.minDate = inicioDeHoy
        }
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                mensajeError = ""
            },
            selectedHour,
            selectedMinute,
            true
        )
    }

    val millisActuales = calcularMillisSeleccionados()
    val textoFechaFormateada = formatoFechaCompleta.format(millisActuales)
    val textoHoraFormateada = formatoHora.format(millisActuales)

    val notificacionProgramadaPrevia = proyecto.fechaProgramada > System.currentTimeMillis() && !proyecto.enviada

    AlertDialog(
        onDismissRequest = { if (!guardando) onCerrar() },
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Programar Recordatorio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Selecciona la fecha y hora exacta en la que deseas recibir la notificación local para '${proyecto.titulo}'.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

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

                OutlinedCard(
                    onClick = { datePickerDialog.show() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text("Fecha de Notificación", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = textoFechaFormateada.replaceFirstChar { it.uppercase() },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                OutlinedCard(
                    onClick = { timePickerDialog.show() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text("Hora de Notificación", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = textoHoraFormateada,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (notificacionProgramadaPrevia) {
                    TextButton(
                        onClick = {
                            val notifId = proyecto.id.hashCode()
                            Notificaciones.cancelarNotificacionLocal(context, notifId)
                            db.collection("proyectos").document(proyecto.id)
                                .update("fechaProgramada", 0L, "enviada", false)
                            Toast.makeText(context, "Recordatorio cancelado", Toast.LENGTH_SHORT).show()
                            onCerrar()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.NotificationsOff, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancelar recordatorio existente", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val programMillis = calcularMillisSeleccionados()
                    val ahoraMillis = System.currentTimeMillis()

                    // Permite seleccionar horas con margen de tolerancia (evita bloqueo de segundos transcurridos)
                    if (programMillis < (ahoraMillis - 60_000)) {
                        mensajeError = "Selecciona una hora futura para el día de hoy"
                        return@Button
                    }

                    guardando = true
                    mensajeError = ""

                    val notifId = proyecto.id.hashCode()
                    val titulo = "🔔 Recordatorio: ${proyecto.titulo}"
                    val mensaje = "Proyecto de ${proyecto.nombreCurso}. ¡Es momento de revisarlo o entregarlo!"

                    Notificaciones.programarNotificacionLocal(
                        context = context,
                        titulo = titulo,
                        mensaje = mensaje,
                        fechaHoraMillis = programMillis,
                        notificacionId = notifId,
                        proyectoId = proyecto.id
                    )

                    db.collection("proyectos").document(proyecto.id)
                        .update("fechaProgramada", programMillis, "enviada", false)
                        .addOnSuccessListener {
                            guardando = false
                            Toast.makeText(
                                context,
                                "Recordatorio programado para las ${String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)} hs",
                                Toast.LENGTH_LONG
                            ).show()
                            onProgramadoExitoso()
                            onCerrar()
                        }
                        .addOnFailureListener {
                            guardando = false
                            mensajeError = "Error al actualizar en Firestore. Inténtalo de nuevo."
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
                    Text("Programando...")
                } else {
                    Text("Programar AlarmManager")
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