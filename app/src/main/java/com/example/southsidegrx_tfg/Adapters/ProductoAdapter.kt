package com.example.southsidegrx_tfg.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.southsidegrx_tfg.Filtro.FiltroBuscarProductos
import com.example.southsidegrx_tfg.Filtro.FiltroProductos
import com.example.southsidegrx_tfg.Modelos.Producto
import com.example.southsidegrx_tfg.databinding.ItemProductoBinding
import com.example.southsidegrx_tfg.vendedor.Productos.AgregarProductoActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductoAdapter : RecyclerView.Adapter<ProductoAdapter.HolderProducto>, Filterable{

    private lateinit var binding: ItemProductoBinding
    private var mContext: Context
    var productosArrayList: ArrayList<Producto>
    private var filtroLista: ArrayList<Producto>
    private var filtro: Filter? = null

    constructor(mContext: Context, productosArrayList: ArrayList<Producto>) {
        this.mContext = mContext
        this.productosArrayList = productosArrayList
        filtroLista = productosArrayList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderProducto {
        binding = ItemProductoBinding.inflate(LayoutInflater.from(mContext),parent,false)
        return HolderProducto(binding.root)
    }

    override fun onBindViewHolder(holder: HolderProducto, position: Int
    ) {
        val modeloProducto = productosArrayList[position]

        //mostrar la información

        val nombre = modeloProducto.nombre
        val stock = modeloProducto.stock

        cargarPrimeraImagen(modeloProducto,holder)
        visualizarDescuento(modeloProducto,holder)

        holder.item_nombre_p.text = "${nombre}"
        holder.item_stock.text = "Stock: ${String.format("%.2f", stock)}"
        if(stock<=0.0){
            holder.item_stock.text="SIN STOCK"
            holder.item_stock.setTextColor(Color.RED)
        }


        holder.ib_eliminar.setOnClickListener {
            android.app.AlertDialog.Builder(mContext)
                .setTitle("Eliminar producto")
                .setMessage("¿Seguro que quieres eliminar ${modeloProducto.nombre}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    eliminarProducto(modeloProducto.id)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }


        holder.ib_editar.setOnClickListener {
            val intent = Intent(mContext, AgregarProductoActivity::class.java)
            intent.putExtra("Edicion",true) //putExtrra envia informacion adicional a ala ventana
            intent.putExtra("idProducto",modeloProducto.id)
            mContext.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return productosArrayList.size
    }
    private fun eliminarProducto(idProducto: String){
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto)
            .removeValue()
            .addOnSuccessListener {
                android.widget.Toast.makeText(mContext, "Producto eliminado", android.widget.Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(mContext, "No se pudo eliminar: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun visualizarDescuento(modeloProducto: Producto,holder: ProductoAdapter.HolderProducto){
        if(!modeloProducto.precioDesc.equals("0") && !modeloProducto.notaDesc.equals("")){
            // habilitar vistas
            holder.item_nota_p.visibility=View.VISIBLE
            holder.item_precio_desc_p.visibility = View.VISIBLE

            //set de la informacion
            holder.item_nota_p.text = "${modeloProducto.notaDesc}"
            holder.item_precio_desc_p.text = "${modeloProducto.precioDesc} CRD"
            holder.item_precio_p.text = "${modeloProducto.precio} CRD"
            holder.item_precio_p.paintFlags = holder.item_precio_p.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG //tachar precio original
        }else{
            //el producto no tiene descuento -> ocultar vistas
            holder.item_nota_p.visibility=View.GONE
            holder.item_precio_desc_p.visibility = View.GONE

            // set informacion
            holder.item_precio_p.text = "${modeloProducto.precio} CRD"
            holder.item_precio_p.paintFlags = holder.item_precio_p.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() //quitar tachado
        }
    }

    private fun cargarPrimeraImagen(modeloProducto: Producto,holder: ProductoAdapter.HolderProducto){
        val idProducto = modeloProducto.id

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).child("Imagenes")
            .limitToLast(1).addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds in snapshot.children){
                        val imagenUrl = "${ds.child("imagenUrl").value}"

                        try{
                            Glide.with(mContext).load(imagenUrl)
                                .placeholder(com.example.southsidegrx_tfg.R.drawable.ico_item_imagen)
                                .into(holder.imagenProducto)
                        }catch (e: Exception){

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun getFilter(): Filter? {
        if(filtro == null){
            filtro = FiltroBuscarProductos(this,filtroLista)
        }
        return filtro as FiltroBuscarProductos
    }

    inner class HolderProducto(itemView: android.view.View): RecyclerView.ViewHolder(itemView){
        var imagenProducto = binding.imagenProducto
        var item_nombre_p = binding.itemNombreP
        var item_precio_p = binding.itemPrecioP
        var item_precio_desc_p = binding.itemPrecioPDesc
        var item_nota_p = binding.itemNotaP
        var ib_editar = binding.ibEditar
        var item_stock = binding.itemStock
        var ib_eliminar = binding.ibEliminar

    }
}