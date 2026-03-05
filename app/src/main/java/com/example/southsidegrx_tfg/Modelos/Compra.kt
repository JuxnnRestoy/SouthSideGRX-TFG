package com.example.southsidegrx_tfg.Modelos

data class Compra(
    var idCompra:String="",
    var clienteUid:String="",
    var clienteNombre: String="",
    var vendedorUid:String="",
    var vendedorNombre:String="",
    var total:Double=0.0,
    var fecha:Long=0,
)