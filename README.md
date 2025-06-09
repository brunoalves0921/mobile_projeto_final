# Onde Tem?

## Universidade Federal do Ceará – Campus Quixadá

**Disciplina:** QXD0256 - Desenvolvimento de Software para Dispositivos Móveis

**Professor:** Francisco Victor da Silva Pinheiro

**Ano:** 2025

---

## 1. Integrantes

| Matrícula | Nome Completo             | E-mail                    |
| :-------- | :------------------------ | :------------------------ |
| 509718    | JORGE BRUNO COSTA ALVES   | jorge.bruno0921@alu.ufc.br |

---

## 2. Resumo da Entrega

Este projeto representa a versão final e consolidada do aplicativo *Onde Tem?*, uma plataforma que conecta consumidores a comerciantes locais. Construído inteiramente com **Jetpack Compose** e **Kotlin**, o aplicativo evoluiu para uma solução robusta que gerencia dois fluxos de usuário distintos: Cliente e Vendedor.

Nesta fase final, o projeto implementa um ciclo completo de gerenciamento de dados pelo vendedor — desde a autenticação e cadastro de lojas até a adição, edição e exclusão de produtos com mídias persistentes. A aplicação agora utiliza uma arquitetura MVVM refinada, com o `ViewModel` atuando como uma fonte de dados única para a UI e os repositórios gerenciando a persistência de dados em arquivos JSON locais. Foram implementadas funcionalidades avançadas como `Jetpack DataStore` para preferências do usuário, `BroadcastReceiver` para notificações e uma interface de usuário interativa com animações e feedback visual, resultando em um aplicativo mais robusto e acessível.

---

## 3. Repositório de Código e Vídeo

