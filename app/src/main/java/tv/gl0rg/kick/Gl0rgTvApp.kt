package tv.gl0rg.kick

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import tv.gl0rg.kick.kick.KickChannel
import tv.gl0rg.kick.kick.KickStream
import tv.gl0rg.kick.kick.KickVideo
import tv.gl0rg.kick.kick.LocalCookieLoginServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import tv.gl0rg.kick.kick.KickResult
import tv.gl0rg.kick.kick.WebKickClient
import tv.gl0rg.kick.kick.WebViewKickSessionProvider
import tv.gl0rg.kick.library.FavoriteEntity
import tv.gl0rg.kick.library.LibraryDatabase
import tv.gl0rg.kick.library.RoomLibraryRepository
import tv.gl0rg.kick.player.PlaybackRoute
import tv.gl0rg.kick.player.PlayerScreen
import tv.gl0rg.kick.player.StreamResolver
import tv.gl0rg.kick.ui.ChannelScreen
import tv.gl0rg.kick.ui.HomeScreen
import tv.gl0rg.kick.ui.LoginScreen
import tv.gl0rg.kick.ui.SearchScreen
import tv.gl0rg.kick.ui.SettingsScreen
import tv.gl0rg.kick.update.AppUpdate
import tv.gl0rg.kick.update.GitHubUpdateClient
import tv.gl0rg.kick.update.InstallLaunchResult
import tv.gl0rg.kick.update.UpdateCheckResult
import tv.gl0rg.kick.update.UpdateInstaller

sealed interface AppRoute {
    data object Home : AppRoute
    data object Search : AppRoute
    data object Login : AppRoute
    data object Settings : AppRoute
    data class Channel(val channel: KickChannel, val videos: List<KickVideo>) : AppRoute
    data class Player(val route: PlaybackRoute) : AppRoute
}

