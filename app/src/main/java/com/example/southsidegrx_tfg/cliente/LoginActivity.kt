package com.example.southsidegrx_tfg.cliente

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