package com.example.southsidegrx_tfg.Adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.southsidegrx_tfg.Modelos.Categoria
import com.example.southsidegrx_tfg.databinding.ItemCategoriaVBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class CategoriasAdapter(
    private val mContext: Context,
    private val categoriasArrayList: ArrayList<Categoria>
) : RecyclerView.Adapter<CategoriasAdapter.HolderCategoria>() {

    private lateinit var binding: ItemCategoriaVBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategoria {
        binding = ItemCategoriaVBinding.inflate(
            LayoutInflater.from(mContext),
            parent,
            false
        )
        return HolderCategoria(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategoria, position: Int) {

        val modelo = categoriasArrayList[position]

        // Mostrar nombre
        holder.itemNombre.text = modelo.categoria

        // Eliminar categoría
        holder.btnBorrar.setOnClickListener {

            var builder = AlertDialog.Builder(mContext)
            builder.setTitle("Eliminar categoría")
            builder.setMessage("¿Estás seguro de eliminar esta categoría?")
                .setPositiveButton("Confirmar"){a,d ->
                    eliminarCategoria(modelo,holder)
            }
                .setNegativeButton("Cancelar"){a,d->
                    a.dismiss()
                }
            builder.show()
        }
    }

    private fun eliminarCategoria(modelo: Categoria,holder: CategoriasAdapter.HolderCategoria){
        val idCat = modelo.id
        val ref = FirebaseDatabase.getInstance().getReference("Categorias")
        ref.child(idCat).removeValue()
            .addOnSuccessListener {
                Toast.makeText(mContext,"Categoría eliminada}", Toast.LENGTH_SHORT).show()
                eliminarImagenCategoria(idCat)
            }
            .addOnFailureListener {e->
                Toast.makeText(mContext,"No se eliminó la categoría debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarImagenCategoria(idCat:String) {
        val nombreImg = idCat
        val rutaImagen = "Categorias/$nombreImg"
        val storageRef = FirebaseStorage.getInstance().getReference(rutaImagen)
        storageRef.delete()
            .addOnFailureListener {
                Toast.makeText(
                    mContext,
                    "Se eliminó la imagen de la categoría",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(mContext,"${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return categoriasArrayList.size
    }

    inner class HolderCategoria(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var itemNombre = binding.itemNombreCV
        var btnBorrar = binding.icoBorrarItem
    }
}
