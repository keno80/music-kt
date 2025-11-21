package com.example.musickt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import android.media.MediaMetadataRetriever
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.musickt.player.MusicPlayer
import com.example.musickt.player.MusicPlayerHolder
import com.example.musickt.ui.components.AnimatedGradientBackground
import com.example.musickt.ui.components.MusicListItem
import com.example.musickt.ui.components.MusicPlayerBar
import com.example.musickt.ui.album.AlbumsGrid
import com.example.musickt.ui.artist.ArtistsGrid
import com.example.musickt.ui.components.SongsList
import com.example.musickt.buildAlbums
import com.example.musickt.buildArtists
import com.example.musickt.ui.theme.MusicKtTheme
import com.example.musickt.ui.theme.dominantColors
import com.example.musickt.ui.theme.lighten
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val musicList = mutableStateListOf<MusicItem>()
    private val sortedMusicList = mutableStateListOf<MusicItem>()
    private lateinit var musicPlayer: MusicPlayer

    private val scanCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadMusicList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        musicPlayer = MusicPlayerHolder.get(this)
        registerScanReceiver()
        loadMusicList()
        
        setContent {
            MusicKtTheme(currentMusic = musicPlayer.currentMusic) {
                MainScreen(
                    musicList = sortedMusicList,
                    musicPlayer = musicPlayer,
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                )
            }
        }
    }

    private fun registerScanReceiver() {
        val filter = IntentFilter(SettingsActivity.SCAN_COMPLETE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(scanCompleteReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(scanCompleteReceiver, filter)
        }
    }

    override fun onResume() {
        super.onResume()
        loadMusicList()
    }

    private fun loadMusicList() {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            val cached = MusicCache.load(this@MainActivity)
            val processed = withContext(Dispatchers.Default) { preprocessAndSort(cached) }
            withContext(Dispatchers.Main) {
                musicList.clear()
                musicList.addAll(cached)
                sortedMusicList.clear()
                sortedMusicList.addAll(processed)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(scanCompleteReceiver)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    musicList: List<MusicItem>,
    musicPlayer: MusicPlayer,
    onSettingsClick: () -> Unit
) {
    var currentMusicIndex by remember { mutableStateOf(-1) }
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    
    LaunchedEffect(musicPlayer.isPlaying) {
        while (musicPlayer.isPlaying) {
            musicPlayer.updateProgress()
            delay(100)
        }
    }
    
    AnimatedGradientBackground(currentMusic = musicPlayer.currentMusic, colorTransitionDurationMs = 900) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Zenly") },
                    actions = {
                        val bitmap = remember(musicPlayer.currentMusic?.path) {
                            musicPlayer.currentMusic?.path?.let { p ->
                                try {
                                    val mmr = MediaMetadataRetriever()
                                    mmr.setDataSource(p)
                                    val art = mmr.embeddedPicture
                                    mmr.release()
                                    art?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                                } catch (_: Exception) {
                                    null
                                }
                            }
                        }
                        val dom = remember(bitmap) { bitmap?.let { dominantColors(it) } }
                        val base = dom?.getOrNull(0) ?: MaterialTheme.colorScheme.surfaceVariant
                        val targetCapsule = lighten(base, 0.18f)
                        val capsuleColor by animateColorAsState(targetValue = targetCapsule, animationSpec = tween(500, easing = FastOutSlowInEasing), label = "settingsCapsule")
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                color = capsuleColor,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                shape = CircleShape
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(onClick = onSettingsClick),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "设置",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
            val displayMusic by remember { derivedStateOf { sortMusicList(musicList) } }
            MusicPlayerBar(
                currentMusic = musicPlayer.currentMusic,
                isPlaying = musicPlayer.isPlaying,
                currentPosition = musicPlayer.currentPosition,
                duration = musicPlayer.duration,
                modifier = Modifier.padding(bottom = 8.dp),
                onPlayPauseClick = {
                    if (musicPlayer.isPlaying) {
                        musicPlayer.pause()
                    } else {
                        musicPlayer.resume()
                    }
                },
                onNextClick = {
                    if (currentMusicIndex < displayMusic.size - 1) {
                        currentMusicIndex++
                        musicPlayer.play(displayMusic[currentMusicIndex])
                    }
                },
                onPreviousClick = {
                    if (currentMusicIndex > 0) {
                        currentMusicIndex--
                        musicPlayer.play(displayMusic[currentMusicIndex])
                    }
                },
                colorTransitionDurationMs = 500
            )
        }
    ) { paddingValues ->
        val albums by remember { derivedStateOf { buildAlbums(musicList) } }
        val artists by remember { derivedStateOf { buildArtists(musicList) } }
        val sortedMusic by remember { derivedStateOf { sortMusicList(musicList) } }
        HorizontalPager(
            state = pagerState,
            flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            if (page == 0) {
                AnimatedContent(targetState = albums.isEmpty(), transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                }, label = "albumsContent") { empty ->
                    if (empty) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "暂无专辑", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        AlbumsGrid(albums = albums)
                    }
                }
            } else if (page == 1) {
                AnimatedContent(targetState = sortedMusic.isEmpty(), transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                }, label = "songsContent") { empty ->
                    if (empty) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "未发现音乐", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "请前往设置页扫描音乐", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = onSettingsClick) { Text("去设置扫描") }
                            }
                        }
                    } else {
                        SongsList(
                            musicList = sortedMusic,
                            isPlayingId = musicPlayer.currentMusic?.id,
                            onItemClick = { index, item ->
                                currentMusicIndex = index
                                musicPlayer.play(item)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                AnimatedContent(targetState = artists.isEmpty(), transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                }, label = "artistsContent") { empty ->
                    if (empty) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "暂无艺术家", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        ArtistsGrid(artists = artists)
                    }
                }
            }
        }
    }
    }
}

