package com.example.southsidegrx_tfg.cliente.Bottom_Nav_Fragments_Cliente

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.southsidegrx_tfg.Adapters.ProductosClienteAdapter
import com.example.southsidegrx_tfg.Modelos.Producto
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.FragmentFavoritosClienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class FragmentFavoritosCliente : Fragment() {

    private lateinit var binding: FragmentFavoritosClienteBinding

    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productosArrayList:ArrayList<Producto>
    private lateinit var productosAdapter: ProductosClienteAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoritosClienteBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        cargarProductosFavoritos()
    }

    private fun cargarProductosFavoritos(){
        productosArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    productosArrayList.clear()
                    for (ds in snapshot.children) {
                        val idProducto = "${ds.child("idProducto").value}"
                        val refProducto = FirebaseDatabase.getInstance().getReference("Productos")
                        refProducto.child(idProducto)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    try {
                                        val modeloProducto = snapshot.getValue(Producto::class.java)
                                        productosArrayList.add(modeloProducto!!)
                                    } catch (e: Exception) {

                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })
                    }
                    Handler().postDelayed({
                        productosAdapter = ProductosClienteAdapter(mContext,productosArrayList)
                        binding.rvFavoritos.adapter = productosAdapter
                    },500)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}