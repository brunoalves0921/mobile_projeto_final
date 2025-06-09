package com.example.ondetem.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Cria e lembra da instância do ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // Efeito que é executado quando a URL do vídeo muda
    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        // Opcional: exoPlayer.playWhenReady = true (para iniciar automaticamente)
    }

    // Efeito que lida com o ciclo de vida do player
    DisposableEffect(Unit) {
        onDispose {
            // Libera o player quando o componente sai da tela para evitar vazamento de memória
            exoPlayer.release()
        }
    }

    // Usa AndroidView para colocar o PlayerView (a UI do ExoPlayer) no Compose
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
            }
        },
        modifier = modifier
    )
}