private var transliteratorCached: Any? = null
private var transliterateMethodCached: java.lang.reflect.Method? = null
private fun initTransliterator() {
    if (transliterateMethodCached != null) return
    if (Build.VERSION.SDK_INT >= 24) {
        try {
            val cls = Class.forName("android.icu.text.Transliterator")
            val getInstance = cls.getMethod("getInstance", String::class.java)
            transliteratorCached = getInstance.invoke(null, "Han-Latin/Names; Latin-ASCII")
            transliterateMethodCached = cls.getMethod("transliterate", String::class.java)
        } catch (_: Exception) { transliterateMethodCached = null; transliteratorCached = null }
    }
}

private fun toHalfWidth(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
        val code = ch.code
        if (code == 12288) sb.append(' ')
        else if (code in 65281..65374) sb.append((code - 65248).toChar())
        else sb.append(ch)
    }
    return sb.toString()
}

private fun transliterateToAsciiCached(s: String): String {
    val t = toHalfWidth(s).trim()
    if (t.isEmpty()) return ""
    initTransliterator()
    val m = transliterateMethodCached
    val inst = transliteratorCached
    if (m != null && inst != null) {
        try {
            val out = m.invoke(inst, t) as String
            return java.text.Normalizer.normalize(out, java.text.Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
        } catch (_: Exception) { /* fall through */ }
    }
    return t
}

private fun preprocessAndSort(list: List<MusicItem>): List<MusicItem> {
    return list.map { item ->
        val t = toHalfWidth(item.title).trim()
        val group: Int
        val letter: Char
        if (t.isEmpty()) {
            group = 2; letter = '#'
        } else {
            val c = t.first()
            if (c.isDigit()) { group = 0; letter = '0' }
            else if (c in 'A'..'Z' || c in 'a'..'z') { group = 1; letter = c.uppercaseChar() }
            else {
                val ascii = transliterateToAsciiCached(t)
                val first = ascii.firstOrNull { it.isLetter() }?.uppercaseChar() ?: '#'
                group = 1; letter = first
            }
        }
        val asciiFull = transliterateToAsciiCached(t).lowercase()
        item.copy(sortGroup = group, sortLetter = letter, sortAscii = asciiFull)
    }.sortedWith(compareBy(
        { it.sortGroup },
        { it.sortLetter },
        { it.sortAscii }
    ))
}

private fun sortMusicList(list: List<MusicItem>): List<MusicItem> {
    fun toHalfWidth(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val code = ch.code
            if (code == 12288) sb.append(' ')
            else if (code in 65281..65374) sb.append((code - 65248).toChar())
            else sb.append(ch)
        }
        return sb.toString()
    }
    fun transliterateToAscii(s: String): String {
        val t = toHalfWidth(s).trim()
        if (t.isEmpty()) return ""
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val cls = Class.forName("android.icu.text.Transliterator")
                val getInstance = cls.getMethod("getInstance", String::class.java)
                val transliterator = getInstance.invoke(null, "Han-Latin/Names; Latin-ASCII")
                val transliterate = cls.getMethod("transliterate", String::class.java)
                val out = transliterate.invoke(transliterator, t) as String
                return java.text.Normalizer.normalize(out, java.text.Normalizer.Form.NFD)
                    .replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
            } catch (_: Exception) { /* fall through */ }
        }
        return t
    }
    fun firstLetterKey(title: String): Pair<Int, Char> {
        val t = toHalfWidth(title).trim()
        if (t.isEmpty()) return 2 to '#'
        val c = t.first()
        if (c.isDigit()) return 0 to '0'
        val asciiLetter = c in 'A'..'Z' || c in 'a'..'z'
        if (asciiLetter) return 1 to c.uppercaseChar()
        val asciiFromHan = transliterateToAscii(t).firstOrNull { it.isLetter() }?.uppercaseChar() ?: '#'
        return 1 to asciiFromHan
    }
    return list.sortedWith(compareBy(
        { firstLetterKey(it.title).first },
        { firstLetterKey(it.title).second },
        { transliterateToAscii(it.title).lowercase() }
    ))
}
