package com.example.southsidegrx_tfg.cliente.Nav_Fragments_Cliente

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.cliente.Bottom_Nav_Fragments_Cliente.FragmentCarritoCompra
import com.example.southsidegrx_tfg.cliente.Bottom_Nav_Fragments_Cliente.FragmentFavoritosCliente
import com.example.southsidegrx_tfg.cliente.Bottom_Nav_Fragments_Cliente.FragmentHistorialC
import com.example.southsidegrx_tfg.cliente.Bottom_Nav_Fragments_Cliente.FragmentTiendaC
import com.example.southsidegrx_tfg.databinding.FragmentInicioClienteBinding

class FragmentInicioCliente : Fragment() {

    private lateinit var binding: FragmentInicioClienteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInicioClienteBinding.inflate(inflater,container,false)

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.op_mi_tienda_c ->{
                    reemplazarFragment(FragmentTiendaC())
                }
                R.id.op_favoritos_c ->{
                    reemplazarFragment(FragmentFavoritosCliente())
                }
                R.id.op_carrito_c->{
                    reemplazarFragment(FragmentCarritoCompra())
                }
                R.id.op_historial_c->{
                    reemplazarFragment(FragmentHistorialC())
                }
            }
            true
        }
        reemplazarFragment(FragmentTiendaC())
        binding.bottomNavigation.selectedItemId = R.id.op_mi_tienda_c
        return binding.root
    }

    private fun reemplazarFragment(fragment: androidx.fragment.app.Fragment) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.bottomFragment,fragment)
            .commit()
    }
}