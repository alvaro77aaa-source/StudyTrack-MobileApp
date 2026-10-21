package com.example.studytrack.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogoConfirmacion(
    titulo: String = "Confirmar eliminación",
    mensaje: String = "¿Estás seguro de que deseas eliminar este elemento? Esta acción no se puede deshacer.",
    textoConfirmar: String = "Eliminar",
    textoCancelar: String = "Cancelar",
    cargando: Boolean = false,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!cargando) onCerrar() },
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = mensaje,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !cargando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(textoConfirmar)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCerrar,
                enabled = !cargando
            ) {
                Text(textoCancelar)
            }
        }
    )
}