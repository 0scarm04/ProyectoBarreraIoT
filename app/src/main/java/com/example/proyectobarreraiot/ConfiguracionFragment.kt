package com.example.proyectobarreraiot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.semantics.setText
import androidx.compose.ui.semantics.text
import androidx.fragment.app.Fragment
import com.example.proyectobarreraiot.databinding.FragmentConfiguracionBinding
import com.google.firebase.auth.FirebaseAuth

class ConfiguracionFragment : Fragment() {

    private var _binding: FragmentConfiguracionBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfiguracionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val sharedPref = activity?.getSharedPreferences("PesosConfig", Context.MODE_PRIVATE) ?: return

        // Cargar los valores guardados
        val pesoMinimo = sharedPref.getFloat("peso_minimo", 0.0f)
        val pesoMaximo = sharedPref.getFloat("peso_maximo", 0.0f)

        if (pesoMinimo > 0.0f) {
            binding.etPesoMinimo.setText(pesoMinimo.toString())
        }
        if (pesoMaximo > 0.0f) {
            binding.etPesoMaximo.setText(pesoMaximo.toString())
        }

        // Guardar configuración
        binding.btnGuardarConfig.setOnClickListener {
            val minStr = binding.etPesoMinimo.text.toString()
            val maxStr = binding.etPesoMaximo.text.toString()

            if (minStr.isNotEmpty() && maxStr.isNotEmpty()) {
                with(sharedPref.edit()) {
                    putFloat("peso_minimo", minStr.toFloat())
                    putFloat("peso_maximo", maxStr.toFloat())
                    apply()
                }
                Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Por favor, completa ambos campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón de cerrar sesión
        binding.btnLogout.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
