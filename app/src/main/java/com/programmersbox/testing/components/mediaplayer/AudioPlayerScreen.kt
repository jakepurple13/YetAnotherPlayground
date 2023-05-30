package com.programmersbox.testing.components.mediaplayer

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.annotation.CallSuper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.programmersbox.testing.ui.theme.LightAndDarkPreviews
import com.programmersbox.testing.ui.theme.TestingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AudioPlayerScreen() {
    val state = rememberAudioPlayerState("https://download.samplelib.com/mp3/sample-15s.mp3")
    val state2 =
        rememberExoPlayerAudioPlayerState("https://download.samplelib.com/mp3/sample-15s.mp3")
    val state3 =
        rememberExoPlayerNoSessionAudioPlayerState("https://download.samplelib.com/mp3/sample-15s.mp3")

    val stateList = remember {
        listOf(
            state,
            state2,
            state3
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Player") },
                actions = {
                    Text("Auto Play?")
                    Switch(
                        checked = stateList.all { it.autoPlay },
                        onCheckedChange = { stateList.forEach { s -> s.autoPlay = it } }
                    )
                }
            )
        },
        bottomBar = {
            Column {
                FlowRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            stateList.forEach { it.changeSong("https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3") }
                        }
                    ) { Text("MP3") }
                    Button(
                        onClick = {
                            stateList.forEach { it.changeSong("https://www2.cs.uic.edu/~i101/SoundFiles/BabyElephantWalk60.wav") }
                        }
                    ) { Text("WAV") }
                    Button(
                        onClick = {
                            stateList.forEach { it.changeSong("https://filesamples.com/samples/audio/m4a/sample2.m4a") }
                        }
                    ) { Text("M4A") }
                    Button(
                        onClick = {
                            stateList.forEach { it.changeSong("") }
                        }
                    ) { Text("Nothing") }
                }
                BottomAppBar {

                }
            }
        }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AudioPlayer(state = state)
            AudioPlayer(state = state2)
            AudioPlayer(state = state3)
        }
    }
}

@Composable
fun AudioPlayer(
    state: AudioPlayerState,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    tonalElevation: Dp = 4.dp,
    sliderColors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.onSurface,
        inactiveTrackColor = MaterialTheme.colorScheme.outline
    ),
) {
    AudioPlayer(
        currentPlayerPosition = state.currentPlayerPosition,
        currentSongDuration = state.currentSongDuration,
        currentPlaybackPosition = state.currentPlaybackPosition,
        onClear = state::clear,
        seekTo = state::seekTo,
        isReady = state.isReady,
        isPlaying = state.isPlaying,
        onRewind = state::rewind,
        onFastForward = state::fastForward,
        onPlayPauseToggle = state::playPause,
        shape = shape,
        tonalElevation = tonalElevation,
        sliderColors = sliderColors,
        modifier = modifier,
    )
}