@Composable
fun Gl0rgTvApp() {
    val appContext = LocalContext.current.applicationContext
    val route = remember { mutableStateOf<AppRoute>(AppRoute.Home) }
    val statusMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val sessionProvider = remember { WebViewKickSessionProvider() }
    val httpClient = remember { OkHttpClient() }
    val kickClient = remember { WebKickClient(httpClient, sessionProvider) }
    val updateClient = remember { GitHubUpdateClient(httpClient) }
    val availableUpdate = remember { mutableStateOf<AppUpdate?>(null) }
    val localLoginServer = remember { LocalCookieLoginServer(sessionProvider) }
    val localLoginUrl = remember { mutableStateOf<String?>(null) }
    val isLoggedIn = remember { mutableStateOf(sessionProvider.hasSession()) }
    val favoriteChannels = remember { mutableStateOf<List<KickChannel>>(emptyList()) }
    val liveStreams = remember { mutableStateOf<List<KickStream>>(emptyList()) }
    val categoryStreams = remember { mutableStateOf<List<KickStream>>(emptyList()) }
    val selectedCategory = remember { mutableStateOf("Just Chatting") }
    val selectedCategorySlug = remember { mutableStateOf("just-chatting") }
    val searchResults = remember { mutableStateOf<List<KickChannel>>(emptyList()) }
    val searchHistory = remember { mutableStateOf<List<String>>(emptyList()) }
    val libraryRepository = remember(appContext) {
        val database = Room.databaseBuilder(appContext, LibraryDatabase::class.java, "gl0rgtv-library.db").build()
        RoomLibraryRepository(database.libraryDao())
    }

    suspend fun refreshFavorites() {
        favoriteChannels.value = libraryRepository.favorites().map { favorite ->
            when (val result = kickClient.getChannel(favorite.slug)) {
                is KickResult.Success -> result.value.copy(
                    displayName = result.value.safeDisplayName.ifBlank { favorite.displayName },
                    avatarUrl = result.value.avatarUrl ?: favorite.avatarUrl
                )
                is KickResult.Failure -> favorite.toKickChannel()
            }
        }
    }

    suspend fun refreshBrowse() {
        when (val result = kickClient.getLiveStreams()) {
            is KickResult.Success -> liveStreams.value = result.value
            is KickResult.Failure -> statusMessage.value = "Browse load failed (${result.reason})"
        }
    }

    suspend fun refreshCategory(name: String, slug: String, announce: Boolean) {
        selectedCategory.value = name
        selectedCategorySlug.value = slug
        if (announce) statusMessage.value = "Loading $name"
        when (val result = kickClient.getCategoryStreams(slug)) {
            is KickResult.Success -> {
                categoryStreams.value = result.value
                if (announce) statusMessage.value = "${result.value.size} live in $name"
            }
            is KickResult.Failure -> if (announce) statusMessage.value = "Category load failed (${result.reason})"
        }
    }

    fun loadCategory(name: String, slug: String) {
        scope.launch {
            refreshCategory(name, slug, announce = true)
        }
    }

    fun runSearch(input: String) {
        val query = input.trim()
        if (query.isBlank()) {
            statusMessage.value = "Enter search text"
            return
        }
        searchHistory.value = (listOf(query) + searchHistory.value.filterNot { it.equals(query, ignoreCase = true) }).take(8)
        statusMessage.value = "Searching $query"
        scope.launch {
            when (val result = kickClient.searchChannels(query)) {
                is KickResult.Success -> {
                    val exactFallback = if (result.value.channels.isEmpty()) {
                        when (val exact = kickClient.getChannel(query.toKickChannelSlug())) {
                            is KickResult.Success -> listOf(exact.value)
                            is KickResult.Failure -> emptyList()
                        }
                    } else {
                        emptyList()
                    }
                    searchResults.value = (result.value.channels + exactFallback).distinctBy { it.slug }
                    statusMessage.value = "${searchResults.value.size} results"
                }
                is KickResult.Failure -> statusMessage.value = "Search failed (${result.reason})"
            }
        }
    }

    fun startLocalLoginServer() {
        localLoginServer.start(
            scope = scope,
            onLogin = {
                scope.launch {
                    isLoggedIn.value = true
                    statusMessage.value = "Kick session linked"
                    localLoginServer.stop()
                    route.value = AppRoute.Home
                }
            },
            onError = { reason -> scope.launch { statusMessage.value = "Login server error ($reason)" } }
        )
        localLoginUrl.value = localLoginServer.url
        statusMessage.value = localLoginUrl.value?.let { "Open $it" } ?: "Login server unavailable"
    }

    LaunchedEffect(Unit) {
        refreshFavorites()
        refreshBrowse()
        refreshCategory("Just Chatting", "just-chatting", announce = true)
        while (true) {
            delay(120_000)
            refreshFavorites()
            refreshBrowse()
            refreshCategory(selectedCategory.value, selectedCategorySlug.value, announce = false)
        }
    }

    val openChannel: (String) -> Unit = { input ->
        val slug = input.toKickChannelSlug()
        if (slug.isBlank()) {
            statusMessage.value = "Enter channel slug"
        } else {
            statusMessage.value = "Loading $slug"
            scope.launch {
                when (val result = kickClient.getChannel(slug)) {
                    is KickResult.Success -> {
                        val videos = when (val videoResult = kickClient.getChannelVideos(slug)) {
                            is KickResult.Success -> videoResult.value
                            is KickResult.Failure -> emptyList()
                        }
                        statusMessage.value = null
                        route.value = AppRoute.Channel(result.value, videos)
                    }
                    is KickResult.Failure -> statusMessage.value = "Could not open $slug (${result.reason})"
                }
            }
        }
    }

    BackHandler(enabled = route.value != AppRoute.Home) {
        route.value = AppRoute.Home
    }

    when (val currentRoute = route.value) {
        AppRoute.Home -> HomeScreen(
            onSearch = { route.value = AppRoute.Search },
            onSettings = { route.value = AppRoute.Settings },
            onOpenChannel = openChannel,
            onOpenStream = { stream ->
                route.value = AppRoute.Player(StreamResolver.resolve(stream))
            },
            onBrowseCategory = { name, slug ->
                loadCategory(name, slug)
            },
            favorites = favoriteChannels.value,
            liveStreams = liveStreams.value,
            selectedCategory = selectedCategory.value,
            categoryStreams = categoryStreams.value,
            statusMessage = statusMessage.value
        )
        AppRoute.Search -> SearchScreen(
            onBack = { route.value = AppRoute.Home },
            onSearch = { runSearch(it) },
            onClearHistory = {
                searchHistory.value = emptyList()
            },
            onOpenChannel = openChannel,
            results = searchResults.value,
            history = searchHistory.value,
            statusMessage = statusMessage.value
        )
        AppRoute.Login -> LoginScreen(
            localLoginUrl = localLoginUrl.value,
            statusMessage = statusMessage.value,
            onBack = {
                localLoginServer.stop()
                route.value = AppRoute.Home
            },
            onRestartServer = {
                localLoginServer.stop()
                startLocalLoginServer()
            }
        )
        AppRoute.Settings -> SettingsScreen(
            onBack = { route.value = AppRoute.Home },
            onSignOut = {
                sessionProvider.clear {
                    isLoggedIn.value = false
                    statusMessage.value = "Signed out"
                    route.value = AppRoute.Home
                }
            },
            isLoggedIn = isLoggedIn.value,
            onRefreshLogin = {
                route.value = AppRoute.Login
                localLoginServer.stop()
                startLocalLoginServer()
            },
            updateStatus = statusMessage.value,
            updateAvailable = availableUpdate.value != null,
            onCheckUpdate = {
                statusMessage.value = "Checking for update"
                scope.launch {
                    when (val result = updateClient.check(BuildConfig.VERSION_NAME)) {
                        is UpdateCheckResult.Available -> {
                            availableUpdate.value = result.update
                            statusMessage.value = "Update ${result.update.latestVersion} available"
                        }
                        is UpdateCheckResult.Current -> {
                            availableUpdate.value = null
                            statusMessage.value = "Up to date (${result.latestVersion})"
                        }
                        is UpdateCheckResult.Failed -> statusMessage.value = "Update check failed (${result.reason})"
                    }
                }
            },
            onInstallUpdate = {
                val update = availableUpdate.value
                if (update == null) {
                    statusMessage.value = "Check for update first"
                } else {
                    statusMessage.value = "Downloading ${update.latestVersion}"
                    scope.launch {
                        runCatching { updateClient.download(appContext, update) }
                            .onSuccess { apkFile ->
                                statusMessage.value = when (val installResult = UpdateInstaller.launch(appContext, apkFile)) {
                                    InstallLaunchResult.InstallerOpened -> "Installer opened"
                                    InstallLaunchResult.PermissionSettingsOpened -> "Allow installs, then install again"
                                    is InstallLaunchResult.Failed -> "Installer failed (${installResult.reason})"
                                }
                            }
                            .onFailure { statusMessage.value = "Download failed (${it.message ?: "unknown"})" }
                    }
                }
            }
        )
        is AppRoute.Channel -> ChannelScreen(
            channel = currentRoute.channel,
            videos = currentRoute.videos,
            onBack = { route.value = AppRoute.Search },
            onWatch = {
                val stream = currentRoute.channel.stream
                if (stream == null) {
                    statusMessage.value = "Channel offline"
                } else {
                    scope.launch {
                        libraryRepository.markWatched(
                            slug = currentRoute.channel.slug,
                            displayName = currentRoute.channel.safeDisplayName,
                            thumbnailUrl = stream.thumbnailUrl,
                            watchedAtMillis = System.currentTimeMillis()
                        )
                    }
                    route.value = AppRoute.Player(StreamResolver.resolve(stream))
                }
            },
            onWatchVideo = { video ->
                val source = video.playbackUrl
                if (source == null) {
                    statusMessage.value = "Video source unavailable"
                } else {
                    route.value = AppRoute.Player(PlaybackRoute.Native(source, isLive = false))
                }
            },
            onFavorite = {
                scope.launch {
                    libraryRepository.setFavorite(
                        slug = currentRoute.channel.slug,
                        displayName = currentRoute.channel.safeDisplayName,
                        avatarUrl = currentRoute.channel.avatarUrl,
                        favorite = true
                    )
                    refreshFavorites()
                    statusMessage.value = "Favorited ${currentRoute.channel.safeDisplayName}"
                }
            },
            statusMessage = statusMessage.value
        )
        is AppRoute.Player -> PlayerScreen(route = currentRoute.route)
    }
}

private fun FavoriteEntity.toKickChannel(): KickChannel =
    KickChannel(
        slug = slug,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bannerUrl = null,
        stream = null
    )

private fun String.toKickChannelSlug(): String {
    val cleaned = trim().removePrefix("@")
    val withoutScheme = cleaned.removePrefix("https://").removePrefix("http://")
    val withoutHost = when {
        withoutScheme.startsWith("www.kick.com/") -> withoutScheme.removePrefix("www.kick.com/")
        withoutScheme.startsWith("kick.com/") -> withoutScheme.removePrefix("kick.com/")
        else -> withoutScheme
    }
    return withoutHost
        .substringBefore("?")
        .substringBefore("#")
        .trim('/')
        .substringBefore("/")
        .trim()
}
