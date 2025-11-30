package com.example.proyectobarreraiot

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.proyectobarreraiot.Models.Camion
import com.example.proyectobarreraiot.databinding.ActivityFormBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*

class FormFragment : Fragment() {

    private var _binding: ActivityFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val ARDUINO_PORT = 80
    private var escuchaActiva = true

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

        val sharedPref = activity?.getSharedPreferences("PesosConfig", Context.MODE_PRIVATE)
        pesoMinimo = sharedPref?.getFloat("peso_minimo", 0.0f) ?: 0.0f
        pesoMaximo = sharedPref?.getFloat("peso_maximo", Float.MAX_VALUE) ?: Float.MAX_VALUE

        binding.tilPeso.isEnabled = false
        binding.tilFecha.isEnabled = false
        binding.tilHora.isEnabled = false

        iniciarEscuchaSensorArduino()

        cargarFechaHora()

        val usuarioConectado = auth.currentUser
        val nombreGuardia = usuarioConectado?.displayName
        if (nombreGuardia != null && nombreGuardia.isNotEmpty()) {
            binding.etGuardia.setText(nombreGuardia)
            binding.tilGuardia.isEnabled = false
        } else {
            binding.etGuardia.setText("Guardia no identificado")
            binding.tilGuardia.isEnabled = false
        }


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
            guardarDatos()
        }
    }

    private fun obtenerIpConfigurada(): String {
        val sharedPref = activity?.getSharedPreferences("ConfiguracionIoT", Context.MODE_PRIVATE)
        return sharedPref?.getString("ARDUINO_IP", "192.168.1.4") ?: "192.168.1.4"
    }

    private fun iniciarEscuchaSensorArduino() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (escuchaActiva && isActive) {
                try {
                    val socket = Socket()

                    val ipDestino = obtenerIpConfigurada()
                    socket.connect(InetSocketAddress(ipDestino, ARDUINO_PORT), 2000)

                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    while (socket.isConnected && !socket.isClosed && escuchaActiva) {
                        val linea = reader.readLine() ?: break

                        if (linea.startsWith("PESO:")) {
                            val partes = linea.split(":")
                            if (partes.size == 2) {
                                val distanciaStr = partes[1].trim()

                                withContext(Dispatchers.Main) {
                                    actualizarFormularioAutomatico(distanciaStr)
                                }
                            }
                        }
                    }
                    socket.close()
                } catch (e: Exception) {
                    Thread.sleep(2000)
                }
            }
        }
    }

    private fun actualizarFormularioAutomatico(distancia: String) {
        cargarFechaHora()
        binding.etPeso.setText(distancia)
    }

    private fun cargarFechaHora(){
        val calendario = Calendar.getInstance()
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val fechaActual = formatoFecha.format(calendario.time)
        val horaActual = formatoHora.format(calendario.time)

        binding.etFecha.setText(fechaActual)
        binding.etHora.setText(horaActual)
    }

    private fun guardarDatos() {
        val patente = binding.etPatente.text.toString().uppercase()
        val peso = binding.etPeso.text.toString()
        val conductor = binding.etConductor.text.toString().capitalize()
        val hora = binding.etHora.text.toString()
        val fecha = binding.etFecha.text.toString()
        val estado = binding.etEstado.text.toString()
        val guardia = binding.etGuardia.text.toString()

        val id = database.push().key ?: return

        if (patente.isEmpty() || peso.isEmpty() || conductor.isEmpty() || hora.isEmpty() || fecha.isEmpty() || estado.isEmpty() || guardia.isEmpty()) {
            Snackbar.make(binding.root, "Por favor, complete todos los campos.", Snackbar.LENGTH_LONG).show()
            return
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

    override fun onDestroyView() {
        super.onDestroyView()
        escuchaActiva = false
        _binding = null
    }
}
