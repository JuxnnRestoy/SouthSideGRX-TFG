package com.example.southsidegrx_tfg.vendedor.Nav_Fragments_Vendedor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.FragmentInicioVBinding
import com.example.southsidegrx_tfg.vendedor.Bottom_Nav_Fragments_Vendedor.FragmentMisProductosV
import com.example.southsidegrx_tfg.vendedor.Bottom_Nav_Fragments_Vendedor.FragmentHistorialV
import com.example.southsidegrx_tfg.vendedor.Productos.AgregarProductoActivity

class FragmentInicioV : Fragment() {

    private lateinit var binding: FragmentInicioVBinding
    private lateinit var mcontext: Context

    // inicializar contexto
    override fun onAttach(context: Context) {
        mcontext= context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInicioVBinding.inflate(inflater,container,false)

        binding.bottomNavigation.setOnItemSelectedListener{
            when(it.itemId){
                R.id.op_mis_productos_v->{
                    reemplazarFragment(FragmentMisProductosV())
                }
                R.id.op_historial_v->{
                    reemplazarFragment(FragmentHistorialV())
                }
            }
            true
        }
        reemplazarFragment(FragmentMisProductosV())
        binding.bottomNavigation.selectedItemId = R.id.op_mis_productos_v

        binding.agregarFab.setOnClickListener {
            val intent = Intent(mcontext, AgregarProductoActivity::class.java)
            intent.putExtra("Edicion",false)
            mcontext.startActivity(intent)
        }
        return binding.root
    }

    private fun reemplazarFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().replace(R.id.bottomFragment, fragment)
            .commit()
    }
}