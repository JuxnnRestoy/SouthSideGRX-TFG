package com.example.southsidegrx_tfg.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.southsidegrx_tfg.Detalles.DetalleCompraActivity
import com.example.southsidegrx_tfg.Modelos.Compra
import com.example.southsidegrx_tfg.databinding.ItemCompraCBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComprasClienteAdapter: RecyclerView.Adapter<ComprasClienteAdapter.HolderCompraC> {
    private lateinit var binding: ItemCompraCBinding
    private var mContext: Context
    private var comprasArrayList: ArrayList<Compra>

    constructor(mContext: Context, comprasArrayList: ArrayList<Compra>):super() {
        this.mContext = mContext
        this.comprasArrayList = comprasArrayList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderCompraC {
        binding = ItemCompraCBinding.inflate(LayoutInflater.from(mContext),parent,false)
        return HolderCompraC(binding.root)
    }

    override fun onBindViewHolder(
        holder: HolderCompraC,
        position: Int
    ) {
        val modelo = comprasArrayList[position]

        holder.tvVendedor.text = "Vendedor: ${modelo.vendedorNombre}"
        holder.tvTotal.text = String.format(Locale.getDefault(), "Total: %.2f CRD", modelo.total)

        val fechaTxt = if(modelo.fecha > 0L){
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(modelo.fecha))
        }else{
            "-"
        }
        holder.tvFecha.text = "Fecha: $fechaTxt"

        holder.btnVerDetalle.setOnClickListener {
            val intent = Intent(mContext, DetalleCompraActivity::class.java)
            intent.putExtra("idCompra", modelo.idCompra)
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return comprasArrayList.size
    }

    inner class HolderCompraC(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvVendedor = binding.tvVendedorCompra
        var tvFecha = binding.tvFechaCompra
        var tvTotal = binding.tvTotalCompra
        var btnVerDetalle = binding.btnVerDetalle
    }
}