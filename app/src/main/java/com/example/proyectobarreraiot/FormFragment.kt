package com.example.proyectobarreraiot

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.proyectobarreraiot.Models.Camion
import com.example.proyectobarreraiot.databinding.ActivityFormBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class FormFragment : Fragment() {

    private var _binding: ActivityFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var pesoMinimo: Float = 0.0f
    private var pesoMaximo: Float = Float.MAX_VALUE

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
        auth = FirebaseAuth.getInstance()

        // Cargar configuración de pesos
        val sharedPref = activity?.getSharedPreferences("PesosConfig", Context.MODE_PRIVATE)
        pesoMinimo = sharedPref?.getFloat("peso_minimo", 0.0f) ?: 0.0f
        pesoMaximo = sharedPref?.getFloat("peso_maximo", Float.MAX_VALUE) ?: Float.MAX_VALUE

        // Ingresa fecha y hora de forma automatica
        cargarFechaHora()

        // Ingresa el nombre del guardia de forma automatica
        val usuarioConectado = auth.currentUser
        val nombreGuardia = usuarioConectado?.displayName
        if (nombreGuardia != null && nombreGuardia.isNotEmpty()) {
            binding.etGuardia.setText(nombreGuardia)
            binding.tilGuardia.isEnabled = false
        } else {
            binding.etGuardia.setText("Guardia no identificado")
            binding.tilGuardia.isEnabled = false
        }

        // Actualiza el estado
        binding.etPeso.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val pesoStr = s.toString()
                if (pesoStr.isNotEmpty()) {
                    val peso = pesoStr.toFloatOrNull()
                    if (peso != null) {
                        if (peso in pesoMinimo..pesoMaximo) {
                            binding.etEstado.setText("Aceptado")
                        } else {
                            binding.etEstado.setText("Rechazado")
                        }
                    }
                } else {
                    binding.etEstado.setText("")
                }
            }
        })


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
                    cargarFechaHora()
                    Snackbar.make(binding.root, "Camion guardado", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Snackbar.make(binding.root, "Error al guardar: ${it.message}", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun cargarFechaHora(){
        // Obtener fecha y hora actual
        val calendario = Calendar.getInstance()
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val fechaActual = formatoFecha.format(calendario.time)
        val horaActual = formatoHora.format(calendario.time)

        // Establecer en los campos de texto
        binding.etFecha.setText(fechaActual)
        binding.etHora.setText(horaActual)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
