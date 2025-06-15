package com.example.ondetem.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // 1. Cria e lembra da instância do ExoPlayer durante o ciclo de vida do Composable
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // 2. Garante que o player seja liberado quando o Composable sair da tela
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 3. Observa o ciclo de vida da tela (Activity/Fragment)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // Pausa o player quando a tela está em pausa
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                // Continua a reprodução quando a tela volta
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.play()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Remove o observador ao sair
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 4. Prepara o player com a URL do vídeo.
    // Este bloco é executado na primeira vez e sempre que a 'videoUrl' mudar.
    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        // Inicia a reprodução automaticamente
        exoPlayer.playWhenReady = true
    }

    // 5. Usa AndroidView para colocar o PlayerView (a UI do player) na tela
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true // Mostra os controles de play, pause, etc.
            }
        },
        update = {
            // O update não é estritamente necessário aqui porque o `player`
            // é o mesmo, mas é boa prática garantir que ele esteja atribuído.
            it.player = exoPlayer
        }
    )
}