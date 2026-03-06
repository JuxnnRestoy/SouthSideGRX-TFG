package com.example.southsidegrx_tfg.cliente.Nav_Fragments_Cliente

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.FragmentMiPerfilClienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FragmentMiPerfilCliente : Fragment() {

    private var _binding: FragmentMiPerfilClienteBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMiPerfilClienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        cargarPerfil()
    }

    private fun cargarPerfil() {
        val uid = firebaseAuth.uid ?: return

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""
                val dni = snapshot.child("dni").getValue(String::class.java) ?: ""
                val fechaNac = snapshot.child("fechaNac").getValue(String::class.java) ?: ""
                val tipoUsuario = snapshot.child("tipoUsuario").getValue(String::class.java) ?: ""
                val imagen = snapshot.child("imagen").getValue(String::class.java) ?: ""

                binding.tvNombreCliente.text = nombre
                binding.tvEmailCliente.text = email
                binding.tvDniCliente.text = dni
                binding.tvFechaNacCliente.text = fechaNac
                binding.tvTipoUsuarioCliente.text = tipoUsuario.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }

                if (imagen.isNotEmpty()) {
                    try {
                        Glide.with(requireContext())
                            .load(imagen)
                            .placeholder(R.drawable.ico_login_cliente)
                            .error(R.drawable.ico_login_cliente)
                            .into(binding.imgPerfilCliente)
                    } catch (e: Exception) {
                        binding.imgPerfilCliente.setImageResource(R.drawable.ico_login_cliente)
                    }
                } else {
                    binding.imgPerfilCliente.setImageResource(R.drawable.ico_login_cliente)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}