package com.tesji.cloudqueries
import com.tesji.cloudqueries.ui.theme.CloudQueriesTheme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.Modifier
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CloudQueriesTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "user_list") {
        composable("user_list") {
            UserListScreen(navController)
        }
        composable("add_user") {
            AddUserScreen(navController)
        }
    }
}

@Composable
fun UserListScreen(navController: NavHostController, viewModel: UserViewModel = viewModel()) {
    val userList by viewModel.userList.collectAsState()

    // Recarga los usuarios cada vez que esta pantalla entra en el foco
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_user") }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            items(userList) { user ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Name: ${user["name"] ?: "Unknown"}")
                    Text(text = "Last Name: ${user["lastName"] ?: "Unknown"}")
                    Text(text = "Email: ${user["email"] ?: "Unknown"}")
                }
            }
        }
    }
}

@Composable
fun AddUserScreen(navController: NavHostController, viewModel: UserViewModel = viewModel()) {
    // Campos de entrada de texto
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var lastName by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }

    // Estado para mostrar mensajes
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Campo para ingresar el nombre
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Campo para ingresar el apellido
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Campo para ingresar el correo electrónico
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para guardar los datos
        Button(
            onClick = {
                if (name.text.isNotEmpty() && lastName.text.isNotEmpty() && email.text.isNotEmpty()) {
                    viewModel.addUser(name.text, lastName.text, email.text)
                    navController.popBackStack() // Vuelve a la lista de usuarios
                } else {
                    message = "Please fill in all fields"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        // Mostrar mensaje de estado
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ViewModel para manejar la lógica
class UserViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _userList = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val userList: StateFlow<List<Map<String, Any>>> = _userList.asStateFlow()

    fun loadUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                _userList.value = result.map { it.data }
            }
            .addOnFailureListener { e ->
                println("Error loading users: ${e.message}")
            }
    }

    fun addUser(name: String, lastName: String, email: String) {
        val user = hashMapOf(
            "name" to name,
            "lastName" to lastName,
            "email" to email
        )
        db.collection("users")
            .add(user)
            .addOnSuccessListener {
                loadUsers() // Actualiza la lista tras agregar el usuario
            }
            .addOnFailureListener { e ->
                println("Error adding user: ${e.message}")
            }
    }

    init {
        loadUsers()
    }
}
