package com.example.southsidegrx_tfg.cliente.Bottom_Nav_Fragments_Cliente

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.southsidegrx_tfg.Adapters.CarritoAdapter
import com.example.southsidegrx_tfg.Modelos.ProductoCarrito
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.FragmentCarritoCompraBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentCarritoCompra : Fragment() {

    private lateinit var binding: FragmentCarritoCompraBinding

    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productosArrayList: ArrayList<ProductoCarrito>
    private lateinit var adapterCarrito: CarritoAdapter

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCarritoCompraBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        cargarProdCarrito()
        sumaProductos()
    }

    private fun sumaProductos(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("CarritoCompras")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var suma = 0.0
                    for(p in snapshot.children){
                        val precioFinal = leerDouble(p,"precioFinal")
                        suma += precioFinal
                    }
                    binding.sumaProductos.setText(String.format("%.2f CRD",suma))
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun cargarProdCarrito(){
        productosArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("CarritoCompras")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    productosArrayList.clear()
                    for(ds in snapshot.children){
                        val modelo = ProductoCarrito()

                        modelo.idProducto = ds.child("idProducto").getValue(String::class.java) ?: ds.key ?: ""
                        modelo.nombre = ds.child("nombre").getValue(String::class.java) ?: ""

                        modelo.precio = leerDouble(ds, "precio")
                        modelo.precioDesc = leerDouble(ds, "precioDesc")
                        modelo.precioFinal = leerDouble(ds, "precioFinal")
                        modelo.cantidad = leerDouble(ds, "cantidad")

                        productosArrayList.add(modelo)
                    }
                    adapterCarrito = CarritoAdapter(mContext,productosArrayList,firebaseAuth.uid!!)
                    binding.rvCarrito.adapter = adapterCarrito
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