- 🎥 **[Vídeo de Apresentação](http://SEU_LINK_AQUI):** (Link para o vídeo de demonstração de 5 minutos)
- 📦 **[Download APK](http://SEU_LINK_AQUI):** (Link para o arquivo `.apk` da release no GitHub)

---

## 4. Funcionalidades Implementadas

| Funcionalidade | Status | Responsável |
| :--- | :--- | :--- |
| **Interface e Navegação (Cliente)** | | |
| Tela inicial com busca de produtos | ✅ Concluído | Jorge Bruno |
| Tela de detalhes com informações, fotos e vídeo | ✅ Concluído | Jorge Bruno |
| Navegação com TopAppBar e BottomBar | ✅ Concluído | Jorge Bruno |
| Tela de Favoritos | ✅ Concluído | Jorge Bruno |
| **Fluxo de Vendedor (Autenticação e Gerenciamento)** | | |
| Tela de seleção de perfil (Cliente/Vendedor) | ✅ Concluído | Jorge Bruno |
| Telas de Login e Cadastro para Vendedor | ✅ Concluído | Jorge Bruno |
| Persistência de dados do vendedor em arquivo JSON | ✅ Concluído | Jorge Bruno |
| Tela de perfil do vendedor com listagem de lojas | ✅ Concluído | Jorge Bruno |
| Cadastro de novas lojas | ✅ Concluído | Jorge Bruno |
| Tela de detalhes da loja com listagem de produtos | ✅ Concluído | Jorge Bruno |
| **Gerenciamento de Produtos (CRUD Completo)** | | |
| Cadastro de novos produtos vinculado a uma loja | ✅ Concluído | Jorge Bruno |
| Anexar foto/vídeo da galeria no cadastro do produto | ✅ Concluído | Jorge Bruno |
| Persistência de mídias (cópia para armazenamento interno) | ✅ Concluído | Jorge Bruno |
| Edição de informações e mídias de produtos existentes | ✅ Concluído | Jorge Bruno |
| Exclusão de produtos com diálogo de confirmação | ✅ Concluído | Jorge Bruno |
| **Funcionalidades Avançadas** | | |
| Persistência de preferências com Jetpack DataStore | ✅ Concluído | Jorge Bruno |
| ᐅ Salvar/Carregar preferência de Modo Escuro | ✅ Concluído | Jorge Bruno |
| ᐅ Salvar/Carregar lista de produtos favoritos | ✅ Concluído | Jorge Bruno |
| ᐅ Salvar/Carregar preferência de ativação de notificações | ✅ Concluído | Jorge Bruno |
| Agendamento de notificações de lembrete com BroadcastReceiver | ✅ Concluído | Jorge Bruno |
| Indicador de progresso (`CircularProgressIndicator`) na busca | ✅ Concluído | Jorge Bruno |
| Animações de transição de tela (fade-in/out) | ✅ Concluído | Jorge Bruno |
| Animação no botão de favoritar | ✅ Concluído | Jorge Bruno |
| **Ajuda e Configurações** | | |
| Tela de Configurações com controles funcionais | ✅ Concluído | Jorge Bruno |
| Tela de Ajuda com FAQ e formulário de contato | ✅ Concluído | Jorge Bruno |

---

## 5. Capturas de Tela

- 🏠 Tela Inicial (Cliente, antes da busca)
- 🔍 Tela de Busca (Cliente, com resultados)
- 📄 Tela de Detalhes do Produto (com foto e/ou vídeo)
- ❤️ Tela de Favoritos (com itens salvos)
- ⚙️ Tela de Configurações
- 🌙 Modo Escuro ativo
- 🔔 Notificação de Lembrete na bandeja do sistema
- 👤 Tela de Seleção de Perfil (Cliente/Vendedor)
- 🔑 Tela de Login e Cadastro do Vendedor
- 🏪 Tela de Perfil do Vendedor (com a lista de suas lojas)
- 📝 Tela de Detalhes da Loja (com a lista de seus produtos)
- ✨ Tela de Adicionar/Editar Produto (com campos preenchidos)

---

## 6. Arquitetura e Organização

O projeto segue o padrão de arquitetura **MVVM (Model-View-ViewModel)**, estruturado para uma clara separação de responsabilidades.
```
com.example.ondetem
│
├── data/
│   ├── Loja.kt
│   ├── LojaRepository.kt
│   ├── Produto.kt
│   ├── ProdutoRepository.kt
│   ├── SettingsDataStore.kt
│   ├── Vendedor.kt
│   └── VendedorRepository.kt
│
├── notifications/
│   └── NotificationReceiver.kt
│
├── ui/
│   ├── components/
│   │   ├── ProdutoCard.kt
│   │   ├── ProdutoItemRow.kt
│   │   └── TopBar.kt
│   │
│   ├── screens/
│   │   ├── AjudaScreen.kt
│   │   ├── CadastroLojaScreen.kt
│   │   ├── CadastroProdutoScreen.kt
│   │   ├── CadastroScreen.kt
│   │   ├── ConfiguracoesScreen.kt
│   │   ├── DetalhesLojaScreen.kt
│   │   ├── DetalhesScreen.kt
│   │   ├── FavoritosScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── PerfilVendedorScreen.kt
│   │   ├── RoleSelectionScreen.kt
│   │   └── VendedorHomeScreen.kt
│   │
│   ├── theme/
│   │   └── (Arquivos de tema: Color.kt, Theme.kt, etc.)
│   │
│   └── MainScreen.kt
│
├── viewmodel/
│   └── ProdutoViewModel.kt
│
└── MainActivity.kt
```

- **Gerenciamento de Estado:** O estado global (modo escuro, favoritos, etc.) é gerenciado na `MainActivity` e lido a partir do **Jetpack DataStore**. O `ProdutoViewModel`, como um `AndroidViewModel`, centraliza a lógica de negócios e o estado da UI para o fluxo do cliente, atuando como uma fonte única de verdade e garantindo que os dados estejam sempre atualizados na tela.

- **Persistência de Dados:**
  - **Preferências do Usuário:** Utiliza-se `Jetpack DataStore` para um armazenamento assíncrono e eficiente.
  - **Dados do Vendedor:** As informações de vendedores, lojas e produtos são serializadas e persistidas em arquivos JSON no armazenamento interno do aplicativo, gerenciadas pelas classes `Repository`.

- **Persistência de Mídia:** Para contornar as restrições de acesso do Android (Scoped Storage), os arquivos de imagem e vídeo selecionados da galeria são copiados para o armazenamento interno do aplicativo. O caminho absoluto e permanente desse arquivo é salvo, garantindo que a mídia sempre possa ser acessada e exibida.

- **Notificações:** O agendamento é feito na tela de detalhes do produto usando `AlarmManager`, que por sua vez ativa um `BroadcastReceiver` no tempo configurado para criar e exibir a notificação para o usuário.




