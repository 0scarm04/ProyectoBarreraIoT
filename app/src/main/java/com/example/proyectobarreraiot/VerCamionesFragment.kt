package com.example.proyectobarreraiot

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectobarreraiot.Adapter.AdapterCamion
import com.example.proyectobarreraiot.Models.Camion
import com.google.firebase.database.*

class VerCamionesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noHayIngresos: TextView
    private lateinit var camionesList: ArrayList<Camion>
    private lateinit var database: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener

    // Para que cambie a horizontal en la tabla
    override fun onResume(){
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    // Para que cambie a vertical en otras vistas
    override fun onPause(){
        super.onPause()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_ver_camiones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvCamiones)
        noHayIngresos = view.findViewById(R.id.noHayIngresos)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.isNestedScrollingEnabled = false
        camionesList = arrayListOf()

        getCamionesData()
    }

    private fun getCamionesData() {
        database = FirebaseDatabase.getInstance().getReference("Camiones")

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                camionesList.clear()
                if (snapshot.exists()) {
                    for (camionSnapshot in snapshot.children) {
                        val camion = camionSnapshot.getValue(Camion::class.java)
                        if (camion != null) {
                            camionesList.add(camion)
                        }
                    }
                }
                if (camionesList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    noHayIngresos.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    noHayIngresos.visibility = View.GONE
                    val adapter = AdapterCamion(camionesList) { camion ->
                        mostrarDialogoConfirmacion(camion)
                    }
                    recyclerView.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        database.addValueEventListener(valueEventListener)
    }

    private fun mostrarDialogoConfirmacion(camion: Camion) {
        val context = context ?: return

        AlertDialog.Builder(context)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar el registro de este camión?")
            .setPositiveButton("Sí") { _, _ ->
                eliminarCamion(camion)
            }
            .setNegativeButton("No", null)
            .setIcon(R.drawable.ic_delete)
            .show()
    }

    private fun eliminarCamion(camion: Camion) {
        if (camion.id == null) {
            Toast.makeText(context, "No existe un camion con esa ID", Toast.LENGTH_SHORT).show()
            return
        }
        database.child(camion.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Camion eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al eliminar el camion", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        database.removeEventListener(valueEventListener)
    }
}
