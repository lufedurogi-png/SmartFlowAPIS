package com.example.smartflow

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    // URL de tu API - CAMBIA ESTA URL por la de tu servidor
    // private val API_BASE_URL = "http://10.0.2.2:3000/api" // Para emulador Android
    // private val API_BASE_URL = "http://192.168.1.13:3000/api" // Para dispositivo físico
    private val API_BASE_URL = "https://smartflow-mwmm.onrender.com/api" // Para producción en Render
    private val LOGIN_ENDPOINT = "$API_BASE_URL/auth/login"

    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvLoginError = findViewById<TextView>(R.id.tvLoginError)

        // Inicializar RequestQueue
        requestQueue = Volley.newRequestQueue(this)

        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                tvLoginError.text = "Por favor completa todos los campos"
                tvLoginError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            // Llamar a la función de login
            loginUser(email, password, tvLoginError)
        }
    }

    private fun loginUser(email: String, password: String, tvLoginError: TextView) {
        // Crear JSON con los datos del login
        val loginData = JSONObject().apply {
            put("correo_user", email)
            put("password_user", password)
        }

        // Crear petición HTTP
        val jsonRequest = JsonObjectRequest(
            Request.Method.POST,
            LOGIN_ENDPOINT,
            loginData,
            { response ->
                // Login exitoso
                try {
                    val message = response.getString("message")
                    val token = response.getString("token")
                    val user = response.getJSONObject("user")
                    val userRole = user.getString("rol_user")
                    val userName = user.getString("name_user") // Obtener el nombre del usuario

                    // Guardar token y datos del usuario
                    saveUserData(token, user)

                    // Mostrar mensaje de éxito
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                    // Ocultar mensaje de error
                    tvLoginError.visibility = TextView.GONE

                    // Redirigir según el rol del usuario
                    redirectUserByRole(userRole, userName) // Pasar el nombre del usuario

                } catch (e: Exception) {
                    Log.e("LoginActivity", "Error parsing response", e)
                    tvLoginError.text = "Error procesando respuesta del servidor"
                    tvLoginError.visibility = TextView.VISIBLE
                }
            },
            { error ->
                // Error en el login
                Log.e("LoginActivity", "Login error", error)

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Credenciales inválidas"
                    401 -> "Email o contraseña incorrectos"
                    500 -> "Error interno del servidor"
                    else -> "Error de conexión. Verifica tu internet"
                }

                tvLoginError.text = errorMessage
                tvLoginError.visibility = TextView.VISIBLE
            }
        )

        // Agregar petición a la cola
        requestQueue.add(jsonRequest)
    }

    private fun saveUserData(token: String, user: JSONObject) {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("auth_token", token)
        editor.putString("user_id", user.getString("_id"))
        editor.putString("user_name", user.getString("name_user"))
        editor.putString("user_email", user.getString("correo_user"))
        editor.putString("user_role", user.getString("rol_user"))
        editor.putBoolean("user_active", user.getBoolean("estado_user"))

        // Debug: Ver todo el objeto user
        Log.d("LoginActivity", "=== DEBUG IMAGEN ===")
        Log.d("LoginActivity", "User JSON completo: $user")

        // Verificar y guardar imagen_url (puede ser Base64 o URL)
        if (user.has("imagen_url") && !user.isNull("imagen_url")) {
            val imageUrl = user.getString("imagen_url")
            
            // Verificar si es Base64 o URL normal
            if (imageUrl.startsWith("data:image")) {
                // Es Base64
                editor.putString("user_image_base64", imageUrl)
                editor.remove("user_image_url") // Limpiar URL si existe
                Log.d("LoginActivity", "✅ Imagen Base64 guardada (${imageUrl.length} caracteres)")
            } else {
                // Es URL normal
                editor.putString("user_image_url", imageUrl)
                editor.remove("user_image_base64") // Limpiar Base64 si existe
                Log.d("LoginActivity", "✅ URL de imagen guardada: $imageUrl")
            }
        } else {
            Log.d("LoginActivity", "❌ NO se encontró imagen_url en JSON")
            // Limpiar ambos campos
            editor.remove("user_image_url")
            editor.remove("user_image_base64")
            
            // Mostrar todos los campos disponibles para debugging
            val keys = user.keys()
            val keysList = mutableListOf<String>()
            while (keys.hasNext()) {
                keysList.add(keys.next())
            }
            Log.d("LoginActivity", "Campos disponibles: $keysList")
        }

        editor.apply()
    }

    // Función para obtener el token guardado (usar en otras actividades)
    fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    // Función para obtener el rol del usuario guardado
    fun getUserRole(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_role", null)
    }

    // Función para verificar si hay una sesión activa
    fun isUserLoggedIn(): Boolean {
        val token = getAuthToken()
        val role = getUserRole()
        return !token.isNullOrEmpty() && !role.isNullOrEmpty()
    }

    // Función para redirigir según el rol del usuario
    private fun redirectUserByRole(userRole: String, userName: String) {
        when (userRole) {
            "Empleado" -> {
                // Empleados van a la app móvil - MainEmpleado
                Toast.makeText(this, "Bienvenido Empleado", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainEmpleadoActivity::class.java)
                intent.putExtra("USER_NAME", userName) // Pasar el nombre del usuario
                startActivity(intent)
                finish()
            }
            "Auditor" -> {
                // Auditores van a la app móvil - MainAuditor
                Toast.makeText(this, "Bienvenido Auditor", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainAuditorActivity::class.java)
                intent.putExtra("USER_NAME", userName) // Pasar el nombre del usuario
                startActivity(intent)
                finish()
            }
            else -> {
                // Rol no reconocido - ir a actividad por defecto
                Toast.makeText(this, "Rol de usuario no reconocido", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
