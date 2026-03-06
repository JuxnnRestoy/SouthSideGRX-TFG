package com.example.southsidegrx_tfg.Adapters

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.southsidegrx_tfg.Modelos.ProductoCarrito
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.ItemCarritoCompraBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CarritoAdapter: RecyclerView.Adapter<CarritoAdapter.HolderProductoCarrito> {

    private lateinit var binding: ItemCarritoCompraBinding
    private var mContext: Context
    var productosArrayList: ArrayList<ProductoCarrito>
    private var firebaseAuth: FirebaseAuth

    private var uidCarrito:String=""

    private val stockCache = HashMap<String,Double>()

    constructor(mContext: Context, productosArrayList: ArrayList<ProductoCarrito>,uidCarrito:String):super(){
        this.mContext = mContext
        this.productosArrayList = productosArrayList
        this.firebaseAuth = FirebaseAuth.getInstance()
        this.uidCarrito = uidCarrito
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProductoCarrito {
        binding = ItemCarritoCompraBinding.inflate(LayoutInflater.from(mContext),parent,false)
        return HolderProductoCarrito(binding.root)
    }

    override fun onBindViewHolder(holder: HolderProductoCarrito, position: Int) {
        val modeloProductoCarrito = productosArrayList[position]

        val nombre = modeloProductoCarrito.nombre
        //val cantidad = modeloProductoCarrito.cantidad
        //val precioFinal = modeloProductoCarrito.precioFinal
        //var precio = modeloProductoCarrito.precio
        //var precioDesc = modeloProductoCarrito.precioDesc


        holder.nombrePCar.text = nombre
        //holder.cantidadPCar.setText(cantidad.toString())

        cargarPrimeraImg(modeloProductoCarrito,holder)

        visualizarDescuento(modeloProductoCarrito,holder)

        holder.btnEliminarCarrito.setOnClickListener { eliminarProductoCarrito(mContext,modeloProductoCarrito.idProducto) }

        //filtro decimales
        holder.cantidadPCar.filters = arrayOf(filtro2Decimales())
        // quito watcher anterior
        holder.watcher?.let{holder.cantidadPCar.removeTextChangedListener(it)}
        // seteo de cantidad
        holder.cantidadPCar.setText(modeloProductoCarrito.cantidad.toString())

        val idProducto = modeloProductoCarrito.idProducto
        //creo nuevo watcher
        val watcher = object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val texto = holder.cantidadPCar.text.toString().trim()
                if(texto.isEmpty()){
                    return
                }

                val cantidadNueva = texto.replace(',','.').toDoubleOrNull()
                if(cantidadNueva==null || cantidadNueva<=0.0){
                    holder.cantidadPCar.error="Cantidad inválida"
                    return
                }

                leerStock(idProducto){stockDisponible ->
                    if(cantidadNueva>stockDisponible){
                        holder.cantidadPCar.error="Stock disponible: $stockDisponible"
                        Toast.makeText(mContext,"No hay suficiente stock",Toast.LENGTH_SHORT).show()

                        val valorAnterior = modeloProductoCarrito.cantidad.toString()
                        holder.cantidadPCar.removeTextChangedListener(this)
                        holder.cantidadPCar.setText(valorAnterior)
                        holder.cantidadPCar.setSelection(valorAnterior.length)
                        holder.cantidadPCar.addTextChangedListener(this)

                        return@leerStock
                    }
                    //recalculo precio final con o sin descuento
                    val precioUnitario = if(modeloProductoCarrito.precioDesc!=0.0){
                        modeloProductoCarrito.precioDesc
                        //(modeloProductoCarrito.precioDesc ?: "0").toDoubleOrNull()?:0.0
                    }else{
                        modeloProductoCarrito.precio
                        //(modeloProductoCarrito.precio ?: "0").toDoubleOrNull()?:0.0
                    }
                    //val cantidad = cantidadNueva!!
                    val nuevoPrecioFinal = precioUnitario*cantidadNueva

                    //actualizo UI local
                    holder.precioFinalPCar.text = String.format("%.2f CRD", nuevoPrecioFinal)
                    if(modeloProductoCarrito.precioDesc!=0.0){
                        val nuevoPrecioOriginal = modeloProductoCarrito.precio*cantidadNueva
                        holder.precioOriginalPCar.text = String.format("%.2f CRD", nuevoPrecioOriginal)

                    }
                    //actualizo modelo de memoria para que al reciclar no vuelva alos valores viejos
                    modeloProductoCarrito.cantidad = cantidadNueva
                    modeloProductoCarrito.precioFinal = nuevoPrecioFinal

                    //actualizo Firebase en tiempo real
                    actualizarCantidadBD(modeloProductoCarrito.idProducto,cantidadNueva,nuevoPrecioFinal)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.cantidadPCar.addTextChangedListener(watcher)
        holder.watcher = watcher
    }

    override fun getItemCount(): Int {
        return productosArrayList.size
    }

    private fun leerStock(idProducto:String, onResult:(Double)->Unit){
        val cached = stockCache[idProducto]
        if(cached!=null){
            onResult(cached)
            return
        }
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
            .child(idProducto).child("stock")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val stock = snapshot.getValue(Double::class.java)?:0.0
                stockCache[idProducto] = stock
                onResult(stock)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(0.0)
            }

        })

    }

    private fun actualizarCantidadBD(idProducto: String, cantidadNueva: Double, nuevoPrecioFinal: Double){
        //val uid = firebaseAuth.uid?:return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        val hashMap = HashMap<String,Any>()
        hashMap["cantidad"] = cantidadNueva
        hashMap["precioFinal"] = nuevoPrecioFinal
        ref.child(uidCarrito).child("CarritoCompras").child(idProducto)
            .updateChildren(hashMap)
    }


    private fun filtro2Decimales(): InputFilter{
        val regex = Regex("^\\d*(?:[.,]\\d{0,2})?$")
        return InputFilter{ source, start, end, dest, dstart, dend ->
            val builder = StringBuilder(dest)
                .replace(dstart,dend,source.subSequence(start,end).toString()).toString()
            if(builder.isEmpty()) return@InputFilter null
            if(regex.matches(builder)) null else ""
        }
    }

    private fun eliminarProductoCarrito(mContext: Context, idProducto: String){
        val firebaseAuth = FirebaseAuth.getInstance()

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidCarrito).child("CarritoCompras").child(idProducto)
            .removeValue().addOnSuccessListener {
                Toast.makeText(mContext,"Producto eliminado del carrito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(mContext,"No se ha podido eliminar el producto del carrito", Toast.LENGTH_SHORT).show()
            }
    }

    private fun visualizarDescuento(modeloProductoCarrito: ProductoCarrito, holder: HolderProductoCarrito){
        val cantidad = modeloProductoCarrito.cantidad

        if(modeloProductoCarrito.precioDesc!=0.0){
            holder.precioFinalPCar.text = String.format("%.2f CRD",modeloProductoCarrito.precioFinal)
            holder.precioOriginalPCar.text = String.format("%.2f CRD",modeloProductoCarrito.precio*cantidad)
            holder.precioOriginalPCar.paintFlags = holder.precioOriginalPCar.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }else{
            holder.precioOriginalPCar.visibility = View.GONE
            holder.precioFinalPCar.text = String.format("%.2f CRD",modeloProductoCarrito.precioFinal)
        }
    }

    private fun cargarPrimeraImg(modeloProductoCarrito: ProductoCarrito, holder: HolderProductoCarrito) {
        val idProducto = modeloProductoCarrito.idProducto

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).child("Imagenes")
            .limitToFirst(1).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds in snapshot.children){
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try{
                            Glide.with(mContext).load(imagenUrl).placeholder(R.drawable.ico_producto)
                                .into(holder.imagenProdCar)
                        }catch (e: Exception){
                            // imagen x defecto aunque arriba tenemos el placeholder

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    inner class HolderProductoCarrito(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // vistas de item carrito
        var imagenProdCar = binding.imagenProdCar
        var nombrePCar = binding.nombrePCar
        var precioFinalPCar = binding.precioFinalPCar
        var precioOriginalPCar = binding.precioOriginalPCar
        var cantidadPCar = binding.edtCantidadProductosCar
        var btnEliminarCarrito = binding.btnEliminarCarrito

        //quitar el watcher al reciclar
        var watcher: TextWatcher?=null
    }

}