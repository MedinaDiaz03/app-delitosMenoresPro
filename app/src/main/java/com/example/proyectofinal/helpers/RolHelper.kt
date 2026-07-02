package com.example.proyectofinal.helpers

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RolHelper {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun setRolCiudadano(context: Context, onComplete: () -> Unit) {
        val user = auth.currentUser ?: return

        db.collection("usuarios")
            .document(user.uid)
            .update("rol", "ciudadano")
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener {
                val nuevoUsuario = mapOf(
                    "uid" to user.uid,
                    "nombre" to (user.displayName ?: "Usuario Google"),
                    "email" to (user.email ?: ""),
                    "rol" to "ciudadano"
                )
                db.collection("usuarios").document(user.uid).set(nuevoUsuario)
                    .addOnSuccessListener { onComplete() }
            }
    }

    fun validarCodigoPolicia(context: Context, codigo: String, onComplete: () -> Unit = {}) {
        val user = auth.currentUser ?: return

        if (codigo == "POLICIA123") {
            val updates = mapOf(
                "rol" to "policia",
                "verificado" to true
            )
            db.collection("usuarios")
                .document(user.uid)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Ahora eres policía ✅", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
                .addOnFailureListener {
                    val nuevoUsuario = mapOf(
                        "uid" to user.uid,
                        "nombre" to (user.displayName ?: "Usuario Google"),
                        "email" to (user.email ?: ""),
                        "rol" to "policia",
                        "verificado" to true
                    )
                    db.collection("usuarios").document(user.uid).set(nuevoUsuario)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Ahora eres policía ✅", Toast.LENGTH_SHORT).show()
                            onComplete()
                        }
                }
        } else {
            Toast.makeText(context, "Código incorrecto ❌", Toast.LENGTH_SHORT).show()
        }
    }

    fun obtenerRol(onResult: (String) -> Unit) {
        val user = auth.currentUser ?: return

        db.collection("usuarios")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val rol = doc.getString("rol") ?: "ciudadano"
                onResult(rol)
            }
    }
}