package com.example.southsidegrx_tfg.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.southsidegrx_tfg.Modelos.ProductoCarrito
import com.example.southsidegrx_tfg.databinding.ItemProductoCompraBinding
import java.util.ArrayList

class ProductosCompraAdapter: RecyclerView.Adapter<ProductosCompraAdapter.HolderProductoCompra> {
    private lateinit var binding: ItemProductoCompraBinding
    private var mContext: Context
    private var productosArrayList: ArrayList<ProductoCarrito>

    constructor(mContext: Context, productosArrayList: ArrayList<ProductoCarrito>):super() {
        this.mContext = mContext
        this.productosArrayList = productosArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProductoCompra {
        binding = ItemProductoCompraBinding.inflate(LayoutInflater.from(mContext),parent,false)
        return HolderProductoCompra(binding.root)
    }

    override fun onBindViewHolder(holder: HolderProductoCompra, position: Int) {
        val modelo = productosArrayList[position]
        holder.tvNombre.text = modelo.nombre
        holder.tvCantidad.text = "Cantidad: ${modelo.cantidad}"
        holder.tvPrecio.text = "Precio: ${modelo.precio} CRD"
        holder.tvTotal.text = "Total: ${modelo.precioFinal} CRD"


    }

    override fun getItemCount(): Int {
        return productosArrayList.size
    }

    inner class HolderProductoCompra(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvNombre = binding.tvNombreProducto
        var tvCantidad = binding.tvCantidad
        var tvPrecio = binding.tvPrecio
        var tvTotal = binding.tvTotalProducto
    }

}