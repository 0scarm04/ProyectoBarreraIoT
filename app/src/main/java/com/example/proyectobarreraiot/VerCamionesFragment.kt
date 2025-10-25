package com.example.proyectobarreraiot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectobarreraiot.Adapter.AdapterCamion
import com.example.proyectobarreraiot.Models.Camion
import com.google.firebase.database.*

class VerCamionesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var camionesList: ArrayList<Camion>
    private lateinit var database: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_ver_camiones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvCamiones)
        recyclerView.layoutManager = LinearLayoutManager(context)
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
                    val adapter = AdapterCamion(camionesList){ camion ->
                        eliminarCamion(camion)
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
