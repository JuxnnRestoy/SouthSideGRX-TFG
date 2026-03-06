package com.example.southsidegrx_tfg.vendedor.Nav_Fragments_Vendedor

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.southsidegrx_tfg.Adapters.CategoriasAdapter
import com.example.southsidegrx_tfg.Modelos.Categoria
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.FragmentCategoriasAgregarVBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class FragmentCategoriasAgregarV : Fragment() {

    private lateinit var binding: FragmentCategoriasAgregarVBinding
    private lateinit var mContext: Context

    private var categoria=""
    private var imageUri: Uri?=null

    private lateinit var categoriasArrayList: ArrayList<Categoria>
    private lateinit var categoriasAdapater: CategoriasAdapter
    private var categoriasListener: ValueEventListener?=null

    override fun onAttach(context: Context) {
        mContext=context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoriasAgregarVBinding.inflate(layoutInflater,container, false)

        categoriasArrayList = ArrayList()
        categoriasAdapater = CategoriasAdapter(mContext, categoriasArrayList)

        binding.rvCategorias.layoutManager = LinearLayoutManager(mContext)
        binding.rvCategorias.adapter = categoriasAdapater

        cargarCategorias()

        binding.imgCategorias.setOnClickListener { seleccionarImagen() }
        binding.btnAgregarCategoria.setOnClickListener {
            validarInformacion()
        }

        return binding.root
    }

    private fun cargarCategorias(){
        categoriasListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoriasArrayList.clear()

                for(ds in snapshot.children){
                    val modelo = ds.getValue(Categoria::class.java)
                    if(modelo!=null){
                        // asegura el id desde la key (por si no viene o viene vacío)
                        modelo.id = ds.key ?: modelo.id
                        categoriasArrayList.add(modelo)
                    }
                }
                categoriasAdapater.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }
        FirebaseDatabase.getInstance().getReference("Categorias").addValueEventListener(categoriasListener as ValueEventListener)
    }

    private fun seleccionarImagen(){
        ImagePicker.with(requireActivity()).crop()
            .compress(1024).maxResultSize(1080,1080)
            .createIntent { intent-> resultadoImg.launch(intent)}
    }

    private val resultadoImg=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado ->
            if(resultado.resultCode == Activity.RESULT_OK){
                val data =resultado.data
                imageUri = data!!.data
                binding.imgCategorias.setImageURI(imageUri)
            }else{
                Toast.makeText(mContext,"Acción cancelada", Toast.LENGTH_SHORT).show()
            }
        }

    private fun agregarCategoriaBD(){
        val ref = FirebaseDatabase.getInstance().getReference("Categorias")
        val keyId = ref.push().key

        val hashMap = HashMap<String,Any>()
        hashMap["id"] = "${keyId}"
        hashMap["categoria"] = "${categoria}"

        ref.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //Toast.makeText(context,"Se agregó la categoría con éxito",Toast.LENGTH_SHORT).show()
                //binding.edtCategoria.setText("")
                subirImagenStorage(keyId)
            }
            .addOnFailureListener {e ->
                Toast.makeText(context,"${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
    private fun subirImagenStorage(keyId:String){
        //setLoading(true)????
        val nombreImagen = keyId
        val nombreCarpeta = "Categorias/${nombreImagen}"
        val storageRef = FirebaseStorage.getInstance().getReference(nombreCarpeta)
        storageRef.putFile(imageUri!!).addOnSuccessListener {taskSnapshot ->
            //setLoading(false)
            val uriTask = taskSnapshot.storage.downloadUrl
            while(!uriTask.isSuccessful);
            val urlImagenCargada = uriTask.result

            if(uriTask.isSuccessful){
                val hashMap = HashMap<String,Any>()
                hashMap["imagenUrl"]="$urlImagenCargada"
                val ref = FirebaseDatabase.getInstance().getReference("Categorias")
                ref.child(nombreImagen).updateChildren(hashMap)
                Toast.makeText(mContext,"Se agregó la categoría con éxito",Toast.LENGTH_SHORT).show()
                binding.edtCategoria.setText("")
                imageUri = null
                binding.imgCategorias.setImageURI(imageUri)
                binding.imgCategorias.setImageResource(R.drawable.ico_categorias)
            }
        }
            .addOnFailureListener {e->
                //setLoading(false)
                Toast.makeText(context,"${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun validarInformacion(){
        categoria = binding.edtCategoria.text.toString().trim()
        when{
            categoria.isEmpty()->Toast.makeText(context,"Ingrese una categoría",Toast.LENGTH_SHORT).show()
            imageUri==null -> Toast.makeText(context,"Selecciona una imagen", Toast.LENGTH_SHORT).show()
            else -> agregarCategoriaBD()
        }
    }

}