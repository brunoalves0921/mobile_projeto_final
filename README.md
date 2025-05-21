# Onde Tem?

## Universidade Federal do Ceará – Campus Quixadá

**Disciplina:** QXD0276 - Desenvolvimento de Software para Dispositivos Móveis

**Professor:** Francisco Victor da Silva Pinheiro

**Ano:** 2025

---

## 1. Integrantes da Dupla

| Matrícula | Nome Completo | E-mail |
|-----------|----------------|--------|
| 509718 | JORGE BRUNO COSTA ALVES | jorge.bruno0921@alu.ufc.br |

---

## 2. Resumo da Entrega

Este projeto é a segunda etapa do trabalho final da disciplina e consiste na construção da primeira versão funcional da aplicação *Onde Tem?*, utilizando **Jetpack Compose** e **Kotlin**. A proposta é facilitar a busca de produtos em lojas físicas próximas ao usuário, conectando consumidores e comerciantes locais.

A aplicação foi desenvolvida com foco em:

- Navegação entre telas usando `NavController`, `Scaffold`, `TopAppBar` e `BottomNavigation`
- Layouts dinâmicos com `LazyColumn` e `LazyRow`
- Tema claro/escuro
- Multimídia simulada (reprodução de vídeo)
- Gerenciamento de estado com `remember`, `mutableStateOf`, `rememberSaveable`

---

## 3. Repositório de Código e Vídeo

- 🔗 **Repositório [GitHub](https://github.com/brunoalves0921/mobile_projeto_final)**
- 🎥 **[Vídeo](https://github.com/brunoalves0921/) de Apresentação:**
- 📦 **Download [APK](https://github.com/brunoalves0921/mobile_projeto_final/tree/main/releases/download/v1.0.0/ondetem.apk)**


---

## 4. Funcionalidades Implementadas

| Funcionalidade                                          | Status       | Responsável           |
|----------------------------------------------------------|--------------|------------------------|
| Tela inicial com busca e lista de produtos               | ✅ Concluído | Jorge Bruno |
| Tela de detalhes com informações e vídeo e botão de favoritar                | ✅ Concluído | Jorge Bruno |
| Tela de favoritos                                         | ✅ Concluído | Jorge Bruno |
| Tela de configurações com modo escuro e ações            | ✅ Concluído | Jorge Bruno |
| Tela de ajuda com FAQs e simulação de envio de mensagem  | ✅ Concluído | Jorge Bruno |
| Menu de três pontinhos com navegação                     | ✅ Concluído | Jorge Bruno |
| Tema claro/escuro dinâmico                               | ✅ Concluído | Jorge Bruno |
| Uso de dados mockados para os produtos                   | ✅ Concluído | Jorge Bruno |
| Logo no TopAppBar (Texto) e ícone do app (Imagem)                    | ✅ Concluído | Jorge Bruno |

---

## 5. Capturas de Tela

> (adicione as imagens abaixo com legenda usando `![Legenda](caminho/arquivo.png)`)

- 🏠 Tela Inicial
- 🔍 Campo de Busca
- 📄 Tela de Detalhes com vídeo
- ❤️ Tela de Favoritos
- ⚙️ Tela de Configurações com switches
- ❓ Tela de Ajuda com FAQ e formulário
- 🧭 TopAppBar com logo (TEXTO) e menu
- 🌙 Modo Escuro ativo

---

## 6. Arquitetura e Organização

O projeto segue o padrão **MVVM (Model-View-ViewModel)** com a seguinte organização:

```
com.example.ondetem/
│
├── data/ // Modelos e dados mockados
│ ├── Produto.kt // Classe de dados do produto
│ └── MockData.kt // Lista de produtos simulados
│
├── viewmodel/ // Lógica de estado e ações do app
│ └── ProdutoViewModel.kt // ViewModel principal com estado das telas
│
├── ui/ // Interface do usuário
│ ├── components/ // Componentes reutilizáveis da UI
│ │ ├── ProdutoCard.kt // Card visual dos produtos
│ │ └── TopBar.kt // TopAppBar com logo (Texto) e menu
│ │
│ ├── screens/ // Telas principais do app
│ │ ├── HomeScreen.kt // Tela inicial com busca e listagem
│ │ ├── DetalhesScreen.kt // Tela de detalhes do produto
│ │ ├── FavoritosScreen.kt // Tela com produtos favoritados
│ │ ├── ConfiguracoesScreen.kt // Tela de preferências (modo escuro etc.)
│ │ └── AjudaScreen.kt // Tela de perguntas frequentes e suporte
│ │
│ └── MainScreen.kt // Gerencia navegação e scaffold geral
│
├── ui/theme/ // Tema visual do app
│ └── AppTheme.kt // Tema dinâmico claro/escuro com Material 3
│
└── MainActivity.kt // Ponto de entrada da aplicação
```

- O gerenciamento de estado foi feito com `mutableStateOf` e `rememberSaveable`
- A navegação entre telas é feita com `NavHost` e `NavController`
- O tema claro/escuro é aplicado com `MaterialTheme` e `isSystemInDarkTheme`

---

## 7. Dificuldades Encontradas

Durante o desenvolvimento deste projeto, algumas dificuldades técnicas e conceituais foram enfrentadas. Uma das principais foi a implementação de um modo escuro funcional que se aplicasse dinamicamente a todo o aplicativo. Foi necessário compreender bem o funcionamento do `MaterialTheme`, bem como como manipular estados globais em Compose.



A construção do menu de três pontinhos também exigiu atenção especial, pois deveria estar presente em todas as telas principais e funcionar em conjunto com a navegação por `NavController`.

Além disso, como o projeto foi desenvolvido individualmente, a carga de organização e execução das tarefas foi integralmente assumida por mim, o que exigiu bastante planejamento, disciplina e gerenciamento de tempo para cumprir os requisitos e manter a qualidade visual e técnica da aplicação.

Por fim, minha maior dificuldade esteve no próprio desenvolvimento da aplicação. Esta foi minha primeira experiência prática utilizando o Android Studio, uma ferramenta robusta e que, para iniciantes, pode parecer complexa em vários aspectos — desde a estrutura de projeto até a configuração de dependências.

Além disso, o uso da linguagem Kotlin também foi um desafio, já que nunca havia trabalhado com ela antes. Foi necessário aprender não só a sintaxe, mas também conceitos específicos como `State`, `Composable`, `Scaffold`, e a abordagem declarativa do Jetpack Compose, que difere bastante das abordagens mais tradicionais que eu estava acostumado.

Apesar das dificuldades, considero que o processo foi extremamente valioso para o meu aprendizado e crescimento como desenvolvedor.



---

