package com.example.southsidegrx_tfg.vendedor.Bottom_Nav_Fragments_Vendedor

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.southsidegrx_tfg.Adapters.ProductoAdapter
import com.example.southsidegrx_tfg.Modelos.Producto
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.FragmentMisProductosVBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FragmentMisProductosV : Fragment() {

    private lateinit var binding: FragmentMisProductosVBinding
    private lateinit var mContext: Context

    private lateinit var productoArrayList: ArrayList<Producto>
    private lateinit var adapterProducto: ProductoAdapter

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMisProductosVBinding.inflate(LayoutInflater.from(mContext),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listarProductos()

        binding.edtBuscarProducto.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(filtro: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val consulta = filtro.toString()
                    adapterProducto.filter?.filter(consulta)
                }catch (e: Exception){

                }
            }
        })
    }

    private fun listarProductos(){
        productoArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productoArrayList.clear()
                for(ds in snapshot.children){
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
                adapterProducto = ProductoAdapter(mContext,productoArrayList)
                binding.rvProductos.adapter = adapterProducto
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