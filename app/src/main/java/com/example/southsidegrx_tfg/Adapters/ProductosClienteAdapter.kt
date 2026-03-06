package com.example.southsidegrx_tfg.Adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.southsidegrx_tfg.Detalles.DetalleProductoActivity
import com.example.southsidegrx_tfg.Filtro.FiltroProductos
import com.example.southsidegrx_tfg.Funciones
import com.example.southsidegrx_tfg.Modelos.Producto
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.ItemProductoClienteBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductosClienteAdapter: RecyclerView.Adapter<ProductosClienteAdapter.HolderProducto>,
    Filterable {

    private lateinit var binding: ItemProductoClienteBinding
    private var mContext: Context
    var productosArrayList: ArrayList<Producto>
    private var filtroLista: ArrayList<Producto>
    private var filtro: FiltroProductos? = null
    private var firebaseAuth: FirebaseAuth


    constructor(mContext: Context, productosArrayList: ArrayList<Producto>) {
        this.mContext = mContext
        this.productosArrayList = productosArrayList
        this.filtroLista = productosArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderProducto {
        binding = ItemProductoClienteBinding.inflate(LayoutInflater.from(mContext),parent,false)
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
        comprobarFavorito(modeloProducto,holder)

        holder.item_nombre_p.text = "${nombre}"
        holder.item_stock.text = "Stock: ${String.format("%.2f", stock)}"
        if(stock<=0.0){
            holder.itemView.isEnabled = false
            holder.itemView.alpha = 0.5f
            holder.itemView.setOnClickListener(null)
            holder.agregar_carrito.isEnabled=false
            holder.agregar_carrito.alpha=0.5f
            holder.item_stock.text="SIN STOCK"
            holder.item_stock.setTextColor(Color.RED)
        }else{
            holder.itemView.isEnabled = true
            holder.itemView.alpha = 1f
            holder.agregar_carrito.isEnabled=true
            holder.agregar_carrito.alpha=1f
        }

        // evento al presionar el imagebutton favorito
        holder.ib_fav.setOnClickListener {
            val favorito = modeloProducto.favorito
            if (favorito){
                Funciones().eliminarProductoFavorito(mContext,modeloProducto.id)
            }else{
                Funciones().agregarProductoFavorito(mContext,modeloProducto.id)
            }
        }

        // evento para ir a la activity de detalle
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, DetalleProductoActivity::class.java)
            intent.putExtra("idProducto",modeloProducto.id)
            mContext.startActivity(intent)
        }

        // agregar al carrito el producto seleccionado
        holder.agregar_carrito.setOnClickListener {
            verCarrito(modeloProducto)
        }

    }

    override fun getItemCount(): Int {
        return productosArrayList.size
    }

    var coste:Double = 0.0
    var costeFinal :Double = 0.0
    var cantidadProducto: Double = 0.0
    private fun verCarrito(modeloProducto: Producto){
        // declaro las visatas que hay en carrito
        var imagenSIV: ShapeableImageView
        var nombreTv: TextView
        var descripcionTv: TextView
        var notaDescTv: TextView
        var precioOriginalTv: TextView
        var precioDescTv: TextView
        var precioFinalTv: TextView
        var cantidadEt: EditText
        var btnAgregarCarrito: MaterialButton

        val dialog = Dialog(mContext)
        dialog.setContentView(R.layout.carrito_compras) // hacemos la referencia a la vista (carrito de compras)

        imagenSIV = dialog.findViewById(R.id.imagenProdCar)
        nombreTv = dialog.findViewById(R.id.nombrePCar)
        descripcionTv = dialog.findViewById(R.id.decripcionPCar)
        notaDescTv = dialog.findViewById(R.id.notaDescPCar)
        precioOriginalTv = dialog.findViewById(R.id.precioOriginalPCar)
        precioDescTv = dialog.findViewById(R.id.precioDescPCar)
        precioFinalTv = dialog.findViewById(R.id.precioFinalPCar)
        cantidadEt = dialog.findViewById(R.id.edtCantidadProductosCar)
        cantidadEt.filters = arrayOf(filtro2Decimales())
        btnAgregarCarrito = dialog.findViewById(R.id.btnAgregarCarrito)

        // obtengo los datos del modelo
        val productoId = modeloProducto.id
        val nombre = modeloProducto.nombre
        val descripcion = modeloProducto.descripcion
        val precio = modeloProducto.precio
        val precioDesc = modeloProducto.precioDesc
        val notaDesc = modeloProducto.notaDesc

        if(precioDesc > 0.0 && notaDesc.isNotEmpty()){
            // El producto si tiene descuento
            notaDescTv.visibility = View.VISIBLE
            precioDescTv.visibility = View.VISIBLE

            notaDescTv.setText(notaDesc)
            precioDescTv.setText(String.format("%.2f CRD", precioDesc))
            precioOriginalTv.setText(String.format("%.2f CRD", precio))
            precioOriginalTv.paintFlags = precioOriginalTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            coste = precioDesc //precio almacena el precio con descuento
        }else{
            // El producto no tiene descuento
            precioOriginalTv.setText(String.format("%.2f CRD", precio))
            precioOriginalTv.paintFlags = precioOriginalTv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            coste = precio //almacena el precio original

        }

        // set de la información
        nombreTv.setText(nombre)
        descripcionTv.setText(descripcion)

        costeFinal = coste
        cantidadProducto = 1.0

        // inicializo edit text y total
        cantidadEt.setText(cantidadProducto.toString())
        precioFinalTv.text = costeFinal.toString()

        // recalculo cuando el usuario escriba
        cantidadEt.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val texto = cantidadEt.text.toString().trim()
                if(texto.isEmpty()){
                    return
                }
                val textoNormalizado = texto.replace(',','.')
                val cantidad = textoNormalizado.toDoubleOrNull()

                if(cantidad == null || cantidad<=0.0){
                    cantidadEt.error="Cantidad inválida"
                    return
                }

                cantidadProducto = cantidad
                costeFinal = coste * cantidadProducto

                precioFinalTv.text = String.format("%.2f CRD",costeFinal)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        cargarImg(productoId,imagenSIV)

        btnAgregarCarrito.setOnClickListener {
            agregarCarrito(mContext,modeloProducto,costeFinal,cantidadProducto)
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(true) // cuando se visualice el carrito y pulsemos fuera de él, se cerrará
    }

    private fun agregarCarrito(mContext: Context,modeloProducto: Producto,costeFinal: Double,cantidadProducto: Double){
        val firebaseAuth = FirebaseAuth.getInstance()
        if(cantidadProducto>modeloProducto.stock){
            Toast.makeText(mContext,"No hay suficiente stock",Toast.LENGTH_SHORT).show()
            return
        }

        val hashMap = HashMap<String,Any>()

        hashMap["idProducto"] = modeloProducto.id
        hashMap["nombre"] = modeloProducto.nombre
        hashMap["precio"] = modeloProducto.precio
        hashMap["precioDesc"] = modeloProducto.precioDesc
        hashMap["precioFinal"] = costeFinal
        hashMap["cantidad"] = cantidadProducto


        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("CarritoCompras").child(modeloProducto.id)
            .setValue(hashMap).addOnSuccessListener {
                Toast.makeText(mContext,"Producto agregado al carrito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(mContext,"No se ha podido agregar el producto al carrito", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarImg(productoId: String,imagenSIV: ShapeableImageView){
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(productoId).child("Imagenes")
            .limitToFirst(1).addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds in snapshot.children){
                        // extraigo la url de la primera imagen
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try{
                            Glide.with(mContext).load(imagenUrl)
                                .placeholder(R.drawable.ico_producto)
                                .into(imagenSIV)
                        }catch (e: Exception){

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


    private fun filtro2Decimales(): InputFilter{
        val regex = Regex("^\\d*(?:[.,]\\d{0,2})?$") //0.5 4.2 etc
        return InputFilter{ source, start, end, dest, dstart, dend ->
            val builder = StringBuilder(dest).replace(dstart,dend,source.subSequence(start,end).toString()).toString()

            //permitir vacio (en caso de borrar que no se cierre)
            if(builder.isEmpty()){return@InputFilter null}

            //permitir solo 0.x o numeros normales con hasta 2 dec
            if(regex.matches(builder)) null else ""
        }
    }

    private fun comprobarFavorito(modeloProducto: Producto,holder: ProductosClienteAdapter.HolderProducto){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(modeloProducto.id)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorito = snapshot.exists()
                    modeloProducto.favorito = favorito
                    if(favorito){
                        holder.ib_fav.setImageResource(R.drawable.ico_favorito)
                    }else{
                        holder.ib_fav.setImageResource(R.drawable.ico_no_favorito)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
    private fun visualizarDescuento(modeloProducto: Producto,holder: ProductosClienteAdapter.HolderProducto){
        if(modeloProducto.precioDesc > 0.0 && modeloProducto.notaDesc.isNotEmpty()){
            // habilitar vistas
            holder.item_nota_p.visibility=View.VISIBLE
            holder.item_precio_desc_p.visibility = View.VISIBLE

            //set de la informacion
            holder.item_nota_p.text = "${modeloProducto.notaDesc}"
            holder.item_precio_desc_p.text = String.format("%.2f CRD", modeloProducto.precioDesc)
            holder.item_precio_p.text = String.format("%.2f CRD", modeloProducto.precio)
            holder.item_precio_p.paintFlags = holder.item_precio_p.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG //tachar precio original
        }else{
            //el producto no tiene descuento -> ocultar vistas
            holder.item_nota_p.visibility=View.GONE
            holder.item_precio_desc_p.visibility = View.GONE

            // set informacion
            holder.item_precio_p.text = String.format("%.2f CRD", modeloProducto.precio)
            holder.item_precio_p.paintFlags = holder.item_precio_p.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() //quitar tachado
        }
    }

    private fun cargarPrimeraImagen(modeloProducto: Producto,holder: ProductosClienteAdapter.HolderProducto){
        val idProducto = modeloProducto.id

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).child("Imagenes")
            .limitToLast(1).addListenerForSingleValueEvent(object: ValueEventListener {
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
            filtro = FiltroProductos(this,filtroLista)
        }
        return filtro as FiltroProductos
    }

    inner class HolderProducto(itemView: android.view.View): RecyclerView.ViewHolder(itemView){
        var imagenProducto = binding.imagenProducto
        var item_nombre_p = binding.itemNombreP
        var item_precio_p = binding.itemPrecioP
        var item_precio_desc_p = binding.itemPrecioPDesc
        var item_nota_p = binding.itemNotaP
        var ib_fav = binding.ibFav
        var agregar_carrito = binding.itemAgregarCarritoP
        var item_stock = binding.itemStock

    }
}