package com.example.southsidegrx_tfg.vendedor

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.southsidegrx_tfg.Funciones
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.ActivityRegistroVendedorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistroVendedorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroVendedorBinding
    private lateinit var firebaseAuth: FirebaseAuth
    //private lateinit var progressDialog: ProgressDialog
    private var nombre =""
    private var email =""
    private var contrasena=""
    private var ccontrasena=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //progressDialog = ProgressDialog(this)
        //progressDialog.setTitle("Espere por favor")
        //progressDialog.setCanceledOnTouchOutside(false)

        binding.btnRegistrarV.setOnClickListener {
            validarInformacion()
        }
        cleanerError()
    }

    private fun validarInformacion(){
        limpiarErrores()

        nombre= binding.edtNombreV.text.toString().trim()
        email = binding.edtEmailV.text.toString().trim()
        contrasena=binding.edtContrasenaV.text.toString().trim()
        ccontrasena=binding.edtCcontrasenaV.text.toString().trim()

        when{
            nombre.isEmpty()->{
                binding.tilNombreV.error = "Ingrese su nombre"
                binding.edtNombreV.requestFocus() //que se quede focus en ese
            }
            email.isEmpty()->{
                binding.tilEmailV.error="Ingrese su email"
                binding.edtEmailV.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches()->{
                binding.tilEmailV.error="Email no válido"
                binding.edtEmailV.requestFocus()
            }
            contrasena.isEmpty()-> {
                binding.tilContrasenaV.error = "Ingrese su contraseña"
                binding.edtContrasenaV.requestFocus()
            }
            contrasena.length<6->{
                binding.tilContrasenaV.error = "Ingrese una contraseña de más de 6 caracteres"
                binding.edtContrasenaV.requestFocus()
            }
            ccontrasena.isEmpty()->{
                binding.tilCcontrasenaV.error= "Confirme su contraseña"
                binding.edtCcontrasenaV.requestFocus()
            }
            contrasena!=ccontrasena->{
                binding.tilCcontrasenaV.error= "Las contraseñas no coinciden"
                binding.edtCcontrasenaV.requestFocus()
            }
            else-> registrarVendedor()
        }
    }

    private fun cleanerError(){
        binding.edtNombreV.doAfterTextChanged { binding.tilNombreV.error=null }
        binding.edtEmailV.doAfterTextChanged { binding.tilEmailV.error=null }
        binding.edtContrasenaV.doAfterTextChanged { binding.tilContrasenaV.error=null }
        binding.edtCcontrasenaV.doAfterTextChanged { binding.tilCcontrasenaV.error=null }
    }

    private fun setLoading(cargado: Boolean){
        binding.overlayCargando.visibility = if (cargado) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnRegistrarV.isEnabled = !cargado
    }

    private fun limpiarErrores(){
        binding.tilNombreV.error=null
        binding.tilEmailV.error=null
        binding.tilContrasenaV.error = null
        binding.tilCcontrasenaV.error = null
    }

    private fun registrarVendedor(){
        //progressDialog.setMessage("Creando cuenta")
        //progressDialog.show()

        setLoading(true)

        firebaseAuth.createUserWithEmailAndPassword(email,contrasena)
            .addOnSuccessListener {
                insertarVendedorBD()
        }
            .addOnFailureListener { e->
                setLoading(false)
                Toast.makeText(this,"Falló el registro debido a ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertarVendedorBD(){
        //progressDialog.setMessage("Guardando vendedor...")

        val uidBD = firebaseAuth.uid
        val nombreBD = nombre
        val emailBD = email
        val tipoUsuario = "vendedor"
        val tiempoBD = Funciones().obtenerTiempoRegistro()

        val datosVendedor = HashMap<String, Any>()

        datosVendedor["uid"] = "$uidBD"
        datosVendedor["nombre"]= "$nombreBD"
        datosVendedor["email"]="$emailBD"
        datosVendedor["tipoUsuario"]= "vendedor"
        datosVendedor["tiempoRegistro"] = tiempoBD

        val references = FirebaseDatabase.getInstance().getReference("Usuarios")
        references.child(uidBD!!)     //ordenar por el uid
            .setValue(datosVendedor)
            .addOnSuccessListener {
                //progressDialog.dismiss() // cierra y oculta el diálogo del proceso activo
                setLoading(false)
                startActivity(Intent(this, MainActivityVendedor::class.java))
                finish()
            }
            .addOnFailureListener { e->
                //progressDialog.dismiss()
                setLoading(false)
                Toast.makeText(this,"Falló el registro en base de datos debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


