package com.example.southsidegrx_tfg

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.example.southsidegrx_tfg.cliente.LoginActivity
import com.example.southsidegrx_tfg.cliente.MainActivityCliente
import com.example.southsidegrx_tfg.vendedor.MainActivityVendedor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        firebaseAuth = FirebaseAuth.getInstance()

        verBienvenida()
    }

    private fun verBienvenida() {
        object: CountDownTimer(3000,1000){
            override fun onFinish() {
                comprobarTipoUsuario()
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()
    }

    private fun comprobarTipoUsuario(){
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser==null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }else{
            val referencia = FirebaseDatabase.getInstance().getReference("Usuarios")
            referencia.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tipoUsuario = snapshot.child("tipoUsuario").getValue(String::class.java)?.lowercase()?.trim()

                        when(tipoUsuario){
                            "vendedor" ->{
                                startActivity(Intent(this@SplashScreenActivity, MainActivityVendedor::class.java))
                                finishAffinity()
                            }
                            "cliente"->{
                                startActivity(Intent(this@SplashScreenActivity, MainActivityCliente::class.java))
                                finishAffinity()
                            }
                            else ->{
                                // si no existe tipoUsuario o está mal cierro sesión y voy a login
                                firebaseAuth.signOut()
                                startActivity(Intent(this@SplashScreenActivity,LoginActivity::class.java))
                                finish()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        firebaseAuth.signOut()
                        startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
                        finish()
                    }
                })
        }
    }
}