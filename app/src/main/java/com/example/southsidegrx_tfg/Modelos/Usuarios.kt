package com.example.southsidegrx_tfg.Modelos

data class Usuarios(
    var uid:String="",
    var nombre:String="",
    var imagen : String="",
    var email:String="",
    var dni:String="",
    var fechaNacimiento:String="",
    var tipoUsuario:String="",
    var tiempoRegistro:Long = 0L,
)