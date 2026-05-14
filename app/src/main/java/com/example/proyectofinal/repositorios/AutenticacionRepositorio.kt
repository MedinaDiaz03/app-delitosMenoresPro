package com.example.proyectofinal.repositorios

import com.example.proyectofinal.modelos.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AutenticacionRepositorio {
    private val autenticacion: FirebaseAuth = FirebaseAuth.getInstance()
    private val baseDeDatos: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun registrarUsuario(usuario: Usuario, contrasena: String): Result<Boolean> {
        return try {
            val resultado = autenticacion.createUserWithEmailAndPassword(usuario.correo, contrasena).await()
            val uid = resultado.user?.uid ?: throw Exception("No se pudo obtener el ID del usuario")
            
            val usuarioConId = usuario.copy(id = uid)
            
            baseDeDatos.collection("usuarios")
                .document(uid)
                .set(usuarioConId)
                .await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Boolean> {
        return try {
            autenticacion.signInWithEmailAndPassword(correo, contrasena).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerDatosUsuarioActual(): Usuario? {
        return try {
            val uid = autenticacion.currentUser?.uid ?: return null
            val documento = baseDeDatos.collection("usuarios").document(uid).get().await()
            if (documento.exists()) {
                documento.toObject(Usuario::class.java)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun recuperarContrasena(correo: String): Result<Boolean> {
        return try {
            autenticacion.sendPasswordResetEmail(correo).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun obtenerUsuarioActual() = autenticacion.currentUser

    fun cerrarSesion() {
        autenticacion.signOut()
    }
}
