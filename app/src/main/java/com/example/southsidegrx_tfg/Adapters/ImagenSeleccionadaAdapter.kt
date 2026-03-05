package com.example.southsidegrx_tfg.Adapters

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.example.southsidegrx_tfg.Modelos.ModeloImagenSeleccionada
import com.example.southsidegrx_tfg.databinding.ItemImagenesSeleccionadasBinding
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.example.southsidegrx_tfg.R

class ImagenSeleccionadaAdapter(
    private val context: Context,
    private val imagenesSeleccionadaArrayList: ArrayList<ModeloImagenSeleccionada>
):Adapter<ImagenSeleccionadaAdapter.HolderImagenSeleccionada>() {
    private lateinit var binding: ItemImagenesSeleccionadasBinding
    //nos permite mostrar en pantalla cada elemento que contiene una lista y se actualiza
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSeleccionada {
        binding = ItemImagenesSeleccionadasBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderImagenSeleccionada(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImagenSeleccionada, position: Int) {
        val modelo = imagenesSeleccionadaArrayList[position]

        try {
            if(modelo.deInternet){
                Glide.with(context)
                    .load(modelo.imagenUrl)
                    .placeholder(R.drawable.ico_item_imagen)
                    .error(R.drawable.ico_item_imagen)
                    .into(holder.item_imagen)
            }else{
                Glide.with(context)
                    .load(modelo.imageUri)
                    .placeholder(R.drawable.ico_item_imagen)
                    .error(R.drawable.ico_item_imagen)
                    .into(holder.item_imagen)
            }
        }catch (e: Exception){
            holder.item_imagen.setImageResource(R.drawable.ico_item_imagen)
        }

        holder.btn_borrar.setOnClickListener {
            imagenesSeleccionadaArrayList.remove(modelo)
            notifyDataSetChanged()
        }
    }

    //muestra la información de un elemento de la lista dependiendo de su posición
    override fun getItemCount(): Int {
        return imagenesSeleccionadaArrayList.size
    }

    inner class HolderImagenSeleccionada(itemView: View): ViewHolder(itemView){
        var item_imagen = binding.itemImagen
        var btn_borrar = binding.borrarItem
    }
}