plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // ADICIONE ESTA LINHA:
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.ondetem"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ondetem"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // MUDE A VERSÃO DO JAVA PARA 1.8, É MAIS COMPATÍVEL COM BIBLIOTECAS ANTIGAS
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8" // MUDE AQUI TAMBÉM
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Suas libs principais
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- ADICIONE ESTAS DUAS LINHAS PARA O PLAYER DE VÍDEO ---
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    // --- FIM DA ADIÇÃO ---

    // --- DEPENDÊNCIAS DO FIREBASE ---
    // Importa a Bill of Materials (BoM) para gerenciar as versões
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    // Autenticação
    implementation("com.google.firebase:firebase-auth")
    // Banco de Dados Firestore
    implementation("com.google.firebase:firebase-firestore")
    // Armazenamento de Mídia
    implementation("com.google.firebase:firebase-storage")
    // Biblioteca de ajuda para usar Coroutines com Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    // --- FIM DAS DEPENDÊNCIAS DO FIREBASE ---

    implementation("com.google.accompanist:accompanist-permissions:0.31.5-beta")

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1") // Versão atualizada

    // Gson para salvar e ler dados JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Navegação
    implementation("androidx.navigation:navigation-compose:2.7.7") // Versão atualizada

    // Coil para carregar imagens
    implementation("io.coil-kt:coil-compose:2.6.0") // Versão atualizada

    // Suporte para VideoView
    implementation("androidx.compose.foundation:foundation:1.6.8") // Versão atualizada

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}