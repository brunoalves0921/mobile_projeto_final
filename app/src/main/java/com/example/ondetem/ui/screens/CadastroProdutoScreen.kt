package com.example.ondetem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ondetem.data.Produto
import com.example.ondetem.data.ProdutoRepository
import kotlin.random.Random

@Composable
fun CadastroProdutoScreen(
    nomeLoja: String,
    onProdutoCadastrado: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var imagemUrl by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()), // Para rolar a tela se o teclado cobrir
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Adicionar Produto à Loja", style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Produto") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = preco, onValueChange = { preco = it }, label = { Text("Preço (Ex: R$ 19,99)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = imagemUrl, onValueChange = { imagemUrl = it }, label = { Text("URL da Imagem") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (nome.isNotBlank() && descricao.isNotBlank() && preco.isNotBlank()) {
                    val novoProduto = Produto(
                        id = Random.nextInt(), // ID aleatório simples por enquanto
                        nome = nome,
                        descricao = descricao,
                        preco = preco,
                        imagemUrl = imagemUrl,
                        loja = nomeLoja, // Vincula o produto à loja atual
                        // Campos de endereço da loja podem ficar vazios aqui, pois já estão na loja
                        endereco = "",
                        telefone = ""
                    )
                    ProdutoRepository.salvar(context, novoProduto)
                    Toast.makeText(context, "Produto cadastrado!", Toast.LENGTH_SHORT).show()
                    onProdutoCadastrado()
                } else {
                    Toast.makeText(context, "Preencha os campos obrigatórios.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Produto")
        }
    }
}