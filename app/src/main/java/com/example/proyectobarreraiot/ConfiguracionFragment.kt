package com.example.proyectobarreraiot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proyectobarreraiot.databinding.FragmentConfiguracionBinding
import com.google.firebase.auth.FirebaseAuth

class ConfiguracionFragment : Fragment() {

    private var _binding: FragmentConfiguracionBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    // valores de peso por defecto
    private var pesoMinimo: Float = 500f
    private var pesoMaximo: Float = 1000f

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

        val pesoMinimo = sharedPref.getFloat("peso_minimo", pesoMinimo)
        val pesoMaximo = sharedPref.getFloat("peso_maximo", pesoMaximo)

        // Guardar y mostrar los valores por defecto
        binding.etPesoMinimo.setText(pesoMinimo.toString())
        binding.etPesoMaximo.setText(pesoMaximo.toString())
        if (!sharedPref.contains("peso_minimo")) {
            with(sharedPref.edit()) {
                putFloat("peso_minimo", pesoMinimo)
                putFloat("peso_maximo", pesoMaximo)
                apply()
            }
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