@Composable
private fun AudioPlayer(
    currentPlayerPosition: Float,
    currentSongDuration: Int,
    currentPlaybackPosition: Int,
    onClear: () -> Unit,
    seekTo: (Float) -> Unit,
    isReady: Boolean,
    isPlaying: Boolean,
    onRewind: () -> Unit,
    onFastForward: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    tonalElevation: Dp = 4.dp,
    sliderColors: SliderColors = SliderDefaults.colors(),
) {
    var isSeeking by remember { mutableStateOf(false) }

    var sliderProgress by remember(
        isSeeking,
        if (isSeeking) Unit else currentPlayerPosition
    ) { mutableFloatStateOf(currentPlayerPosition) }

    DisposableEffect(Unit) {
        onDispose { onClear() }
    }
    Surface(
        shape = shape,
        tonalElevation = tonalElevation,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = sliderProgress,
                    onValueChange = {
                        sliderProgress = it
                        isSeeking = true
                    },
                    onValueChangeFinished = {
                        seekTo(sliderProgress)
                        isSeeking = false
                    },
                    colors = sliderColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Text(formatSongTime(currentPlaybackPosition))
                    Text("-${formatSongTime(currentSongDuration)}")
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onRewind) {
                    Icon(
                        imageVector = Icons.Rounded.Replay10,
                        contentDescription = "Replay 10 seconds",
                        modifier = Modifier.size(40.dp)
                    )
                }
                if (isReady) {
                    IconButton(onClick = onPlayPauseToggle) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.PauseCircleFilled else Icons.Rounded.PlayCircleFilled,
                            contentDescription = "Play",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                } else {
                    CircularProgressIndicator()
                }
                IconButton(onClick = onFastForward) {
                    Icon(
                        imageVector = Icons.Rounded.Forward10,
                        contentDescription = "Forward 10 seconds",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

private fun formatSongTime(value: Int): String {
    val dateFormat = SimpleDateFormat("m:ss", Locale.getDefault())
    return dateFormat.format(value)
}

@Composable
fun rememberAudioPlayerState(
    songUrl: String? = null,
    shouldAutoPlay: Boolean = false,
): AudioPlayerState = rememberAudioState {
    remember { AudioPlayerStateImpl(songUrl, shouldAutoPlay) }
}

@Composable
fun rememberExoPlayerAudioPlayerState(
    songUrl: String? = null,
    shouldAutoPlay: Boolean = false,
): AudioPlayerState = rememberAudioState {
    val context = LocalContext.current
    remember { ExoPlayerAudioPlayerStateImpl(songUrl, shouldAutoPlay, context) }
}

@Composable
fun rememberExoPlayerNoSessionAudioPlayerState(
    songUrl: String? = null,
    shouldAutoPlay: Boolean = false,
): AudioPlayerState = rememberAudioState {
    val context = LocalContext.current
    remember { ExoPlayerNoSessionAudioPlayerStateImpl(songUrl, shouldAutoPlay, context) }
}

@Composable
private fun rememberAudioState(
    state: @Composable () -> AudioPlayerState,
): AudioPlayerState = if (LocalInspectionMode.current) {
    remember { MockAudioPlayerState() }
} else {
    state()
}

abstract class AudioPlayerState {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    abstract val currentSongDuration: Int
    abstract var currentPlaybackPosition: Int
    abstract var autoPlay: Boolean
    abstract var isReady: Boolean
    abstract val currentPlayerPosition: Float
    abstract var isPlaying: Boolean
    abstract var hasError: Boolean
    abstract fun playPause()
    abstract fun seekTo(seek: Float)
    abstract fun fastForward()
    abstract fun rewind()
    abstract fun changeSong(url: String): Result<Unit>

    @CallSuper
    open fun clear() {
        scope.cancel()
    }
}

private class MockAudioPlayerState : AudioPlayerState() {
    override val currentSongDuration: Int get() = 0
    override var currentPlaybackPosition: Int = 0
    override var autoPlay: Boolean = false
    override var isReady: Boolean = true
    override val currentPlayerPosition: Float = 0f
    override var isPlaying: Boolean = false
    override var hasError: Boolean = false

    override fun playPause() {
        isPlaying = !isPlaying
    }

    override fun seekTo(seek: Float) {}

    override fun fastForward() {}

    override fun rewind() {}

    override fun changeSong(url: String): Result<Unit> = runCatching { }

}

private class AudioPlayerStateImpl(
    songUrl: String? = null,
    shouldAutoPlay: Boolean = false,
) : AudioPlayerState() {
    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build()
            )
            runCatching {
                songUrl?.let {
                    setDataSource(it)
                    prepareAsync()
                }
            }.onFailure { hasError = true }
            setOnPreparedListener {
                isReady = true
                if (autoPlay) it.start()
            }
        }
    }

    override var currentPlaybackPosition by mutableIntStateOf(0)

    override var autoPlay: Boolean by mutableStateOf(shouldAutoPlay)

    override var isReady by mutableStateOf(false)

    override val currentPlayerPosition: Float
        get() {
            return currentPlaybackPosition.toFloat() / currentSongDuration.coerceAtLeast(1)
        }

    override var isPlaying by mutableStateOf(false)

    override val currentSongDuration: Int get() = mediaPlayer.duration

    override var hasError: Boolean by mutableStateOf(false)

    init {
        flow {
            while (currentCoroutineContext().isActive) {
                emit(Unit)
                delay(1.milliseconds)
            }
        }
            .onEach {
                currentPlaybackPosition = mediaPlayer.currentPosition
                isPlaying = mediaPlayer.isPlaying
            }
            .launchIn(scope)
    }

    override fun playPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
    }

    override fun seekTo(seek: Float) {
        mediaPlayer.seekTo((currentSongDuration * seek).roundToInt())
    }

    override fun fastForward() {
        mediaPlayer.seekTo(mediaPlayer.currentPosition + 10 * 1000)
    }

    override fun rewind() {
        val currentPosition = mediaPlayer.currentPosition
        mediaPlayer.seekTo(if (currentPosition - 10 * 1000 < 0) 0 else currentPosition - 10 * 1000)
    }

    override fun changeSong(url: String) = runCatching {
        mediaPlayer.stop()
        mediaPlayer.reset()
        isReady = false
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
    }.onFailure { hasError = true }

    override fun clear() {
        super.clear()
        mediaPlayer.release()
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private class ExoPlayerAudioPlayerStateImpl(
    songUrl: String? = null,
    shouldAutoPlay: Boolean = false,
    context: Context,
) : AudioPlayerState() {

    override var currentPlaybackPosition by mutableIntStateOf(0)

    override var autoPlay: Boolean by mutableStateOf(shouldAutoPlay)

    override var isReady by mutableStateOf(false)

    override val currentPlayerPosition: Float
        get() {
            return currentPlaybackPosition.toFloat() / currentSongDuration.coerceAtLeast(1)
        }

    override var isPlaying by mutableStateOf(false)

    override val currentSongDuration: Int
        get() = if (::exoPlayer.isInitialized) exoPlayer.duration.toInt() else 1

    override var hasError: Boolean by mutableStateOf(false)

    val controller = MediaController.Builder(
        context,
        SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
    )
        .buildAsync()

    private lateinit var exoPlayer: MediaController

    init {
        flow {
            while (currentCoroutineContext().isActive) {
                emit(Unit)
                delay(1.milliseconds)
            }
        }
            .onEach {
                if (::exoPlayer.isInitialized)
                    exoPlayer.let {
                        currentPlaybackPosition = it.currentPosition.toInt()
                        isPlaying = it.isPlaying
                    }
            }
            .launchIn(scope)

        controller.addListener(
            {
                exoPlayer = controller.get()
                setup(controller.get(), songUrl)
            },
            MoreExecutors.directExecutor()
        )

        snapshotFlow { autoPlay }
            .onEach { if (::exoPlayer.isInitialized) exoPlayer.playWhenReady = it }
            .launchIn(scope)
    }

    private fun setup(mediaController: MediaController, songUrl: String?) {
        isReady = true
        if (!mediaController.isPlaying) {
            mediaController.playWhenReady = autoPlay
            songUrl
                ?.let { MediaItem.Builder().setMediaId(it).build() }
                ?.let { mediaController.setMediaItem(it) }
            mediaController.prepare()
        }
        mediaController.addListener(
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    hasError = true
                }
            }
        )
    }

    override fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    override fun seekTo(seek: Float) {
        exoPlayer.seekTo((currentSongDuration * seek).roundToLong())
    }

    override fun fastForward() {
        exoPlayer.seekTo(exoPlayer.currentPosition + 10 * 1000)
    }

    override fun rewind() {
        val currentPosition = exoPlayer.currentPosition
        exoPlayer.seekTo(if (currentPosition - 10 * 1000 < 0) 0 else currentPosition - 10 * 1000)
    }

    override fun changeSong(url: String) = runCatching {
        exoPlayer.stop()
        isReady = false
        exoPlayer.setMediaItem(MediaItem.Builder().setMediaId(url).build())
        exoPlayer.prepare()
        isReady = true
    }.onFailure {
        hasError = true
        it.printStackTrace()
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setMediaSourceFactory(ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory()))
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(CustomMediaSessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private inner class CustomMediaSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems
                .map { it.buildUpon().setUri(it.mediaId).build() }
                .toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private class ExoPlayerNoSessionAudioPlayerStateImpl(
    songUrl: String? = null,
    shouldAutoPlay: Boolean = false,
    context: Context,
) : AudioPlayerState() {

    private val exoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.DEFAULT,
                true
            )
            .setMediaSourceFactory(ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory()))
            .build()
    }

    override var currentPlaybackPosition by mutableIntStateOf(0)

    override var autoPlay: Boolean by mutableStateOf(shouldAutoPlay)

    override var isReady by mutableStateOf(false)

    override val currentPlayerPosition: Float
        get() {
            return currentPlaybackPosition.toFloat() / currentSongDuration.coerceAtLeast(1)
        }

    override var isPlaying by mutableStateOf(false)

    override val currentSongDuration: Int get() = exoPlayer.duration.toInt()

    override var hasError: Boolean by mutableStateOf(false)

    init {
        flow {
            while (currentCoroutineContext().isActive) {
                emit(Unit)
                delay(1.milliseconds)
            }
        }
            .onEach {
                exoPlayer.let {
                    currentPlaybackPosition = it.currentPosition.toInt()
                    isPlaying = it.isPlaying
                }
            }
            .launchIn(scope)

        snapshotFlow { autoPlay }
            .onEach { exoPlayer.playWhenReady = it }
            .launchIn(scope)

        setup(songUrl)
    }

    private fun setup(songUrl: String?) {
        isReady = true
        exoPlayer.playWhenReady = autoPlay
        songUrl
            ?.let { MediaItem.fromUri(it) }
            ?.let { exoPlayer.setMediaItem(it) }
        exoPlayer.prepare()
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    hasError = true
                }
            }
        )
    }

    override fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    override fun seekTo(seek: Float) {
        exoPlayer.seekTo((currentSongDuration * seek).roundToLong())
    }

    override fun fastForward() {
        exoPlayer.seekTo(exoPlayer.currentPosition + 10 * 1000)
    }

    override fun rewind() {
        val currentPosition = exoPlayer.currentPosition
        exoPlayer.seekTo(if (currentPosition - 10 * 1000 < 0) 0 else currentPosition - 10 * 1000)
    }

    override fun changeSong(url: String) = runCatching {
        exoPlayer.stop()
        isReady = false
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        isReady = true
    }.onFailure {
        hasError = true
        it.printStackTrace()
    }
}

@LightAndDarkPreviews
@Composable
private fun AudioPlayerPreview() {
    TestingTheme {
        AudioPlayer(
            state = rememberAudioPlayerState()
        )
    }
}