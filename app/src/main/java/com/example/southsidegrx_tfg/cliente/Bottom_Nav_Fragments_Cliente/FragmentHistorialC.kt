package com.example.southsidegrx_tfg.cliente.Bottom_Nav_Fragments_Cliente

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.southsidegrx_tfg.Adapters.ComprasClienteAdapter
import com.example.southsidegrx_tfg.Modelos.Compra
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.FragmentHistorialCBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentHistorialC : Fragment() {

    private lateinit var binding: FragmentHistorialCBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var comprasArrayList: ArrayList<Compra>
    private lateinit var adapterCompra: ComprasClienteAdapter

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistorialCBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        cargarMisCompras()
    }

    private fun cargarMisCompras(){
        comprasArrayList = ArrayList()

        val uid = firebaseAuth.uid?: return
        val ref = FirebaseDatabase.getInstance().getReference("Compras")
            .orderByChild("clienteUid").equalTo(uid)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                comprasArrayList.clear()
                for(ds in snapshot.children){
                    val modelo = Compra()
                    modelo.idCompra = ds.child("idCompra").getValue(String::class.java) ?: ds.key ?: ""
                    modelo.clienteUid = ds.child("clienteUid").getValue(String::class.java) ?: ""
                    modelo.clienteNombre = ds.child("clienteNombre").getValue(String::class.java) ?: ""
                    modelo.vendedorUid = ds.child("vendedorUid").getValue(String::class.java) ?: ""
                    modelo.vendedorNombre = ds.child("vendedorNombre").getValue(String::class.java) ?: ""
                    modelo.total = leerDouble(ds, "total")
                    modelo.fecha = ds.child("fecha").getValue(Long::class.java) ?: 0L

                    comprasArrayList.add(modelo)
                }
                adapterCompra = ComprasClienteAdapter(mContext,comprasArrayList)
                binding.rvMisCompras.adapter = adapterCompra
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