package com.example.proyectobarreraiot

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectobarreraiot.Adapter.AdapterCamion
import com.example.proyectobarreraiot.Models.Camion
import com.google.firebase.database.*
import androidx.appcompat.widget.SearchView
import java.util.Locale

class VerCamionesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noHayIngresos: TextView

    private lateinit var searchView: SearchView
    private lateinit var camionesList: ArrayList<Camion>
    private lateinit var camionesFiltrados: ArrayList<Camion>
    private var adapter: AdapterCamion? = null
    private lateinit var database: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener


    override fun onResume(){
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

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
        searchView = view.findViewById(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.isNestedScrollingEnabled = false
        camionesList = arrayListOf()
        camionesFiltrados = arrayListOf()

        configurarBuscador()
        getCamionesData()
    }

    private fun configurarBuscador() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLista(newText)
                return true
            }
        })
    }

    private fun filtrarLista(texto: String?) {
        if (texto.isNullOrEmpty()) {
            camionesFiltrados.clear()
            camionesFiltrados.addAll(camionesList)
            adapter?.actualizarLista(camionesFiltrados)
            mostrarOcultarVacias()
            return
        }

        val busqueda = texto.lowercase(Locale.getDefault())
        camionesFiltrados.clear()

        for (item in camionesList) {
            if (item.patente?.lowercase(Locale.getDefault())?.contains(busqueda) == true ||
                item.conductor?.lowercase(Locale.getDefault())?.contains(busqueda) == true) {
                camionesFiltrados.add(item)
            }
        }
        adapter?.actualizarLista(camionesFiltrados)
        mostrarOcultarVacias()
    }

    private fun mostrarOcultarVacias() {
        if (camionesFiltrados.isEmpty()) {
            recyclerView.visibility = View.GONE
            noHayIngresos.visibility = View.VISIBLE
            noHayIngresos.text = "No se encontraron resultados"
        } else {
            recyclerView.visibility = View.VISIBLE
            noHayIngresos.visibility = View.GONE
        }
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

                camionesFiltrados.clear()
                camionesFiltrados.addAll(camionesList)

                iniciarAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        database.addValueEventListener(valueEventListener)
    }

    private fun iniciarAdapter() {
        if (camionesFiltrados.isEmpty()) {
            recyclerView.visibility = View.GONE
            noHayIngresos.visibility = View.VISIBLE
            noHayIngresos.text = "No hay camiones registrados"
        } else {
            recyclerView.visibility = View.VISIBLE
            noHayIngresos.visibility = View.GONE

            if (adapter == null) {
                // Se pasa una lambda vacía para deshabilitar el click
                adapter = AdapterCamion(camionesFiltrados)
                recyclerView.adapter = adapter
            } else {
                adapter?.notifyDataSetChanged()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (::valueEventListener.isInitialized) {
            database.removeEventListener(valueEventListener)
        }
    }
}
