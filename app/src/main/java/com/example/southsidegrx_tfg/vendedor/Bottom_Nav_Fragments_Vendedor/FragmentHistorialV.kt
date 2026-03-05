package com.example.southsidegrx_tfg.vendedor.Bottom_Nav_Fragments_Vendedor

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.southsidegrx_tfg.Adapters.ComprasVendedorAdapter
import com.example.southsidegrx_tfg.Modelos.Compra
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.FragmentHistorialVBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentHistorialV : Fragment() {

    private lateinit var binding: FragmentHistorialVBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var comprasArrayList: ArrayList<Compra>
    private lateinit var adapterCompra: ComprasVendedorAdapter

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHistorialVBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firebaseAuth = FirebaseAuth.getInstance()
        cargarCompras()
    }

    private fun cargarCompras(){
        comprasArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Compras")
        ref.orderByChild("vendedorUid").equalTo(firebaseAuth.uid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    comprasArrayList.clear()

                    for(ds in snapshot.children){
                        val modelo = Compra()

                        modelo.idCompra = ds.child("idCompra").getValue(String::class.java) ?: ""
                        modelo.clienteUid = ds.child("clienteUid").getValue(String::class.java) ?: ""
                        modelo.clienteNombre = ds.child("clienteNombre").getValue(String::class.java) ?: ""
                        modelo.vendedorUid = ds.child("vendedorUid").getValue(String::class.java) ?: ""
                        modelo.total = leerDouble(ds,"total")
                        modelo.fecha = ds.child("fecha").getValue(Long::class.java) ?: 0

                        comprasArrayList.add(modelo)
                    }
                    if(comprasArrayList.isEmpty()){
                        binding.tvSinVentas.visibility = View.VISIBLE
                    }else{
                        binding.tvSinVentas.visibility = View.GONE
                    }
                    adapterCompra = ComprasVendedorAdapter(mContext,comprasArrayList)
                    binding.rvCompras.adapter = adapterCompra
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