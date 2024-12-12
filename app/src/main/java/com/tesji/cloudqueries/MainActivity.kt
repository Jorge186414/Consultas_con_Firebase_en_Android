package com.tesji.cloudqueries

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.tesji.cloudqueries.ui.theme.CloudQueriesTheme

import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect


val db = FirebaseFirestore.getInstance()


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CloudQueriesTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Scaffold { paddingValues ->
        UserListScreen(Modifier.fillMaxSize().padding(paddingValues))
    }
}

@Composable
fun UserListScreen(modifier: Modifier = Modifier) {
    var userList by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    // Cargar datos desde Firestore
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { result ->
                userList = result.map { it.data }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    // Mostrar la lista de usuarios en un LazyColumn
    LazyColumn(modifier = modifier) {
        items(userList) { user ->
            Text(text = "Nombre: " + user["name"]?.toString() ?: "Unknown User")
            Text(text = "Apellido Paterno:" + user["lastName"]?.toString() ?: "Unknown User")
            Text(text = "Correo:" + user["email"]?.toString() ?: "Unknown User")
        }
    }
}