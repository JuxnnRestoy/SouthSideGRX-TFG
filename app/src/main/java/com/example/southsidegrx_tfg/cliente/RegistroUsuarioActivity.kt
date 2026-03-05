 package com.example.southsidegrx_tfg.cliente

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.southsidegrx_tfg.Funciones
import com.example.southsidegrx_tfg.databinding.ActivityRegistroUsuarioBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

 class RegistroUsuarioActivity : AppCompatActivity() {

     private lateinit var binding: ActivityRegistroUsuarioBinding

     private lateinit var primaryAuth: FirebaseAuth
     private var secondaryAuth: FirebaseAuth? = null
     private var secondaryApp: FirebaseApp? = null

     private var imageUri: Uri? = null

     private var tipoUsuario = "cliente" // por defesto
     private var nombre = ""
     private var email = ""
     private var fechaNac = ""
     private var dni = ""
     private var pass = ""
     private var pass2 = ""

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         binding = ActivityRegistroUsuarioBinding.inflate(layoutInflater)
         setContentView(binding.root)
         Funciones().ocultarTecladoAlTocarFuera(this,binding.root)

         primaryAuth = FirebaseAuth.getInstance()

         // Recojo tipo usuario desde FragmentUsuariosV
         tipoUsuario = (intent.getStringExtra("tipoUsuario") ?: "cliente").lowercase()
         binding.tvTipoUsuario.text = "Tipo usuario: $tipoUsuario"

         inicializarAuthSecundario()

         binding.imgUsuario.setOnClickListener { seleccionarImagen() }
         binding.btnGuardarUsuario.setOnClickListener { validarYCrear() }
     }

     private fun setLoading(cargando: Boolean) {
         binding.overlayCargando.visibility =
             if (cargando) android.view.View.VISIBLE else android.view.View.GONE
         binding.btnGuardarUsuario.isEnabled = !cargando
     }

     private fun inicializarAuthSecundario() {
         val options = FirebaseApp.getInstance().options

         secondaryApp = try {
             FirebaseApp.initializeApp(this, options, "SecondaryApp")
         } catch (e: IllegalStateException) {
             FirebaseApp.getInstance("SecondaryApp")
         }

         secondaryAuth = FirebaseAuth.getInstance(secondaryApp!!)
     }

     private fun seleccionarImagen() {
         ImagePicker.with(this)
             .crop()
             .compress(1024)
             .maxResultSize(1080, 1080)
             .createIntent { intent -> resultadoImg.launch(intent) }
     }

     private val resultadoImg =
         registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
             if (resultado.resultCode == Activity.RESULT_OK) {
                 val data = resultado.data
                 imageUri = data?.data
                 binding.imgUsuario.setImageURI(imageUri)
             } else {
                 Toast.makeText(this, "Acción cancelada", Toast.LENGTH_SHORT).show()
             }
         }

     private fun validarYCrear() {
         nombre = binding.edtNombre.text?.toString()?.trim().orEmpty()
         email = binding.edtEmail.text?.toString()?.trim().orEmpty()
         fechaNac = binding.edtFechaNac.text?.toString()?.trim().orEmpty()
         dni = binding.edtDni.text?.toString()?.trim().orEmpty()
         pass = binding.edtPass.text?.toString()?.trim().orEmpty()
         pass2 = binding.edtPasss.text?.toString()?.trim().orEmpty()

         // Limpio errores
         binding.tilNombre.error = null
         binding.tilEmail.error = null
         binding.tilFechaNac.error = null
         binding.tilDni.error = null
         binding.tilPass.error = null
         binding.tilPasss.error = null

         when {
             imageUri == null -> {
                 Toast.makeText(this, "Selecciona una foto obligatoria", Toast.LENGTH_SHORT).show()
             }
             nombre.isEmpty() -> {
                 binding.tilNombre.error = "Ingrese nombre"
                 binding.edtNombre.requestFocus()
             }
             email.isEmpty() -> {
                 binding.tilEmail.error = "Ingrese email"
                 binding.edtEmail.requestFocus()
             }
             !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                 binding.tilEmail.error = "Email no válido"
                 binding.edtEmail.requestFocus()
             }
             fechaNac.isEmpty() -> {
                 binding.tilFechaNac.error = "Ingrese fecha nacimiento"
                 binding.edtFechaNac.requestFocus()
             }
             dni.isEmpty() -> {
                 binding.tilDni.error = "Ingrese DNI"
                 binding.edtDni.requestFocus()
             }
             pass.isEmpty() -> {
                 binding.tilPass.error = "Ingrese contraseña"
                 binding.edtPass.requestFocus()
             }
             pass.length < 6 -> {
                 binding.tilPass.error = "Mínimo 6 caracteres"
                 binding.edtPass.requestFocus()
             }
             pass2.isEmpty() -> {
                 binding.tilPasss.error = "Confirme contraseña"
                 binding.edtPasss.requestFocus()
             }
             pass != pass2 -> {
                 binding.tilPasss.error = "No coinciden"
                 binding.edtPasss.requestFocus()
             }
             else -> crearUsuarioAuthSecundario()
         }
     }

     private fun crearUsuarioAuthSecundario() {
         val auth = secondaryAuth ?: run {
             Toast.makeText(this, "Auth secundario no disponible", Toast.LENGTH_SHORT).show()
             return
         }

         setLoading(true)

         auth.createUserWithEmailAndPassword(email, pass)
             .addOnSuccessListener { result ->
                 val uidNuevo = result.user?.uid
                 if (uidNuevo == null) {
                     setLoading(false)
                     Toast.makeText(this, "No se pudo obtener UID", Toast.LENGTH_SHORT).show()
                     return@addOnSuccessListener
                 }

                 subirFotoYGuardarBD(uidNuevo)
             }
             .addOnFailureListener { e ->
                 setLoading(false)
                 Toast.makeText(this, "Error registro: ${e.message}", Toast.LENGTH_SHORT).show()
             }
     }

     private fun subirFotoYGuardarBD(uidNuevo: String) {
         val uri = imageUri ?: run {
             setLoading(false)
             Toast.makeText(this, "Foto obligatoria", Toast.LENGTH_SHORT).show()
             return
         }

         val ruta = "Usuarios/$uidNuevo/perfil.jpg"
         val storageRef = FirebaseStorage.getInstance().reference.child(ruta)

         storageRef.putFile(uri)
             .addOnSuccessListener { task ->
                 task.storage.downloadUrl
                     .addOnSuccessListener { url ->
                         guardarUsuarioEnBD(uidNuevo, url.toString())
                     }
                     .addOnFailureListener { e ->
                         setLoading(false)
                         Toast.makeText(this, "URL imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                     }
             }
             .addOnFailureListener { e ->
                 setLoading(false)
                 Toast.makeText(this, "Subida imagen: ${e.message}", Toast.LENGTH_SHORT).show()
             }
     }

     private fun guardarUsuarioEnBD(uidNuevo: String, imagenUrl: String) {
         val tiempo = Funciones().obtenerTiempoRegistro()

         val datos = HashMap<String, Any>()
         datos["uid"] = uidNuevo
         datos["nombre"] = nombre
         datos["email"] = email
         datos["tipoUsuario"] = tipoUsuario
         datos["fechaNac"] = fechaNac
         datos["dni"] = dni
         datos["imagen"] = imagenUrl
         datos["tiempoRegistro"] = tiempo

         val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
         ref.child(uidNuevo)
             .setValue(datos)
             .addOnSuccessListener {
                 //cierro sesión del auth secundario (no del vendedor)
                 secondaryAuth?.signOut()
                 setLoading(false)
                 Toast.makeText(this, "Usuario creado", Toast.LENGTH_SHORT).show()
                 finish()
             }
             .addOnFailureListener { e ->
                 secondaryAuth?.signOut()
                 setLoading(false)
                 Toast.makeText(this, "BD: ${e.message}", Toast.LENGTH_SHORT).show()
             }
     }

}

