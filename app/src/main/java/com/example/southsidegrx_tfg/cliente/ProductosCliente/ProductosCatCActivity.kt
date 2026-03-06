package com.example.southsidegrx_tfg.cliente.ProductosCliente

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.southsidegrx_tfg.Adapters.ProductosClienteAdapter
import com.example.southsidegrx_tfg.Modelos.Producto
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.ActivityProductosCatCactivityBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductosCatCActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductosCatCactivityBinding

    private lateinit var productoArrayList: ArrayList<Producto>
    private lateinit var productosAdapter: ProductosClienteAdapter
    private var nombreCat =""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProductosCatCactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //obtengo el nombre de la categoría enviada desde CategoriaClienteAdapter
        nombreCat = intent.getStringExtra("nombreCat").toString()
        binding.tvCategoria.text = nombreCat
        listarProductos(nombreCat)

        binding.edtBuscarProducto.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(filtro: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val consulta = filtro.toString()
                    productosAdapter.filter?.filter(consulta)
                }catch (e: Exception){

                }
            }
        })
    }

    private fun listarProductos(nombreCat:String){
        productoArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.orderByChild("categoria").equalTo(nombreCat)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    productoArrayList.clear()
                    for (ds in snapshot.children) {
                        val modeloProducto = Producto()
                        modeloProducto.id = ds.child("id").getValue(String::class.java) ?: ds.key ?: ""
                        modeloProducto.nombre = ds.child("nombre").getValue(String::class.java) ?: ""
                        modeloProducto.descripcion = ds.child("descripcion").getValue(String::class.java) ?: ""
                        modeloProducto.categoria = ds.child("categoria").getValue(String::class.java) ?: ""
                        modeloProducto.notaDesc = ds.child("notaDesc").getValue(String::class.java) ?: ""
                        modeloProducto.favorito = ds.child("favorito").getValue(Boolean::class.java) ?: false

                        modeloProducto.precio = leerDouble(ds, "precio")
                        modeloProducto.precioDesc = leerDouble(ds, "precioDesc")
                        modeloProducto.stock = leerDouble(ds, "stock")
                        
                        productoArrayList.add(modeloProducto)
                    }
                    productosAdapter = ProductosClienteAdapter(this@ProductosCatCActivity,productoArrayList)
                    binding.rvProductosC.adapter = productosAdapter
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