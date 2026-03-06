package com.example.southsidegrx_tfg

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.southsidegrx_tfg.cliente.MainActivityCliente
import com.example.southsidegrx_tfg.databinding.ActivityLoginBinding
import com.example.southsidegrx_tfg.vendedor.MainActivityVendedor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private var email=""
    private var contrasena=""

    //private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)*/

        //binding.btnLoginTelefono.setOnClickListener { startActivity(Intent(this, LoginTelefonoActivity::class.java)) }

        /*
        binding.tvRegistrarC.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegistroUsuarioActivity::class.java))
        }*/

        //Iniciar sesion con una cuenta de Google
        //binding.btnLoginGoogle.setOnClickListener { googleLogin() }

        firebaseAuth = FirebaseAuth.getInstance()

        binding.tvOlvidePass.setOnClickListener {
            recuperarContrasena()
        }

        binding.btnLoginC.setOnClickListener {
            validarInformacion()
        }
    }

    private fun recuperarContrasena() {
        val correo = binding.edtEmailC.text.toString().trim()

        binding.tilEmailC.error = null

        when {
            correo.isEmpty() -> {
                binding.tilEmailC.error = "Introduce tu email para recuperar la contraseña"
                binding.edtEmailC.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                binding.tilEmailC.error = "Email no válido"
                binding.edtEmailC.requestFocus()
            }
            else -> {
                setLoading(true)

                firebaseAuth.sendPasswordResetEmail(correo)
                    .addOnSuccessListener {
                        setLoading(false)
                        Toast.makeText(this, "Te hemos enviado un correo para restablecer la contraseña", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        setLoading(false)
                        Toast.makeText(this, "No se pudo enviar el correo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun irSegunTipoUsuario(uid:String){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tipo = snapshot.child("tipoUsuario").getValue(String::class.java)
                    ?.lowercase()?.trim()

                setLoading(false)

                when(tipo){
                    "vendedor" -> {
                        startActivity(Intent(this@LoginActivity, MainActivityVendedor::class.java))
                        finishAffinity()
                    }
                    "cliente" ->{
                        startActivity(Intent(this@LoginActivity, MainActivityCliente::class.java))
                        finishAffinity()
                    }
                    else ->{
                        Toast.makeText(this@LoginActivity,"Tu usuario no tiene tipo asignado",
                            Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                setLoading(false)
                Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }
    private fun validarInformacion(){
        binding.tilEmailC.error=null
        binding.tilContrasenaC.error=null

        email = binding.edtEmailC.text.toString().trim()
        contrasena = binding.edtContrasenaC.text.toString().trim()

        when{
            email.isEmpty()->{
                binding.tilEmailC.error = "Ingrese su email"
                binding.edtEmailC.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches()->{
                binding.tilEmailC.error="Ingrese un email válido"
                binding.edtEmailC.requestFocus()
            }
            contrasena.isEmpty()->{
                binding.tilContrasenaC.error="Ingrese su contraseña"
                binding.edtContrasenaC.requestFocus()
            }
            else-> loginCliente()
        }
    }

    private fun setLoading(cargado: Boolean){
        binding.overlayCargando.visibility =
            if(cargado) View.VISIBLE else View.GONE

        binding.btnLoginC.isEnabled=!cargado
        binding.tvRegistrarC.isEnabled=!cargado
        binding.edtEmailC.isEnabled=!cargado
        binding.edtContrasenaC.isEnabled=!cargado
    }

    private fun loginCliente(){
        setLoading(true)

        firebaseAuth.signInWithEmailAndPassword(email,contrasena)
            .addOnSuccessListener {
                val uid = firebaseAuth.uid
                if(uid == null){
                    setLoading(false)
                    Toast.makeText(this,"No se pudo obtener el UID", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                irSegunTipoUsuario(uid)
            }
            .addOnFailureListener {e ->
                setLoading(false)
                Toast.makeText(this,"No se pudo iniciar sesión debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}