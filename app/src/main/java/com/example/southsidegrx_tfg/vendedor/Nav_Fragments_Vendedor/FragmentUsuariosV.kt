package com.example.southsidegrx_tfg.vendedor.Nav_Fragments_Vendedor

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.southsidegrx_tfg.Adapters.UsuariosVendedorAdapter
import com.example.southsidegrx_tfg.Modelos.Usuarios
import com.example.southsidegrx_tfg.databinding.FragmentUsuariosVBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentUsuariosV : Fragment() {

    private lateinit var binding: FragmentUsuariosVBinding
    private lateinit var mContext: Context

    private lateinit var usuariosArrayList: ArrayList<Usuarios>
    private lateinit var usuariosAdapter: UsuariosVendedorAdapter

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUsuariosVBinding.inflate(layoutInflater, container, false)

        /*
        binding.btnAnadirCliente.setOnClickListener { startActivity(Intent(mContext, RegistroUsuarioActivity::class.java).putExtra("tipoUsuario","cliente")) }
        binding.btnAnadirVendedor.setOnClickListener { startActivity(Intent(mContext, RegistroUsuarioActivity::class.java).putExtra("tipoUsuario","vendedor")) }


         */
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listarUsuarios()
    }

    private fun listarUsuarios(){
        usuariosArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usuariosArrayList.clear()

                for (ds in snapshot.children){
                    val modelo = Usuarios()
                    modelo.uid = ds.child("uid").getValue(String::class.java)?:ds.key ?: ""
                    modelo.nombre = ds.child("nombre").getValue(String::class.java)?:ds.key?:""
                    modelo.email = ds.child("email").getValue(String::class.java)?:ds.key?:""
                    modelo.tipoUsuario = ds.child("tipoUsuario").getValue(String::class.java)?:ds.key?:""
                    modelo.imagen = ds.child("imagen").getValue(String::class.java)?:""

                    usuariosArrayList.add(modelo)

                }
                usuariosAdapter = UsuariosVendedorAdapter(mContext,usuariosArrayList)
                binding.rvUsuarios.adapter = usuariosAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}