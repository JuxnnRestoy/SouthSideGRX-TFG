package com.example.southsidegrx_tfg.vendedor

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.LoginActivity
import com.example.southsidegrx_tfg.databinding.ActivityMainVendedorBinding
import com.example.southsidegrx_tfg.vendedor.Bottom_Nav_Fragments_Vendedor.FragmentMisProductosV
import com.example.southsidegrx_tfg.vendedor.Bottom_Nav_Fragments_Vendedor.FragmentHistorialV
import com.example.southsidegrx_tfg.vendedor.Nav_Fragments_Vendedor.FragmentCategoriasAgregarV
import com.example.southsidegrx_tfg.vendedor.Nav_Fragments_Vendedor.FragmentInicioV
import com.example.southsidegrx_tfg.vendedor.Nav_Fragments_Vendedor.FragmentUsuariosV
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivityVendedor : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainVendedorBinding
    private var firebaseAuth: FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        binding.navigationView.setNavigationItemSelectedListener(this)

        val toogle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolBar,
            R.string.abrir_drawer,
            R.string.cerrar_drawer
        )
        binding.drawerLayout.addDrawerListener(toogle)
        toogle.syncState()

        //Establecer fragmento por defecto

        reemplazarFragment(FragmentInicioV())
        binding.navigationView.setCheckedItem(R.id.op_inicio_v)
    }

    private fun cerrarSesion(){
        firebaseAuth!!.signOut()
        startActivity(Intent(applicationContext, LoginActivity::class.java))
        finishAffinity()
        Toast.makeText(applicationContext,"Has cerrado sesión",Toast.LENGTH_SHORT).show()
    }

    private fun comprobarSesion() {
        // si usuario no ha iniciado sesión, lo manda a la ventana de registro
        if(firebaseAuth!!.currentUser==null){
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            finishAffinity()
        }else{
            Toast.makeText(applicationContext,"Vendedor ya registrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reemplazarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.navFragment, fragment).addToBackStack(null)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.op_inicio_v->{
                reemplazarFragment(FragmentInicioV())
            }
            R.id.op_categorias_v-> {
                reemplazarFragment(FragmentCategoriasAgregarV())
            }
            R.id.op_usuarios_v->{
                reemplazarFragment(FragmentUsuariosV())
            }
            R.id.op_cerrar_sesion_v->{
                cerrarSesion()
            }
            R.id.op_mis_productos_v->{
                reemplazarFragment(FragmentMisProductosV())
            }
            R.id.op_historial_v->{
                reemplazarFragment(FragmentHistorialV())
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}