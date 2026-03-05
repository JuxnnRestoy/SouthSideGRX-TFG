package com.example.southsidegrx_tfg.Detalles

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.southsidegrx_tfg.Adapters.ProductosCompraAdapter
import com.example.southsidegrx_tfg.Modelos.ProductoCarrito
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.ActivityDetalleCompraBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalleCompraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleCompraBinding
    private lateinit var productosArrayList: ArrayList<ProductoCarrito>
    private lateinit var adapterProductoCompra: ProductosCompraAdapter

    private var idCompra =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleCompraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idCompra = intent.getStringExtra("idCompra")?: ""

        cargarCabecera()
        cargarProductosCompra()
    }

    private fun cargarCabecera(){
        val ref = FirebaseDatabase.getInstance().getReference("Compras")
            .child(idCompra)

        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val clienteNombre = snapshot.child("clienteNombre").getValue(String::class.java) ?: "-"
                val total = leerDouble(snapshot, "total")
                val fechaMs = snapshot.child("fecha").getValue(Long::class.java) ?: 0L

                binding.tvCliente.text = "Cliente: $clienteNombre"
                binding.tvTotal.text = String.format(Locale.getDefault(), "Total: %.2f CRD", total)

                val fechaTxt = if(fechaMs > 0){
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(fechaMs))
                }else{
                    "-"
                }
                binding.tvFecha.text = "Fecha: $fechaTxt"

                //compras nuevas (vendedorNombre)
                val vendedorNombreGuardado = snapshot.child("vendedorNombre").getValue(String::class.java)
                val vendedorUid = snapshot.child("vendedorUid").getValue(String::class.java) ?: ""
                if(!vendedorNombreGuardado.isNullOrEmpty()){
                    binding.tvVendedor.text = "Vendedor: $vendedorNombreGuardado"
                    return
                }

                //compras ya hechas hago un fallback a usuarios
                if(vendedorUid.isEmpty()){
                    binding.tvVendedor.text = "Vendedor: -"
                    return
                }
                binding.tvVendedor.text = "Vendedor..."
                val refVendedor = FirebaseDatabase.getInstance().getReference("Usuarios")
                    .child(vendedorUid)
                refVendedor.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val nombreV = snapshot.child("nombre").getValue(String::class.java)
                            ?:"Desconocido"
                        binding.tvVendedor.text = "Vendedor: $nombreV"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.tvVendedor.text = "Vendedor: $vendedorUid"
                    }

                })

            }

            override fun onCancelled(error: DatabaseError) {
                //si falla la lectura de Compra dejo valores por defecto
                binding.tvCliente.text = "Cliente: -"
                binding.tvVendedor.text = "Vendedor: -"
                binding.tvFecha.text = "Fecha: -"
                binding.tvTotal.text = "Total: 0.00 CRD"
            }
        })
    }

    private fun cargarProductosCompra(){
        productosArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Compras")
            .child(idCompra).child("items")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productosArrayList.clear()
                for(ds in snapshot.children){
                    val modelo = ProductoCarrito()
                    modelo.idProducto = ds.child("idProducto").getValue(String::class.java) ?: ""
                    modelo.nombre = ds.child("nombre").getValue(String::class.java) ?: ""
                    modelo.precio = leerDouble(ds,"precio")
                    modelo.precioDesc = leerDouble(ds,"precioDesc")
                    modelo.precioFinal = leerDouble(ds,"precioFinal")
                    modelo.cantidad = leerDouble(ds,"cantidad")

                    productosArrayList.add(modelo)
                }
                adapterProductoCompra = ProductosCompraAdapter(this@DetalleCompraActivity,productosArrayList)
                binding.rvProductosCompra.adapter = adapterProductoCompra
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun leerDouble(ds:DataSnapshot,campo:String):Double{

        val v = ds.child(campo).value ?: return 0.0

        return when(v){
            is Double -> v
            is Long -> v.toDouble()
            is Int -> v.toDouble()
            is String -> v.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}