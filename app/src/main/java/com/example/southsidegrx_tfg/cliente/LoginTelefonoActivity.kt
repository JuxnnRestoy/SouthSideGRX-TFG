package com.example.southsidegrx_tfg.cliente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.Visibility
import com.example.southsidegrx_tfg.Funciones
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.ActivityLoginTelefonoBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class LoginTelefonoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginTelefonoBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var frt: PhoneAuthProvider.ForceResendingToken?=null
    private lateinit var mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mVerification:String?=null

    private var codigoTel =""
    private var numeroTel=""
    private var codTelnumTel =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginTelefonoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.rlTelefono.visibility= View.VISIBLE
        binding.rlCodigoVerificacion.visibility = View.GONE

        phoneLoginCallbacks()

        binding.btnEnviarCodigo.setOnClickListener {
            validarData()
        }
        binding.btnVerificarCodigo.setOnClickListener {
            val otp = binding.edtCodigoVerificacion.text.toString().trim()
            if(otp.isEmpty()){
                binding.edtCodigoVerificacion.error="Ingrese el código de verificación"
                binding.edtCodigoVerificacion.requestFocus()
            }else if(otp.length<6){
                binding.edtCodigoVerificacion.error="El código debe contener 6 caracteres"
                binding.edtCodigoVerificacion.requestFocus()
            }else{
                verificarCodigoTel(otp)
            }
        }
        binding.tvReenviarCodigo.setOnClickListener {
            if(frt!=null){
                reenviarCodigoVerificacion()
            }else{
                Toast.makeText(this,"No se puede reenviar el código de verificación",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarCodigoTel(otp:String){
        //setLoading(true)
        val credencial = PhoneAuthProvider.getCredential(mVerification!!,otp)
        signInWithPhoneAuthCredential(credencial)
    }

    private fun signInWithPhoneAuthCredential(credencial: PhoneAuthCredential){
        //setLoading(true)
        firebaseAuth.signInWithCredential(credencial)
            .addOnSuccessListener { authResult ->
                if(authResult.additionalUserInfo!!.isNewUser){
                    guardarInfo()
                }else{
                    startActivity(Intent(this, MainActivityCliente::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {

            }
    }

    private fun guardarInfo(){
        //setLoading(true)
        val uid = firebaseAuth.uid
        val tiempoReg = Funciones().obtenerTiempoRegistro()

        val datosCliente = HashMap<String,Any>()
        datosCliente["uid"]="$uid"
        datosCliente["nombre"] = ""
        datosCliente["telefono"]="$codTelnumTel"
        datosCliente["tiempoRegistro"]= tiempoReg
        datosCliente["imagen"] = ""
        datosCliente["tipoUsuario"] = "cliente"

        val referencia = FirebaseDatabase.getInstance().getReference("Usuarios")
        referencia.child(uid!!).setValue(datosCliente)
            .addOnSuccessListener {
                // setLoading(true)
                startActivity(Intent(this, MainActivityCliente::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                //setLoading(false)
                Toast.makeText(this,"Falló el registro en la base de datos debido a ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun reenviarCodigoVerificacion(){
        // setLoding(true)

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(codTelnumTel).setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this).setCallbacks(mCallBack)
            .setForceResendingToken(frt!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun validarData(){
        codigoTel = binding.telCodePicker.selectedCountryCodeWithPlus
        numeroTel = binding.edtTelefono.text.toString().trim()
        codTelnumTel = codigoTel+numeroTel

        if(numeroTel.isEmpty()){
            binding.edtTelefono.error="Ingrese número de teléfono"
            binding.edtTelefono.requestFocus()
        }else{
            verificarNumeroTelefono()
        }
    }

    private fun verificarNumeroTelefono(){
        // setLoding(true)

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(codTelnumTel).setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this).setCallbacks(mCallBack)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun phoneLoginCallbacks(){
        mCallBack = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                mVerification = verificationId
                frt = token

                //setLoadin false progressdialog dismiss
                binding.rlTelefono.visibility = View.GONE
                binding.rlCodigoVerificacion.visibility = View.VISIBLE

                Toast.makeText(this@LoginTelefonoActivity,"Enviando código ${codTelnumTel}",Toast.LENGTH_SHORT).show()
            }
            override fun onVerificationCompleted(phoneAuthCredencial: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredencial)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                //setLoading false progressdialog dismiss
                Toast.makeText(this@LoginTelefonoActivity,"Falló la verificación debido a ${e.message}",Toast.LENGTH_SHORT).show()
            }

        }
    }
}