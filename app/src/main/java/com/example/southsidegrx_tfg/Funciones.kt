package com.example.southsidegrx_tfg

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Funciones {

    fun ocultarTecladoAlTocarFuera(context: Context,view: View){
        if (view !is EditText) {
            view.setOnTouchListener { v, _ ->
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                false
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                ocultarTecladoAlTocarFuera(context, innerView)
            }
        }
    }

    fun obtenerTiempoRegistro():Long {
        return System.currentTimeMillis()
    }

    fun agregarProductoFavorito(context: Context,idProducto:String){
        val firebaseAuth = FirebaseAuth.getInstance()
        val tiempo = Funciones().obtenerTiempoRegistro() // servirá como id

        val hashMap = HashMap<String,Any>()
        hashMap["idProducto"] = idProducto
        hashMap["idFav"] = tiempo

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(idProducto)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(context,"Producto agregado a favoritos",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context,"No se ha podido agregar el producto a favoritos",Toast.LENGTH_SHORT).show()
            }

    }

    fun eliminarProductoFavorito(context: Context,idProducto:String){
        var firebaseAuth = FirebaseAuth.getInstance()

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(idProducto)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context,"Producto eliminado de favoritos",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context,"${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
}