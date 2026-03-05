package com.example.southsidegrx_tfg.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.southsidegrx_tfg.Modelos.Categoria
import com.example.southsidegrx_tfg.cliente.ProductosCliente.ProductosCatCActivity
import com.example.southsidegrx_tfg.databinding.ItemCategoriaCBinding

class CategoriasClienteAdapter(
    private var mContext: Context,
    private var categoriaArrayList: ArrayList<Categoria>
): RecyclerView.Adapter<CategoriasClienteAdapter.HolderCategoriaCliente>() {

    private lateinit var binding: ItemCategoriaCBinding


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderCategoriaCliente {
        binding = ItemCategoriaCBinding.inflate(LayoutInflater.from(mContext),parent,false)
        return HolderCategoriaCliente(binding.root)

    }

    override fun onBindViewHolder(
        holder: HolderCategoriaCliente,
        position: Int
    ) {
        val modelo = categoriaArrayList[position]

        holder.item_nombre_c_c.text = modelo.categoria

        Glide.with(mContext).load(modelo.imagenUrl).into(holder.item_img_cat)

        //Ver productos de una categoría
        holder.item_ver_productos.setOnClickListener {
            val intent = Intent(mContext, ProductosCatCActivity::class.java)
            intent.putExtra("nombreCat",modelo.categoria)
            Toast.makeText(mContext,"Categoría seleccionada: ${modelo.categoria}",Toast.LENGTH_SHORT).show()
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return categoriaArrayList.size
    }

    inner class HolderCategoriaCliente(itemView: View): RecyclerView.ViewHolder(itemView){
        var item_nombre_c_c = binding.itemNombreCC
        var item_img_cat = binding.imagenCateg
        var item_ver_productos = binding.itemVerProductos
    }
}