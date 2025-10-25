package com.example.proyectobarreraiot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.proyectobarreraiot.Models.Camion
import com.example.proyectobarreraiot.databinding.ActivityFormBinding // We reuse the same layout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FormFragment : Fragment() {

    private var _binding: ActivityFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("Camiones")

        binding.btnGuardar.setOnClickListener {
            val patente = binding.etPatente.text.toString()
            val peso = binding.etPeso.text.toString()
            val conductor = binding.etConductor.text.toString()
            val hora = binding.etHora.text.toString()
            val fecha = binding.etFecha.text.toString()
            val estado = binding.etEstado.text.toString()
            val guardia = binding.etGuardia.text.toString()

            val id = database.push().key
            if (id == null) {
                Snackbar.make(binding.root, "Error al generar ID para el camión.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (patente.isEmpty() || peso.isEmpty() || conductor.isEmpty() || hora.isEmpty() || fecha.isEmpty() || estado.isEmpty() || guardia.isEmpty()) {
                Snackbar.make(binding.root, "Por favor, complete todos los campos.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val camion = Camion(id, patente, peso, conductor, hora, fecha, estado, guardia)
            database.child(id).setValue(camion)
                .addOnSuccessListener {
                    binding.etPatente.setText("")
                    binding.etPeso.setText("")
                    binding.etConductor.setText("")
                    binding.etHora.setText("")
                    binding.etFecha.setText("")
                    binding.etEstado.setText("")
                    binding.etGuardia.setText("")
                    Snackbar.make(binding.root, "Camion guardado", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Snackbar.make(binding.root, "Error al guardar: ${it.message}", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
