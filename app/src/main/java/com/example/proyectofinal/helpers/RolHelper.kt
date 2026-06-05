package com.example.proyectofinal.helpers

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RolHelper {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun validarCodigoPolicia(context: Context, codigo: String) {
        val user = auth.currentUser ?: return

        if (codigo == "POLICIA123") {

            db.collection("usuarios")
                .document(user.uid)
                .update(
                    mapOf(
                        "rol" to "policia",
                        "verificado" to true
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(context, "Ahora eres policía ✅", Toast.LENGTH_SHORT).show()
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
