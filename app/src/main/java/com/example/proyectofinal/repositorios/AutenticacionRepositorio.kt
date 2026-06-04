package com.example.proyectofinal.repositorios

import com.example.proyectofinal.modelos.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AutenticacionRepositorio {
    private val autenticacion: FirebaseAuth = FirebaseAuth.getInstance()//escanea que el correo y la contraseña son correctos
    private val baseDeDatos: FirebaseFirestore = FirebaseFirestore.getInstance()//guarda las credenciales del usuario

    suspend fun registrarUsuario(usuario: Usuario, contrasena: String): Result<Boolean> {
        return try {
            val resultado = autenticacion.createUserWithEmailAndPassword(usuario.correo, contrasena).await()//crea una llave para este usuario
            val uid = resultado.user?.uid ?: throw Exception("No se pudo obtener el ID del usuario")//con la llave crear un uid secreto
            
            val usuarioConId = usuario.copy(id = uid)//toma el número secreto y se lo asigna al usuario
            
            baseDeDatos.collection("usuarios")//guarda al usuario con ese uid y guarda las credenciales
                .document(uid)
                .set(usuarioConId)
                .await()
            
            Result.success(true)
        } catch (e: Exception) {//esto le dice que si algo falla solo deja un mensaje de error y sigue funcionando
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
