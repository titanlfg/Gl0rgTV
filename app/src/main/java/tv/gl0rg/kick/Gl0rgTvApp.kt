package tv.gl0rg.kick

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import tv.gl0rg.kick.kick.KickChannel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import tv.gl0rg.kick.kick.KickResult
import tv.gl0rg.kick.kick.WebKickClient
import tv.gl0rg.kick.kick.WebViewKickSessionProvider
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

sealed interface AppRoute {
    data object Home : AppRoute
    data object Search : AppRoute
    data object Login : AppRoute
    data object Settings : AppRoute
    data class Channel(val channel: KickChannel) : AppRoute
    data class Player(val route: PlaybackRoute) : AppRoute
}

@Composable
fun Gl0rgTvApp() {
    val appContext = LocalContext.current.applicationContext
    val route = remember { mutableStateOf<AppRoute>(AppRoute.Home) }
    val statusMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val sessionProvider = remember { WebViewKickSessionProvider() }
    val kickClient = remember { WebKickClient(OkHttpClient(), sessionProvider) }
    val libraryRepository = remember(appContext) {
        val database = Room.databaseBuilder(appContext, LibraryDatabase::class.java, "gl0rgtv-library.db").build()
        RoomLibraryRepository(database.libraryDao())
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
                        statusMessage.value = null
                        route.value = AppRoute.Channel(result.value)
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
            onLogin = { route.value = AppRoute.Login },
            onSettings = { route.value = AppRoute.Settings }
        )
        AppRoute.Search -> SearchScreen(
            onBack = { route.value = AppRoute.Home },
            onOpenChannel = openChannel,
            statusMessage = statusMessage.value
        )
        AppRoute.Login -> LoginScreen(
            onLoginObserved = { route.value = AppRoute.Home },
            sessionProvider = sessionProvider
        )
        AppRoute.Settings -> SettingsScreen(
            onBack = { route.value = AppRoute.Home },
            onSignOut = {
                sessionProvider.clear {
                    statusMessage.value = "Signed out"
                    route.value = AppRoute.Home
                }
            }
        )
        is AppRoute.Channel -> ChannelScreen(
            channel = currentRoute.channel,
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
            onFavorite = {
                scope.launch {
                    libraryRepository.setFavorite(
                        slug = currentRoute.channel.slug,
                        displayName = currentRoute.channel.safeDisplayName,
                        avatarUrl = currentRoute.channel.avatarUrl,
                        favorite = true
                    )
                    statusMessage.value = "Favorited ${currentRoute.channel.safeDisplayName}"
                }
            }
        )
        is AppRoute.Player -> PlayerScreen(route = currentRoute.route)
    }
}

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
