package com.example.southsidegrx_tfg.cliente

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.cliente.Bottom_Nav_Fragments_Cliente.FragmentHistorialC
import com.example.southsidegrx_tfg.cliente.Nav_Fragments_Cliente.FragmentInicioCliente
import com.example.southsidegrx_tfg.cliente.Nav_Fragments_Cliente.FragmentMiPerfilCliente
import com.example.southsidegrx_tfg.databinding.ActivityMainClienteBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivityCliente : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainClienteBinding
    private var firebaseAuth: FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        binding.navigationView.setNavigationItemSelectedListener(this)

        val toogle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.abrir_drawer,
            R.string.cerrar_drawer
        )

        binding.drawerLayout.addDrawerListener(toogle)
        toogle.syncState()

        reemplazarFragment(FragmentInicioCliente())
    }

    private fun comprobarSesion(){
        if(firebaseAuth!!.currentUser==null){
            startActivity(Intent(this@MainActivityCliente, LoginActivity::class.java))
            finishAffinity()
        }else{
            Toast.makeText(this,"Usuario en línea",Toast.LENGTH_SHORT).show()
        }
    }

    private fun cerrarSesion(){
        firebaseAuth!!.signOut()
        startActivity(Intent(this@MainActivityCliente, LoginActivity::class.java))
        finishAffinity()
        Toast.makeText(this,"Cerrando sesión",Toast.LENGTH_SHORT).show()
    }

    private fun reemplazarFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navFragment,fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.op_inicio_c -> {reemplazarFragment(FragmentInicioCliente())}
            R.id.op_mi_perfil_c -> { reemplazarFragment(FragmentMiPerfilCliente())}
            R.id.op_historial_c ->{reemplazarFragment(FragmentHistorialC())}
            R.id.op_cerrar_sesion_c->{cerrarSesion()}
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}