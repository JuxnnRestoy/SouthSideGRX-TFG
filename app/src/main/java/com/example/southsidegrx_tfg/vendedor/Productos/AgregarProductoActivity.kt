package com.example.southsidegrx_tfg.vendedor.Productos

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.southsidegrx_tfg.Adapters.ImagenSeleccionadaAdapter
import com.example.southsidegrx_tfg.Funciones
import com.example.southsidegrx_tfg.Modelos.Categoria
import com.example.southsidegrx_tfg.Modelos.ModeloImagenSeleccionada
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.ActivityAgregarProductoBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarProductoBinding

    private var imagenUri: Uri?=null

    private lateinit var imagenSeleccionadaArrayList: ArrayList<ModeloImagenSeleccionada>
    private lateinit var adaptadorImagenSeleccionada: ImagenSeleccionadaAdapter

    private lateinit var categoriasArrayList: ArrayList<Categoria>

    private var idCat =""
    private var nombreCat = ""

    private var nombreP=""
    private var descripcionP=""
    private var categoriaP=""
    private var precioP=""
    private var descuentoHab = false
    private var precioDescuentoP=""
    private var notaDesc=""
    private var porcentajeDescP =""

    private var Edicion = false
    private var idProducto =""
    private var stockP = ""

    private val resultadoImg =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if(resultado.resultCode== Activity.RESULT_OK){
                val data = resultado.data
                imagenUri =data!!.data //si la imagen ha sio tomada o seleccionada correctamente se carga en imagenUri

                val tiempo = "${Funciones().obtenerTiempoRegistro()}"

                val modeloImagenSelecciona = ModeloImagenSeleccionada(tiempo,imagenUri,null,false)
                imagenSeleccionadaArrayList.add(modeloImagenSelecciona)
                cargarImagenes()
            }else{
                Toast.makeText(this,"Acción cancelada", Toast.LENGTH_SHORT).show()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarCategorias()

        Edicion = intent.getBooleanExtra("Edicion",false)


        // estas vistas se inicializar ocultas
        binding.edtPorcentajeDescuentoP.visibility = View.GONE
        binding.btnCalcularPrecioDesc.visibility = View.GONE
        binding.precioConDescuentoProdTxt.visibility = View.GONE
        binding.edtPrecioDescuentoP.visibility = View.GONE
        binding.edtNotaDescuentoP.visibility = View.GONE

        binding.descuentoSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                binding.edtPorcentajeDescuentoP.visibility = View.VISIBLE
                binding.btnCalcularPrecioDesc.visibility = View.VISIBLE
                binding.precioConDescuentoProdTxt.visibility = View.VISIBLE
                binding.edtPrecioDescuentoP.visibility = View.VISIBLE
                binding.edtNotaDescuentoP.visibility = View.VISIBLE
            }else{
                binding.edtPorcentajeDescuentoP.visibility = View.GONE
                binding.btnCalcularPrecioDesc.visibility = View.GONE
                binding.precioConDescuentoProdTxt.visibility = View.GONE
                binding.edtPrecioDescuentoP.visibility = View.GONE
                binding.edtNotaDescuentoP.visibility = View.GONE
            }
        }

        if(Edicion){
            idProducto = intent.getStringExtra("idProducto")?:""
            binding.txtAgregarProducto.text = "Editar Producto"
            cargarInfo()
        }else{
            binding.txtAgregarProducto.text = "Agregar Producto"
        }

        imagenSeleccionadaArrayList = ArrayList()

        binding.imgAgregarProducto.setOnClickListener { seleccionarImagen() }

        binding.tvCategoria.setOnClickListener { seleccionarCategorias() }

        binding.btnCalcularPrecioDesc.setOnClickListener { calcularPrecioConDescuento() }

        binding.btnAgregarProducto.setOnClickListener { validarInformacion() }

        cargarImagenes()
    }

    private fun cargarInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val nombre = "${snapshot.child("nombre").value}"
                val descripcion = "${snapshot.child("descripcion").value}"
                val categoria = "${snapshot.child("categoria").value}"
                val precio = "${snapshot.child("precio").value}"
                val precioDesc = "${snapshot.child("precioDesc").value}"
                val notaDesc = "${snapshot.child("notaDesc").value}"
                val stock = "${snapshot.child("stock").value}"

                binding.edtNombreP.setText(nombre)
                binding.edtDescripcionP.setText(descripcion)
                binding.tvCategoria.text = categoria
                binding.edtPrecioP.setText(precio)
                binding.edtPrecioDescuentoP.setText(precioDesc)
                binding.edtNotaDescuentoP.setText(notaDesc)
                binding.edtStock.setText(stock)


                val tieneDescuento = (precioDesc.toDoubleOrNull() ?: 0.0) > 0.0 || notaDesc.isNotEmpty()
                binding.descuentoSwitch.isChecked = tieneDescuento

                if(tieneDescuento){
                    binding.edtPorcentajeDescuentoP.visibility = View.VISIBLE
                    binding.btnCalcularPrecioDesc.visibility = View.VISIBLE
                    binding.precioConDescuentoProdTxt.visibility = View.VISIBLE
                    binding.edtPrecioDescuentoP.visibility = View.VISIBLE
                    binding.edtNotaDescuentoP.visibility = View.VISIBLE
                }else{
                    binding.edtPorcentajeDescuentoP.visibility = View.GONE
                    binding.btnCalcularPrecioDesc.visibility = View.GONE
                    binding.precioConDescuentoProdTxt.visibility = View.GONE
                    binding.edtPrecioDescuentoP.visibility = View.GONE
                    binding.edtNotaDescuentoP.visibility = View.GONE
                }

                val refImg = FirebaseDatabase.getInstance().getReference("Productos")
                    .child(idProducto).child("Imagenes")

                refImg.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshotImg: DataSnapshot) {
                        imagenSeleccionadaArrayList.clear()
                        for(ds in snapshotImg.children){
                            val idImg = ds.child("id").getValue(String::class.java) ?: ds.key ?: ""
                            val url = ds.child("imagenUrl").getValue(String::class.java) ?: ""

                            if(url.isNotEmpty()){
                                val modelo = ModeloImagenSeleccionada(
                                    idImg,
                                    null,
                                    url,
                                    true
                                )
                                imagenSeleccionadaArrayList.add(modelo)
                            }
                        }
                        cargarImagenes()
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun calcularPrecioConDescuento(){
        val precioOriginal = binding.edtPrecioP.text.toString()
        val notaDescuento = binding.edtNotaDescuentoP.text.toString()
        val porcentaje = binding.edtPorcentajeDescuentoP.text.toString()

        when{
            precioOriginal.isEmpty() -> Toast.makeText(this,"Introduce el precio", Toast.LENGTH_SHORT).show()
            notaDescuento.isEmpty() -> Toast.makeText(this,"Introduce la nota del descuento", Toast.LENGTH_SHORT).show()
            porcentaje.isEmpty() -> Toast.makeText(this,"Introduce el porcentaje", Toast.LENGTH_SHORT).show()
            else ->{
                val precioOriginalDouble = precioOriginal.replace(",", ".").toDoubleOrNull()
                val porcentajeDouble = porcentaje.replace(",", ".").toDoubleOrNull()
                if(precioOriginalDouble ==null || porcentajeDouble == null){
                    Toast.makeText(this,"Datos de descuento no válidos", Toast.LENGTH_SHORT).show()
                    return
                }
                val descuento = precioOriginalDouble*(porcentajeDouble/100)
                val precioDescuentoAplicado = precioOriginalDouble - descuento
                binding.precioConDescuentoProdTxt.text = "Precio con descuento"
                binding.edtPrecioDescuentoP.setText(String.format("%.2f", precioDescuentoAplicado))
            }
        }
    }

    private fun validarInformacion(){
        nombreP=binding.edtNombreP.text.toString().trim()
        descripcionP=binding.edtDescripcionP.text.toString().trim()
        categoriaP=binding.tvCategoria.text.toString().trim()
        precioP=binding.edtPrecioP.text.toString().trim()
        descuentoHab=binding.descuentoSwitch.isChecked
        stockP = binding.edtStock.text.toString().trim()

        val stockDouble = stockP.replace(",",".").toDoubleOrNull()

        when{
            nombreP.isEmpty() ->{
                binding.tilNombreP.error="Introduce el nombre"
                binding.edtNombreP.requestFocus()
            }
            categoriaP.isEmpty()->{
                binding.tvCategoria.error="Selecciona una categoría"
                binding.tvCategoria.requestFocus()
            }
            precioP.isEmpty()->{
                binding.tilPrecioP.error="Introduce un precio"
                binding.edtPrecioP.requestFocus()
            }
            stockDouble==null -> {
                binding.tilStock.error="Stock inválido"
                binding.edtStock.requestFocus()
                return
            }
            imagenSeleccionadaArrayList.isEmpty() ->{
                Toast.makeText(this,"Selecciona al menos una imagen",Toast.LENGTH_SHORT).show()
                return
            }
            else ->{
                if(descuentoHab){
                    notaDesc = binding.edtNotaDescuentoP.text.toString().trim()
                    porcentajeDescP = binding.edtPorcentajeDescuentoP.text.toString().trim()
                    precioDescuentoP = binding.edtPrecioDescuentoP.text.toString().trim()
                    when{
                        notaDesc.isEmpty()->{
                            binding.edtNotaDescuentoP.error="Ingrese una nota"
                            binding.edtNotaDescuentoP.requestFocus()
                        }
                        porcentajeDescP.isEmpty()->{
                            binding.edtPorcentajeDescuentoP.error="Introduce el porcentaje"
                            binding.edtPorcentajeDescuentoP.requestFocus()
                        }
                        precioDescuentoP.isEmpty()-> {
                            binding.tilPrecioDescuentoP.error = "Calcula el precio con descuento"
                        }
                        else -> agregarProductoBD()
                    }
                }
                // descuento false
                else{
                    precioDescuentoP="0"
                    notaDesc=""
                    agregarProductoBD()
                }
            }

        }
    }

    private fun agregarProductoBD(){
        // set loadin ?
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        val idFinal = if(Edicion) idProducto else (ref.push().key ?: return)

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = idFinal
        hashMap["nombre"] = nombreP
        hashMap["descripcion"] = descripcionP
        hashMap["categoria"] = categoriaP
        hashMap["precio"] = precioP
        hashMap["precioDesc"] = precioDescuentoP
        hashMap["notaDesc"] = notaDesc
        hashMap["stock"] = stockP.replace(",",".").toDouble()

        ref.child(idFinal).updateChildren(hashMap)
            .addOnSuccessListener {

                val hayNuevas = imagenSeleccionadaArrayList.any { it.imageUri != null }
                if(hayNuevas){
                    subirImagenStorage(idFinal)
                }else{
                    Toast.makeText(this, if(Edicion) "Producto actualizado" else "Se agregó el producto", Toast.LENGTH_SHORT).show()
                    if(Edicion) finish() else limpiarCampos()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirImagenStorage(idFinal: String){
        val imagenesNuevas = imagenSeleccionadaArrayList.filter { it.imageUri != null }

        if(imagenesNuevas.isEmpty()){
            Toast.makeText(
                this,
                if(Edicion) "Producto actualizado correctamente" else "Producto añadido correctamente",
                Toast.LENGTH_SHORT
            ).show()

            if(Edicion) finish() else limpiarCampos()
            return
        }

        var subidasCompletadas = 0
        val totalImagenes = imagenesNuevas.size

        for(modeloImagenSel in imagenesNuevas){
            val nombreImagen = modeloImagenSel.id
            val rutaImagen = "Productos/$nombreImagen"

            val storageRef = FirebaseStorage.getInstance().getReference(rutaImagen)
            storageRef.putFile(modeloImagenSel.imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { urlImgCargada ->

                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = modeloImagenSel.id
                        hashMap["imagenUrl"] = urlImgCargada.toString()

                        val ref = FirebaseDatabase.getInstance().getReference("Productos")
                        ref.child(idFinal).child("Imagenes").child(nombreImagen)
                            .updateChildren(hashMap)
                            .addOnSuccessListener {
                                subidasCompletadas++

                                if(subidasCompletadas == totalImagenes){
                                    Toast.makeText(this, if(Edicion) "Producto actualizado correctamente" else "Producto añadido correctamente", Toast.LENGTH_SHORT).show()

                                    if(Edicion){ finish()
                                    }else{ limpiarCampos()
                                    }
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun limpiarCampos(){
        binding.edtNombreP.setText("")
        binding.edtDescripcionP.setText("")
        binding.tvCategoria.text = "Selecciona categoría"
        binding.edtPrecioP.setText("")
        binding.edtPrecioDescuentoP.setText("")
        binding.edtNotaDescuentoP.setText("")
        binding.edtStock.setText("")
        binding.edtPorcentajeDescuentoP.setText("")

        binding.precioConDescuentoProdTxt.text = "Precio con descuento"
        binding.tilPrecioDescuentoP.error = null

        binding.descuentoSwitch.isChecked = false

        binding.edtPorcentajeDescuentoP.visibility = View.GONE
        binding.btnCalcularPrecioDesc.visibility = View.GONE
        binding.precioConDescuentoProdTxt.visibility = View.GONE
        binding.tilPrecioDescuentoP.visibility = View.GONE
        binding.edtNotaDescuentoP.visibility = View.GONE

        imagenUri = null
        binding.imgAgregarProducto.setImageResource(R.drawable.ico_agregar_producto)

        imagenSeleccionadaArrayList.clear()
        cargarImagenes()
    }

    private fun cargarCategorias(){
        categoriasArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categorias").orderByChild("categoria")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //limpiar la lista dentro de modelo se guarda la información de la base de datos  Leer las categorías en tiempo real
                categoriasArrayList.clear()
                for(ds in snapshot.children){
                    var modelo = ds.getValue(Categoria::class.java)
                    categoriasArrayList.add(modelo!!)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun seleccionarCategorias(){
        val categoriasArray = arrayOfNulls<String>(categoriasArrayList.size)
        for(i in categoriasArray.indices){
            categoriasArray[i] = categoriasArrayList[i].categoria
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccione una categoría").setItems(categoriasArray){dialog, witch ->
            idCat = categoriasArrayList[witch].id
            nombreCat = categoriasArrayList[witch].categoria
            binding.tvCategoria.text = nombreCat
        }.show()
    }

    private fun cargarImagenes(){
        adaptadorImagenSeleccionada = ImagenSeleccionadaAdapter(this,imagenSeleccionadaArrayList)
        binding.RVImagenesProducto.adapter = adaptadorImagenSeleccionada
    }
    private fun seleccionarImagen(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent->
                resultadoImg.launch(intent)
            }
    }

}