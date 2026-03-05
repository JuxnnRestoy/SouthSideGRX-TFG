package com.example.southsidegrx_tfg.cliente.Bottom_Nav_Fragments_Cliente

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.southsidegrx_tfg.Adapters.CategoriasClienteAdapter
import com.example.southsidegrx_tfg.Modelos.Categoria
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.FragmentTiendaCBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentTiendaC : Fragment() {

    private lateinit var binding: FragmentTiendaCBinding
    private lateinit var mContext: Context

    private lateinit var categoriaArrayList: ArrayList<Categoria>
    private lateinit var categoriasAdapter: CategoriasClienteAdapter
    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTiendaCBinding.inflate(LayoutInflater.from(mContext),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listarCategorias()
    }

    private fun listarCategorias(){
        categoriaArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categorias")
            .orderByChild("categoria")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoriaArrayList.clear()
                for(ds in snapshot.children){
                    val modelo = ds.getValue(Categoria::class.java)
                    categoriaArrayList.add(modelo!!)
                }
                categoriasAdapter = CategoriasClienteAdapter(mContext,categoriaArrayList)
                binding.rvCategorias.adapter = categoriasAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}