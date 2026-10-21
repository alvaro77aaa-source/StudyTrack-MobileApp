package com.example.studytrack.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException

object FirebaseErrorHandler {

    fun obtenerMensajeError(exception: Throwable?): String {
        if (exception == null) return "Ocurrió un error inesperado. Inténtalo de nuevo."

        return when (exception) {
            is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil. Debe tener al menos 6 caracteres."
            is FirebaseAuthInvalidCredentialsException -> "El correo o la contraseña son incorrectos."
            is FirebaseAuthInvalidUserException -> "No existe ninguna cuenta registrada con este correo electrónico."
            is FirebaseAuthUserCollisionException -> "El correo ya está registrado en el sistema."
            is FirebaseAuthActionCodeException -> "El código de verificación es inválido o ha expirado."
            is FirebaseNetworkException -> "Error de conexión con el servidor. Inténtalo de nuevo."
            is FirebaseFirestoreException -> when (exception.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "No tienes permisos para realizar esta operación."
                FirebaseFirestoreException.Code.UNAVAILABLE -> "El servicio no está disponible en este momento."
                else -> "Error de conexión con la base de datos."
            }
            else -> {
                val msg = exception.message.orEmpty()
                when {
                    msg.contains("badly formatted", ignoreCase = true) ->
                        "El formato del correo electrónico es incorrecto."
                    msg.contains("network error", ignoreCase = true) ->
                        "Error de conexión con el servidor. Inténtalo de nuevo."
                    msg.contains("password", ignoreCase = true) && msg.contains("invalid", ignoreCase = true) ->
                        "El correo o la contraseña son incorrectos."
                    msg.contains("blocked all requests", ignoreCase = true) ->
                        "Demasiados intentos fallidos. Inténtalo más tarde."
                    else -> exception.localizedMessage ?: "Ocurrió un error inesperado."
                }
            }
        }
    }
}