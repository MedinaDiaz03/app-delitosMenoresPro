package com.example.proyectofinal.repositorios

import com.example.proyectofinal.modelos.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AutenticacionRepositorio {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun asegurarUsuarioEnFirestore(): Usuario? {
        val user = auth.currentUser ?: return null
        val userRef = db.collection("usuarios").document(user.uid)

        return try {
            val document = userRef.get().await()
            if (!document.exists()) {
                val nuevoUsuario = Usuario(
                    uid = user.uid,
                    nombre = user.displayName ?: "Usuario",
                    email = user.email ?: "",
                    rol = "",
                    verificado = false
                )
                userRef.set(nuevoUsuario).await()
                nuevoUsuario
            } else {
                document.toObject(Usuario::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun actualizarRol(nuevoRol: String, verificado: Boolean = false): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
            val actualizaciones = mutableMapOf<String, Any>(
                "rol" to nuevoRol,
                "verificado" to verificado
            )
            db.collection("usuarios").document(uid).update(actualizaciones).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarTelefonoEmergencia(telefono: String): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
            db.collection("usuarios").document(uid)
                .update("telefonoEmergencia", telefono)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarUsuario(usuario: Usuario, contrasena: String): Result<Boolean> {
        return try {
            val resultado = auth.createUserWithEmailAndPassword(usuario.email, contrasena).await()
            val uid = resultado.user?.uid ?: throw Exception("No se pudo obtener el ID del usuario")
            val usuarioConId = usuario.copy(uid = uid)
            db.collection("usuarios").document(uid).set(usuarioConId).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(correo, contrasena).await()
            asegurarUsuarioEnFirestore()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerDatosUsuarioActual(): Usuario? {
        return try {
            val userFirebase = auth.currentUser ?: return null
            val uid = userFirebase.uid
            val documento = db.collection("usuarios").document(uid).get().await()
            if (documento.exists()) {
                documento.toObject(Usuario::class.java)
            } else {
                val nuevoUsuario = Usuario(
                    uid = uid,
                    nombre = userFirebase.displayName ?: "Usuario",
                    email = userFirebase.email ?: "",
                    rol = "ciudadano",
                    verificado = false
                )
                db.collection("usuarios").document(uid).set(nuevoUsuario).await()
                nuevoUsuario
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun recuperarContrasena(correo: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(correo).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun obtenerUsuarioActual() = auth.currentUser

    fun cerrarSesion() {
        auth.signOut()
    }
}