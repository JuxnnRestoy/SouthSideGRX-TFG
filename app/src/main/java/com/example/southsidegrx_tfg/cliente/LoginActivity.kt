package com.example.southsidegrx_tfg.cliente

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        binding.btnLoginC.setOnClickListener {
            validarInformacion()
        }
    }

    private fun irSegunTipoUsuario(uid:String){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
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
                        Toast.makeText(this@LoginActivity,"Tu usuario no tiene tipo asignado",Toast.LENGTH_SHORT).show()
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
            if(cargado) android.view.View.VISIBLE else android.view.View.GONE

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

    /*
    private fun googleLogin(){
        val googleSignInClient = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInClient)
    }
    private val googleSignInARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if(result.resultCode == RESULT_OK){
            // si el usuario selecciona una cuenta del cuadro de diálogo
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val cuenta = task.getResult(ApiException::class.java)
                autenticacionGoogle(cuenta.idToken)
            }catch (e: Exception){
                Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this,"La operación ha sido cancelada",Toast.LENGTH_SHORT).show()
        }
    }

    private fun autenticacionGoogle(idToken:String?){
        val credencial = GoogleAuthProvider.getCredential(idToken,null)
        firebaseAuth.signInWithCredential(credencial)
            .addOnSuccessListener {result ->
                val uid = firebaseAuth.uid ?: return@addOnSuccessListener

                if(result.additionalUserInfo!!.isNewUser){
                    rellenarInfoBD(uid) //no existia
                }else{
                    irSegunTipoUsuario(uid) // existía ya
                }
            }
            .addOnFailureListener { e->
                Toast.makeText(this,"${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun rellenarInfoBD(uid: String){
        val nombreC = firebaseAuth.currentUser?.displayName?:""
        val emailC = firebaseAuth.currentUser?.email?:""
        val tiempoRegistro = Funciones().obtenerTiempoRegistro()

        val datosCliente = HashMap<String,Any>()
        datosCliente["uid"]=uid
        datosCliente["nombre"] = nombreC
        datosCliente["email"] = emailC
        datosCliente["tipoUsuario"] = "cliente"
        datosCliente["tiempoRegistro"] = tiempoRegistro
        datosCliente["imagen"] = ""

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid).setValue(datosCliente)
            .addOnSuccessListener {
                irSegunTipoUsuario(uid)
            }
            .addOnFailureListener { e->
                setLoading(false)
                Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

     */
