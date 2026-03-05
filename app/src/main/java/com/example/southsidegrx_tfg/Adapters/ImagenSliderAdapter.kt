package com.example.southsidegrx_tfg.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.southsidegrx_tfg.Modelos.ImgSlider
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.ItemImagenSliderBinding
import com.google.android.material.imageview.ShapeableImageView

class ImagenSliderAdapter : RecyclerView.Adapter<ImagenSliderAdapter.HolderImagenSlider>{
    private lateinit var binding: ItemImagenSliderBinding
    private var context: Context
    private var imagenArrayList: ArrayList<ImgSlider>

    constructor(context: Context, imagenArrayList: ArrayList<ImgSlider>) {
        this.context = context
        this.imagenArrayList = imagenArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSlider {
        binding = ItemImagenSliderBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderImagenSlider(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImagenSlider, position: Int
    ) {
        val modeloImagenSlider = imagenArrayList[position]
        val imagenUrl = modeloImagenSlider.imagenUrl
        val imagenContador = "${position+1}/${imagenArrayList.size}" //2/4 3/4 etc

        holder.imagenContadorTv.text = imagenContador

        try {
            Glide.with(context).load(imagenUrl).placeholder(R.drawable.ico_agregar_producto).into(holder.imagenSIV)
        }catch (e: Exception) {

        }

    }

    override fun getItemCount(): Int {
        return imagenArrayList.size
    }

    inner class HolderImagenSlider(itemView: View): RecyclerView.ViewHolder(itemView){
        var imagenSIV: ShapeableImageView = binding.imagenSIV
        var imagenContadorTv: TextView = binding.imagenContadorTv
    }
}