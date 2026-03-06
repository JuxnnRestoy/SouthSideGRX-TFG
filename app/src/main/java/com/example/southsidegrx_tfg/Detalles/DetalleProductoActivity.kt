package com.example.southsidegrx_tfg.Detalles

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.southsidegrx_tfg.Adapters.ImagenSliderAdapter
import com.example.southsidegrx_tfg.Modelos.ImgSlider
import com.example.southsidegrx_tfg.Modelos.Producto
import com.example.southsidegrx_tfg.databinding.ActivityDetalleProductoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetalleProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleProductoBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var idProducto = "" //id del producto enviado desde adaptador producto cliente

    private lateinit var imagenSlider: ArrayList<ImgSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //obtenemos el id del producto enviado desde el adapter
        idProducto = intent.getStringExtra("idProducto").toString()
        cargarImagenesProducto()

        cargarInfoProducto()

    }

    private fun cargarImagenesProducto() {
        imagenSlider = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).child("Imagenes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    imagenSlider.clear()
                    for(ds in snapshot.children){
                        try{
                            val modeloImagenSlider = ds.getValue(ImgSlider::class.java)
                            imagenSlider.add(modeloImagenSlider!!)
                        }catch (e: Exception){

                        }
                    }
                    val imgSliderAdapter = ImagenSliderAdapter(this@DetalleProductoActivity,imagenSlider)
                    binding.imagenVp.adapter = imgSliderAdapter
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun cargarInfoProducto(){
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
            val modeloProducto = Producto()
            modeloProducto.id = snapshot.child("id").getValue(String::class.java) ?: snapshot.key ?: ""
            modeloProducto.nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
            modeloProducto.descripcion = snapshot.child("descripcion").getValue(String::class.java) ?: ""
            modeloProducto.categoria = snapshot.child("categoria").getValue(String::class.java) ?: ""
            modeloProducto.notaDesc = snapshot.child("notaDesc").getValue(String::class.java) ?: ""
            modeloProducto.favorito = snapshot.child("favorito").getValue(Boolean::class.java) ?: false

            modeloProducto.precio = leerDouble(snapshot, "precio")
            modeloProducto.precioDesc = leerDouble(snapshot, "precioDesc")
            modeloProducto.stock = leerDouble(snapshot, "stock")

            val nombre = modeloProducto.nombre
            val descripcion = modeloProducto.descripcion
            val precio = modeloProducto.precio
            val precioDesc = modeloProducto.precioDesc
            val notaDesc = modeloProducto.notaDesc

            binding.nombrePD.text = nombre
            binding.descripcionPD.text = descripcion
            binding.precioPD.text = String.format("%.2f CRD", precio)

            if(precioDesc > 0.0 && notaDesc.isNotEmpty()){
                // producto con descuento
                binding.precioDescPD.text = String.format("%.2f CRD", precioDesc)
                binding.notaDescPD.text = notaDesc
                binding.precioPD.paintFlags = binding.precioPD.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            }else{
                binding.precioDescPD.visibility = View.GONE
                binding.notaDescPD.visibility = View.GONE

            }


            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun leerDouble(ds: DataSnapshot, campo: String): Double {
        val v = ds.child(campo).value ?: return 0.0
        return when (v) {
            is Double -> v
            is Long -> v.toDouble()
            is Int -> v.toDouble()
            is String -> v.replace(',', '.').toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}