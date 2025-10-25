package com.example.proyectobarreraiot.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectobarreraiot.Models.Camion
import com.example.proyectobarreraiot.R
import com.google.android.material.button.MaterialButton

class AdapterCamion (
    private var camiones: ArrayList<Camion>,
    private val onDeleteClicked: (Camion) -> Unit
    ):
    RecyclerView.Adapter<AdapterCamion.ViewHolder>() {
    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        val tituloCamion: TextView = itemView.findViewById(R.id.tvTituloCamion)
        val patente: TextView = itemView.findViewById(R.id.tvPatente)
        val peso: TextView = itemView.findViewById(R.id.tvPeso)
        val conductor: TextView = itemView.findViewById(R.id.tvConductor)
        val hora: TextView = itemView.findViewById(R.id.tvHora)
        val fecha: TextView = itemView.findViewById(R.id.tvFecha)
        val estado: TextView = itemView.findViewById(R.id.tvEstado)
        val guardia: TextView = itemView.findViewById(R.id.tvGuardia)
        val btnEliminar: MaterialButton = itemView.findViewById(R.id.btnEliminaritem)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterCamion.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_camiones, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val camion = camiones[position]
        holder.tituloCamion.text = "Camion ${position + 1}"
        holder.patente.text = camion.patente
        holder.peso.text = camion.peso
        holder.conductor.text = camion.conductor
        holder.hora.text = camion.hora
        holder.fecha.text = camion.fecha
        holder.estado.text = camion.estado
        holder.guardia.text = camion.guardia
        holder.btnEliminar.setOnClickListener {
            camiones.removeAt(position)
            notifyItemRemoved(position)
            onDeleteClicked(camion)
        }
    }

    override fun getItemCount(): Int {
        return camiones.size
    }
}