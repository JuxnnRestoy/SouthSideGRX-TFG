package com.example.southsidegrx_tfg.Modelos

data class Producto(
    var id:String="",
    var nombre:String="",
    var descripcion:String="",
    var categoria:String="",
    var precio:String = "",
    var precioDesc : String="",
    var notaDesc:String="",
    var favorito: Boolean = false,
    var stock: Double=0.0
)