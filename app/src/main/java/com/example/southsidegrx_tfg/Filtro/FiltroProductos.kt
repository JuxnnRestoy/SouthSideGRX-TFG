package com.example.southsidegrx_tfg.Filtro

import android.widget.Filter
import com.example.southsidegrx_tfg.Adapters.ProductosClienteAdapter
import com.example.southsidegrx_tfg.Modelos.Producto
import java.util.Locale

class FiltroProductos(
    private val adapter: ProductosClienteAdapter,
    private val filtroLista: ArrayList<Producto>
): Filter() {

    override fun performFiltering(filtro: CharSequence?): FilterResults {
        var filtro = filtro
        val resultados = FilterResults()

        if(!filtro.isNullOrEmpty()){
            filtro = filtro.toString().uppercase(Locale.getDefault())
            val filtroProducto = ArrayList<Producto>()
            for(i in filtroLista.indices){
                if(filtroLista[i].nombre.uppercase(Locale.getDefault()).contains(filtro)){
                    filtroProducto.add(filtroLista[i])
                }
            }
            resultados.count = filtroProducto.size
            resultados.values= filtroProducto
        }else{
            resultados.count = filtroLista.size
            resultados.values = filtroLista
        }
        return resultados
    }

    override fun publishResults(filtro: CharSequence?, resultados: FilterResults) {
        adapter.productosArrayList = resultados.values as ArrayList<Producto>
        adapter.notifyDataSetChanged()
    }
}