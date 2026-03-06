package com.example.southsidegrx_tfg.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.southsidegrx_tfg.Modelos.Usuarios
import com.example.southsidegrx_tfg.R
import com.example.southsidegrx_tfg.databinding.ItemUsuarioVBinding
import com.example.southsidegrx_tfg.vendedor.VentaCarritoClienteActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UsuariosVendedorAdapter: RecyclerView.Adapter<UsuariosVendedorAdapter.HolderUsuario> {

    private lateinit var binding: ItemUsuarioVBinding
    private var mContext: Context
    private var usuariosArrayList: ArrayList<Usuarios>

    constructor(mContext: Context, usuariosArrayList: ArrayList<Usuarios>):super() {
        this.mContext = mContext
        this.usuariosArrayList = usuariosArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderUsuario {
        binding = ItemUsuarioVBinding.inflate(LayoutInflater.from(mContext),parent,false)
        return HolderUsuario(binding.root)
    }

    override fun onBindViewHolder(holder: HolderUsuario, position: Int) {
        val modelo  = usuariosArrayList[position]

        holder.tv_nombre.text = modelo.nombre
        holder.tv_email.text = modelo.email
        holder.tv_tipoUsuario.text = modelo.tipoUsuario
        val imagen = (modelo.imagen?: "".trim())
        if(imagen.isEmpty() || imagen == null){
            holder.imgUsuarioItem.setImageResource(R.drawable.ico_login_cliente)
        }else{
            Glide.with(mContext).load(imagen)
                .placeholder(R.drawable.ico_login_cliente)
                .into(holder.imgUsuarioItem)
        }
        try{
            Glide.with(mContext).load(imagen).placeholder(R.drawable.ico_login_cliente)
                .error(R.drawable.ico_login_cliente).into(holder.imgUsuarioItem)
        }catch (e: Exception){
            holder.imgUsuarioItem.setImageResource(R.drawable.ico_login_cliente)
        }

        holder.btn_eliminar.setOnClickListener {

            AlertDialog.Builder(mContext)
                .setTitle("Eliminar usuario")
                .setMessage("¿Seguro que quieres eliminar a ${modelo.nombre}?")
                .setPositiveButton("Eliminar") { _, _ ->

                    eliminarUsuario(modelo.uid)

                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        //solo clientes pueden tener venta
        if(modelo.tipoUsuario=="Cliente" || modelo.tipoUsuario=="cliente"){
            holder.btn_venta.visibility = View.VISIBLE
            holder.btn_venta.setOnClickListener {
                val intent = Intent(mContext, VentaCarritoClienteActivity::class.java)
                intent.putExtra("clienteUid",modelo.uid)
                intent.putExtra("clienteNombre",modelo.nombre)
                mContext.startActivity(intent)

            }
        }else{
            holder.btn_venta.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return usuariosArrayList.size
    }

    private fun eliminarUsuario(uid:String){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")

        val miUid = FirebaseAuth.getInstance().uid

        if(uid == miUid){
            Toast.makeText(mContext,"No puedes eliminarte a ti mismo",Toast.LENGTH_SHORT).show()
            return
        }

        ref.child(uid)
            .removeValue()
            .addOnSuccessListener {


                Toast.makeText(mContext,"Usuario eliminado",Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {

                Toast.makeText(mContext,"Error al eliminar",Toast.LENGTH_SHORT).show()

            }
    }

    inner class HolderUsuario(itemView: View): RecyclerView.ViewHolder(itemView){
        var imgUsuarioItem = binding.imgUsuarioItem
        var tv_nombre = binding.tvNombreUsuario
        var tv_email = binding.emailUsuario
        var tv_tipoUsuario =binding.tipoUsuario
        var btn_venta = binding.btnVenta
        var btn_eliminar = binding.btnEliminarUsuario

    }
}