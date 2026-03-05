package com.example.southsidegrx_tfg.vendedor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.southsidegrx_tfg.Adapters.CarritoAdapter
import com.example.southsidegrx_tfg.Modelos.ProductoCarrito
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.ActivityVentaCarritoClienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class VentaCarritoClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVentaCarritoClienteBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var productosArrayList:ArrayList<ProductoCarrito>
    private lateinit var adapterCarrito: CarritoAdapter

    private var clienteUid:String = ""
    private var clienteNombre:String=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVentaCarritoClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        clienteUid = intent.getStringExtra("clienteUid")?: ""
        clienteNombre = intent.getStringExtra("clienteNombre")?: ""

        binding.tvCliente.text = "Carrito de: $clienteNombre"

        cargarCarritoCliente()

        binding.btnVenta.setOnClickListener { confirmarVenta() }
    }

    private fun cargarCarritoCliente(){
        productosArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
            .child(clienteUid).child("CarritoCompras")

        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productosArrayList.clear()
                var total = 0.0
                for(ds in snapshot.children){
                    val modelo = ProductoCarrito()
                    modelo.idProducto = ds.child("idProducto").getValue(String::class.java)?: ds.key ?:""
                    modelo.nombre = ds.child("nombre").getValue(String::class.java)?:""
                    modelo.precio = leerDouble(ds,"precio")
                    modelo.precioDesc = leerDouble(ds,"precioDesc")
                    modelo.precioFinal = leerDouble(ds,"precioFinal")
                    modelo.cantidad = leerDouble(ds,"cantidad")
                    total+=modelo.precioFinal
                    productosArrayList.add(modelo)
                }

                adapterCarrito = CarritoAdapter(this@VentaCarritoClienteActivity,productosArrayList,clienteUid)
                binding.rvCarritoCliente.adapter = adapterCarrito
                binding.tvTotal.text = String.format("%.2f CRD",total)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun confirmarVenta(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
            .child(clienteUid).child("CarritoCompras")

        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = HashMap<String,Any>()
                var total = 0.0

                for(ds in snapshot.children){
                    val idProducto = ds.key ?:continue
                    val cantidad = leerDouble(ds,"cantidad")
                    val precioFinal = leerDouble(ds,"precioFinal")
                    total +=precioFinal

                    val item = HashMap<String,Any>()
                    item["idProducto"] = idProducto
                    item["nombre"] = ds.child("nombre").getValue(String::class.java) ?: ""
                    item["precio"] = leerDouble(ds, "precio")
                    item["precioDesc"] = leerDouble(ds, "precioDesc")
                    item["cantidad"] = cantidad
                    item["precioFinal"] = precioFinal
                    items[idProducto] = item
                }

                // compruebo carrito vacío
                if(items.isEmpty()){
                    Toast.makeText(this@VentaCarritoClienteActivity, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                    return
                }

                //nombre del vendedor
                val vendedorUid = firebaseAuth.uid?:""
                if(vendedorUid.isEmpty()){
                    Toast.makeText(this@VentaCarritoClienteActivity,"No se pudo obtener el vendedor",Toast.LENGTH_SHORT).show()
                    return
                }
                // leo nombre del vendedor en usuarios con el vendedorUid
                val refVendedor = FirebaseDatabase.getInstance().getReference("Usuarios")
                    .child(vendedorUid)
                refVendedor.addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val vendedorNombre = snapshot.child("nombre").getValue(String::class.java)
                            ?:"Desconocido"

                        // DESCUEnto stock con transactions por lo que si falla no crea la compra
                        descontarStockProducto(items,onSuccess ={
                            //creo la compra
                            val comprasRef = FirebaseDatabase.getInstance().getReference("Compras")
                            val idCompra = comprasRef.push().key ?: return@descontarStockProducto

                            val compra = HashMap<String, Any>()
                            compra["idCompra"] = idCompra
                            compra["clienteUid"] = clienteUid
                            compra["clienteNombre"] = clienteNombre
                            compra["vendedorUid"] = vendedorUid
                            compra["vendedorNombre"] = vendedorNombre
                            compra["total"] = total
                            compra["fecha"] = System.currentTimeMillis()
                            compra["items"] = items

                            val updates = HashMap<String, Any?>()
                            updates["/Compras/$idCompra"] = compra
                            updates["/Usuarios/$clienteUid/Compras/$idCompra"] = true
                            updates["/Usuarios/$clienteUid/CarritoCompras"] = null //vaciar carrito

                            FirebaseDatabase.getInstance().reference.updateChildren(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(this@VentaCarritoClienteActivity, "Venta realizada", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e->
                                    Toast.makeText(this@VentaCarritoClienteActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        },onError = {msg->
                            Toast.makeText(this@VentaCarritoClienteActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                        )
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@VentaCarritoClienteActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }

                })

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@VentaCarritoClienteActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun descontarStockProducto(items: HashMap<String,Any>,onSuccess:()->Unit,onError:(msg:String)->Unit){
        val productoRef = FirebaseDatabase.getInstance().getReference("Productos")
        val keys = items.keys.toList()
        if(keys.isEmpty()){
            onError("Carrito vacío")
            return
        }

        var pendientes = keys.size
        var fallo = false

        for(idProducto in keys){
            val item = items[idProducto] as HashMap<*,*>
            val cantidad = (item["cantidad"] as? Double)?:0.0

            val stockRef = productoRef.child(idProducto).child("stock")
            stockRef.runTransaction(object: Transaction.Handler{
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val actual = when(val v=currentData.value){
                        is Double -> v
                        is Long -> v.toDouble()
                        is Int -> v.toDouble()
                        is String -> v.replace(',', '.').toDoubleOrNull() ?: 0.0
                        null -> 0.0
                        else -> 0.0

                    }
                    val nuevoStock = actual-cantidad
                    if(nuevoStock<0.0) return Transaction.abort()

                    currentData.value=nuevoStock
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if(fallo) return
                    if(error!=null){
                        fallo=true
                        onError("Error al actualizar el stock: ${error.message}")
                        return
                    }
                    if(!committed){
                        fallo=true
                        onError("No hay stock suficiente para completar la venta")
                        return
                    }
                    pendientes--
                    if(pendientes==0 && !fallo) onSuccess()
                }
            })
        }
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