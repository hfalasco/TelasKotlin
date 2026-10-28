package com.example.telaskotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

data class PostApi(val userId: Int, val id: Int, val title: String, val body: String)

data class ComentarioApi(val id: Int, val name: String, val email: String, val body: String)

suspend fun buscarPost(id: Int): PostApi = withContext(Dispatchers.IO) {
    val p = JSONObject(URL("https://jsonplaceholder.typicode.com/posts/$id").readText())
    PostApi(p.getInt("userId"), p.getInt("id"), p.getString("title"), p.getString("body"))
}

suspend fun buscarComentarios(postId: Int): List<ComentarioApi> = withContext(Dispatchers.IO) {
    val lista = JSONArray(URL("https://jsonplaceholder.typicode.com/posts/$postId/comments").readText())
    List(lista.length()) { i ->
        val c = lista.getJSONObject(i)
        ComentarioApi(c.getInt("id"), c.getString("name"), c.getString("email"), c.getString("body"))
    }
}

@Composable
fun App() {
    var post by remember { mutableStateOf<PostApi?>(null) }
    var comentarios by remember { mutableStateOf<List<ComentarioApi>>(emptyList()) }
    var erro by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(true) }
    var atualizar by remember { mutableStateOf(0) }

    LaunchedEffect(atualizar) {
        carregando = true
        erro = ""
        try {
            post = buscarPost(1)
            comentarios = buscarComentarios(1)
        } catch (e: Exception) {
            erro = "Erro ao carregar: ${e.message}"
        }
        carregando = false
    }

    TelaPost(post, comentarios, erro, carregando, onAtualizar = { atualizar++ })
}

@Composable
fun TelaPost(
    post: PostApi?,
    comentarios: List<ComentarioApi>,
    erro: String,
    carregando: Boolean,
    onAtualizar: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color(0xFFE0E0E0))) {
        Topo(carregando = carregando, onAtualizar = onAtualizar)

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (erro.isNotEmpty()) item { Text(erro) }

            post?.let { item { CardPost(it) } }

            if (comentarios.isNotEmpty()) {
                item {
                    Text(
                        "Comentários (${comentarios.size})",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(comentarios, key = { it.id }) { CardComentario(it) }
            }
        }
    }
}

@Composable
fun Topo(carregando: Boolean, onAtualizar: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Post",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (carregando) {
            CircularProgressIndicator(Modifier.padding(12.dp).size(24.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            IconButton(onClick = onAtualizar) {
                Icon(Icons.Filled.Refresh, contentDescription = "Atualizar", tint = Color.White)
            }
        }
    }
}

@Composable
fun CardPost(post: PostApi) {
    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Usuário ${post.userId}", color = Color.Gray, fontSize = 14.sp)
            Text(post.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(post.body, fontSize = 16.sp)
        }
    }
}

@Composable
fun CardComentario(comentario: ComentarioApi) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(comentario.email, fontWeight = FontWeight.Bold)
            Text(comentario.name, color = Color.Gray, fontSize = 13.sp)
            Text(comentario.body)
        }
    }
}

val postExemplo = PostApi(
    userId = 1,
    id = 1,
    title = "Meu primeiro post",
    body = "Esse é um post de exemplo para ver como a tela fica."
)

val comentarioExemplo = ComentarioApi(
    id = 1,
    name = "Adorei o post!",
    email = "ana@email.com",
    body = "Muito bom, Kotlin é demais!"
)

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    TelaPost(
        post = postExemplo,
        comentarios = listOf(comentarioExemplo),
        erro = "",
        carregando = false,
        onAtualizar = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ComentarioPreview() {
    CardComentario(comentarioExemplo)
}
