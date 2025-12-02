package com.example.proyectobarreraiot

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class ControlManualFragment : Fragment() {

    private val ARDUINO_PORT = 80

    private lateinit var tvEstadoCmd: TextView
    private lateinit var btnConexion: Button
    private lateinit var tvEstadoConexion: TextView
    private lateinit var cvEstadoContainer: MaterialCardView
    private lateinit var etIpArduino: EditText

    companion object {
        private var ultimoEstadoTexto: String = "Estado: Desconocido"
        private var ultimoColorFondo: Int = Color.DKGRAY
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_control_manual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar Vistas
        tvEstadoCmd = view.findViewById(R.id.tvEstadoCmd)
        btnConexion = view.findViewById(R.id.btnConexionManual)
        tvEstadoConexion = view.findViewById(R.id.tvEstadoConexionManual)
        cvEstadoContainer = view.findViewById(R.id.cvEstadoContainerManual)
        etIpArduino = view.findViewById(R.id.etIpArduino)

        val sharedPref = requireActivity().getSharedPreferences("ConfiguracionIoT", Context.MODE_PRIVATE)
        val ipGuardada = sharedPref.getString("ARDUINO_IP", "192.168.1.4") // Valor por defecto
        etIpArduino.setText(ipGuardada)

        tvEstadoConexion.text = ultimoEstadoTexto
        if (ultimoColorFondo != Color.DKGRAY) {
            cvEstadoContainer.setCardBackgroundColor(ultimoColorFondo)
        }

        btnConexion.setOnClickListener {
            verificarConexionArduino()
        }

        view.findViewById<Button>(R.id.btnAbrirEntrada).setOnClickListener {
            enviarComando("Q", "Barrera Bascula Abierta")
        }
        view.findViewById<Button>(R.id.btnCerrarEntrada).setOnClickListener {
            enviarComando("W", "Barrera Bascula Cerrada")
        }

        view.findViewById<Button>(R.id.btnAbrirSalida).setOnClickListener {
            enviarComando("E", "Barrera Proximidad Abierta")
        }
        view.findViewById<Button>(R.id.btnCerrarSalida).setOnClickListener {
            enviarComando("R", "Barrera Proximidad Cerrada")
        }

        view.findViewById<Button>(R.id.btnEncenderAlarma).setOnClickListener {
            enviarComando("S", "¡Alarma Encendida!")
        }

        view.findViewById<Button>(R.id.btnApagarAlarma).setOnClickListener {
            enviarComando("D", "Alarma Apagada")
        }
    }

    private fun obtenerIpActual(): String {
        return etIpArduino.text.toString().trim()
    }

    private fun verificarConexionArduino() {
        val currentIp = obtenerIpActual()

        val sharedPref = requireActivity().getSharedPreferences("ConfiguracionIoT", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString("ARDUINO_IP", currentIp)
            apply()
        }
        // -------------------------------------------------------

        tvEstadoConexion.text = "Conectando a $currentIp..."
        cvEstadoContainer.setCardBackgroundColor(Color.GRAY)
        btnConexion.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            var conectado = false
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(currentIp, ARDUINO_PORT), 2000)
                conectado = true
                socket.close()
            } catch (e: Exception) {
                conectado = false
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                btnConexion.isEnabled = true
                if (conectado) {
                    val texto = "Estado: Conectado ($currentIp)"
                    val color = Color.GREEN
                    tvEstadoConexion.text = texto
                    cvEstadoContainer.setCardBackgroundColor(color)

                    ultimoEstadoTexto = texto
                    ultimoColorFondo = color

                    Toast.makeText(context, "¡Conexión exitosa!", Toast.LENGTH_SHORT).show()
                } else {
                    val texto = "Falló la conexión"
                    val color = Color.RED
                    tvEstadoConexion.text = texto
                    cvEstadoContainer.setCardBackgroundColor(color)

                    ultimoEstadoTexto = texto
                    ultimoColorFondo = color

                    Toast.makeText(context, "No se pudo conectar a $currentIp", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun enviarComando(comando: String, descripcion: String) {
        val currentIp = obtenerIpActual()

        tvEstadoCmd.text = "Procesando: $descripcion..."
        tvEstadoCmd.setTextColor(Color.DKGRAY)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(currentIp, ARDUINO_PORT), 1000)

                val output = PrintWriter(socket.getOutputStream(), true)
                output.print(comando)
                output.flush()

                socket.close()

                withContext(Dispatchers.Main) {
                    tvEstadoCmd.text = descripcion
                    tvEstadoCmd.setTextColor(Color.GREEN)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    tvEstadoCmd.text = "Error: No se pudo ejecutar '$descripcion'"
                    tvEstadoCmd.setTextColor(Color.RED)
                }
            }
        }
    }
}
