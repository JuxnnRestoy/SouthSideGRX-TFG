package com.example.southsidegrx_tfg.Modelos

data class Producto(
    var id:String="",
    var nombre:String="",
    var descripcion:String="",
    var categoria:String="",
    var precio:Double = 0.0,
    var precioDesc : Double = 0.0,
    var notaDesc:String="",
    var favorito: Boolean = false,
    var stock: Double=0.0
)