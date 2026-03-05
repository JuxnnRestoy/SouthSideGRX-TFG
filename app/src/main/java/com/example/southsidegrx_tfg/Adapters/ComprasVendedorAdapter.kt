package com.example.southsidegrx_tfg.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.southsidegrx_tfg.Detalles.DetalleCompraActivity
import com.example.southsidegrx_tfg.Modelos.Compra
import com.example.southsidegrx_tfg.databinding.ItemCompraVBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComprasVendedorAdapter: RecyclerView.Adapter<ComprasVendedorAdapter.HolderCompraV> {

    private lateinit var binding: ItemCompraVBinding
    private var mContext: Context
    private var comprasArrayList: ArrayList<Compra>

    constructor(mContext: Context, comprasArrayList: ArrayList<Compra>) : super() {
        this.mContext = mContext
        this.comprasArrayList = comprasArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCompraV {
        binding = ItemCompraVBinding.inflate(LayoutInflater.from(mContext),parent,false)
        return HolderCompraV(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCompraV, position: Int) {
        val modelo = comprasArrayList[position]
        holder.tvClienteCompra.text = modelo.clienteNombre
        holder.tvTotalCompra.text = modelo.total.toString()+" CRD"

        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(modelo.fecha))
        holder.tvFechaCompra.text = fecha

        holder.btnVerDetalle.setOnClickListener {
            val intent = Intent(mContext, DetalleCompraActivity::class.java)
            intent.putExtra("idCompra",modelo.idCompra)
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return comprasArrayList.size
    }

    inner class HolderCompraV(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvClienteCompra = binding.tvClienteCompra
        var tvFechaCompra = binding.tvFechaCompra
        var tvTotalCompra = binding.tvTotalCompra
        var btnVerDetalle = binding.btnVerDetalle
    }

}