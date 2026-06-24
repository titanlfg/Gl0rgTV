# Gl0rgTV Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build Gl0rgTV, a sideloaded Android TV app for Kick streams with native browsing, local favorites/history, WebView login, and native-player-first playback.

**Architecture:** Create a Kotlin Android TV app from an empty workspace. Keep Kick website/session scraping inside a small `kick` boundary, keep playback inside `player`, and keep local personalization inside `library`. UI consumes typed repositories only.

**Tech Stack:** Kotlin, Gradle Android Plugin, Compose for TV, AndroidX Media3, OkHttp, kotlinx.serialization, Jsoup, Room, DataStore, JUnit, AndroidX Test.

---

## File Structure

- Create `settings.gradle.kts`: Gradle plugin repositories and `:app` include.
- Create `build.gradle.kts`: root plugin versions.
- Create `gradle.properties`: AndroidX and Kotlin settings.
- Create `app/build.gradle.kts`: Android app config and dependencies.
- Create `app/src/main/AndroidManifest.xml`: TV launcher, internet permission, app theme.
- Create `app/src/main/res/values/colors.xml`: Kick green and app palette.
- Create `app/src/main/res/values/strings.xml`: app name and disclaimer strings.
- Create `app/src/main/res/values/themes.xml`: app theme.
- Create `app/src/main/res/drawable/ic_launcher_foreground.xml`: Kick-green Gl0rgTV icon foreground.
- Create `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`: adaptive app icon.
- Create `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`: adaptive round app icon.
- Create `app/src/main/java/tv/gl0rg/kick/MainActivity.kt`: app entry point.
- Create `app/src/main/java/tv/gl0rg/kick/Gl0rgTvApp.kt`: navigation shell.
- Create `app/src/main/java/tv/gl0rg/kick/kick/KickModels.kt`: typed Kick models.
- Create `app/src/main/java/tv/gl0rg/kick/kick/KickResult.kt`: typed success/error wrapper.
- Create `app/src/main/java/tv/gl0rg/kick/kick/KickHtmlParsers.kt`: HTML parsing.
- Create `app/src/main/java/tv/gl0rg/kick/kick/KickJsonParsers.kt`: browser-visible Kick JSON parsing.
- Create `app/src/main/java/tv/gl0rg/kick/kick/KickClient.kt`: network client contract and implementation.
- Create `app/src/main/java/tv/gl0rg/kick/kick/KickSessionProvider.kt`: WebView cookie session boundary.
- Create `app/src/main/java/tv/gl0rg/kick/library/LibraryDatabase.kt`: Room database.
- Create `app/src/main/java/tv/gl0rg/kick/library/LibraryDao.kt`: favorites and recents DAO.
- Create `app/src/main/java/tv/gl0rg/kick/library/LibraryEntities.kt`: Room entities.
- Create `app/src/main/java/tv/gl0rg/kick/library/LibraryRepository.kt`: local favorites/history API.
- Create `app/src/main/java/tv/gl0rg/kick/settings/SettingsRepository.kt`: DataStore-backed settings.
- Create `app/src/main/java/tv/gl0rg/kick/player/StreamResolver.kt`: direct-HLS resolution and fallback decision.
- Create `app/src/main/java/tv/gl0rg/kick/player/PlayerScreen.kt`: native player and WebView fallback UI.
- Create `app/src/main/java/tv/gl0rg/kick/ui/HomeScreen.kt`: home rows.
- Create `app/src/main/java/tv/gl0rg/kick/ui/SearchScreen.kt`: search UI.
- Create `app/src/main/java/tv/gl0rg/kick/ui/ChannelScreen.kt`: channel detail UI.
- Create `app/src/main/java/tv/gl0rg/kick/ui/LoginScreen.kt`: embedded Kick login.
- Create `app/src/main/java/tv/gl0rg/kick/ui/SettingsScreen.kt`: settings UI.
- Create `app/src/test/java/tv/gl0rg/kick/kick/KickHtmlParsersTest.kt`: parser tests.
- Create `app/src/test/java/tv/gl0rg/kick/kick/KickJsonParsersTest.kt`: JSON parser tests.
- Create `app/src/test/java/tv/gl0rg/kick/player/StreamResolverTest.kt`: stream resolver tests.
- Create `app/src/test/java/tv/gl0rg/kick/library/LibraryRepositoryTest.kt`: local library tests.
- Create `app/src/test/java/tv/gl0rg/kick/settings/SettingsRepositoryTest.kt`: settings tests.
- Create `docs/sideload.md`: sideload release instructions.

## Task 1: Gradle Android TV Scaffold

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/java/tv/gl0rg/kick/MainActivity.kt`

- [ ] **Step 1: Create failing scaffold check**

Create `app/src/test/java/tv/gl0rg/kick/ScaffoldTest.kt`:

```kotlin
package tv.gl0rg.kick

import org.junit.Assert.assertEquals
import org.junit.Test

class ScaffoldTest {
    @Test
    fun appNameIsGl0rgTV() {
        assertEquals("Gl0rgTV", BuildConfig.APP_DISPLAY_NAME)
    }
}
```

- [ ] **Step 2: Run scaffold test to verify it fails**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.ScaffoldTest`

Expected: command fails because Gradle project files do not exist or `BuildConfig.APP_DISPLAY_NAME` is undefined.

- [ ] **Step 3: Create Gradle settings**

Create `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Gl0rgTV"
include(":app")
```

Create `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}
```

Create `gradle.properties`:

```properties
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8
```

- [ ] **Step 4: Create app Gradle config**

Create `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "tv.gl0rg.kick"
    compileSdk = 35

    defaultConfig {
        applicationId = "tv.gl0rg.kick"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "APP_DISPLAY_NAME", "\"Gl0rgTV\"")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui:1.7.6")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.6")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.tv:tv-foundation:1.0.0-alpha12")
    implementation("androidx.tv:tv-material:1.0.0")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jsoup:jsoup:1.18.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
```

- [ ] **Step 5: Generate Gradle wrapper**

Run: `gradle wrapper --gradle-version 8.10.2`

Expected: `BUILD SUCCESSFUL` and files `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, and `gradle/wrapper/gradle-wrapper.properties` exist.

- [ ] **Step 6: Create Android manifest and resources**

Create `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />

    <uses-feature
        android:name="android.software.leanback"
        android:required="true" />
    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />

    <application
        android:allowBackup="false"
        android:banner="@drawable/ic_launcher_foreground"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Gl0rgTV">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Create `app/src/main/res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">Gl0rgTV</string>
    <string name="unofficial_disclaimer">Unofficial Kick viewer. Not affiliated with Kick.</string>
</resources>
```

Create `app/src/main/res/values/colors.xml`:

```xml
<resources>
    <color name="kick_green">#53FC18</color>
    <color name="gl0rg_background">#090B0A</color>
    <color name="gl0rg_surface">#151A17</color>
    <color name="gl0rg_text">#F3F7F1</color>
</resources>
```

Create `app/src/main/res/values/themes.xml`:

```xml
<resources>
    <style name="Theme.Gl0rgTV" parent="android:style/Theme.Material.NoActionBar">
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowActionBar">false</item>
        <item name="android:windowBackground">@color/gl0rg_background</item>
    </style>
</resources>
```

- [ ] **Step 7: Create main activity**

Create `app/src/main/java/tv/gl0rg/kick/MainActivity.kt`:

```kotlin
package tv.gl0rg.kick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text("Gl0rgTV")
        }
    }
}
```

- [ ] **Step 8: Run scaffold test to verify it passes**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.ScaffoldTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit scaffold**

Run:

```powershell
git add settings.gradle.kts build.gradle.kts gradle.properties gradlew gradlew.bat gradle app
git commit -m "chore: scaffold Gl0rgTV Android TV app"
```

Expected: commit succeeds when workspace is a git repo. If workspace has no `.git`, record `commit skipped: not a git repository` in work notes.

## Task 2: Kick Domain Models and Result Type

**Files:**
- Create: `app/src/main/java/tv/gl0rg/kick/kick/KickModels.kt`
- Create: `app/src/main/java/tv/gl0rg/kick/kick/KickResult.kt`
- Create: `app/src/test/java/tv/gl0rg/kick/kick/KickModelsTest.kt`

- [ ] **Step 1: Write failing model test**

Create `app/src/test/java/tv/gl0rg/kick/kick/KickModelsTest.kt`:

```kotlin
package tv.gl0rg.kick.kick

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KickModelsTest {
    @Test
    fun channelDisplayNameFallsBackToSlug() {
        val channel = KickChannel(
            slug = "test-streamer",
            displayName = "",
            avatarUrl = null,
            bannerUrl = null,
            stream = null
        )

        assertEquals("test-streamer", channel.safeDisplayName)
    }

    @Test
    fun kickFailureKeepsReason() {
        val failure = KickResult.Failure("parse_failed")

        assertTrue(failure is KickResult.Failure)
        assertEquals("parse_failed", failure.reason)
    }
}
```

- [ ] **Step 2: Run model test to verify it fails**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.kick.KickModelsTest`

Expected: fails because `KickChannel` and `KickResult` are undefined.

- [ ] **Step 3: Create Kick models**

Create `app/src/main/java/tv/gl0rg/kick/kick/KickModels.kt`:

```kotlin
package tv.gl0rg.kick.kick

data class KickStream(
    val slug: String,
    val title: String,
    val category: String?,
    val thumbnailUrl: String?,
    val viewerCount: Int?,
    val isMature: Boolean,
    val hlsUrl: String?
)

data class KickChannel(
    val slug: String,
    val displayName: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val stream: KickStream?
) {
    val safeDisplayName: String
        get() = displayName.ifBlank { slug }
}

data class KickSearchResults(
    val liveChannels: List<KickChannel>,
    val channels: List<KickChannel>
)
```

Create `app/src/main/java/tv/gl0rg/kick/kick/KickResult.kt`:

```kotlin
package tv.gl0rg.kick.kick

sealed interface KickResult<out T> {
    data class Success<T>(val value: T) : KickResult<T>
    data class Failure(val reason: String, val cause: Throwable? = null) : KickResult<Nothing>
}
```

- [ ] **Step 4: Run model test to verify it passes**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.kick.KickModelsTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit models**

Run:

```powershell
git add app/src/main/java/tv/gl0rg/kick/kick app/src/test/java/tv/gl0rg/kick/kick/KickModelsTest.kt
git commit -m "feat: add Kick domain models"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 3: Kick HTML Parsers

**Files:**
- Create: `app/src/main/java/tv/gl0rg/kick/kick/KickHtmlParsers.kt`
- Create: `app/src/test/java/tv/gl0rg/kick/kick/KickHtmlParsersTest.kt`

- [ ] **Step 1: Write failing parser tests**

Create `app/src/test/java/tv/gl0rg/kick/kick/KickHtmlParsersTest.kt`:

```kotlin
package tv.gl0rg.kick.kick

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KickHtmlParsersTest {
    private val html = """
        <html>
          <head>
            <script id="__NEXT_DATA__" type="application/json">
              {
                "props": {
                  "pageProps": {
                    "channel": {
                      "slug": "gl0rg",
                      "user": {"username": "Gl0rg"},
                      "profile_picture": "https://img.example/avatar.png",
                      "banner_picture": "https://img.example/banner.png",
                      "stream": {
                        "is_live": true,
                        "stream_title": "Green stream",
                        "viewer_count": 321,
                        "is_mature": true,
                        "thumbnail": "https://img.example/thumb.png",
                        "category": {"name": "Just Chatting"}
                      }
                    }
                  }
                }
              }
            </script>
          </head>
        </html>
    """.trimIndent()

    @Test
    fun parsesChannelFromNextData() {
        val channel = KickHtmlParsers.parseChannel(html)

        requireNotNull(channel)
        assertEquals("gl0rg", channel.slug)
        assertEquals("Gl0rg", channel.safeDisplayName)
        assertEquals("Green stream", channel.stream?.title)
        assertEquals(321, channel.stream?.viewerCount)
        assertEquals(true, channel.stream?.isMature)
        assertEquals("Just Chatting", channel.stream?.category)
    }

    @Test
    fun invalidHtmlReturnsNull() {
        assertNull(KickHtmlParsers.parseChannel("<html></html>"))
    }

    @Test
    fun parsesChannelLinksFromPage() {
        val page = """
            <html>
              <body>
                <a href="/gl0rg">Gl0rg</a>
                <a href="/xqc">xQc</a>
                <a href="/terms-of-service">Terms</a>
              </body>
            </html>
        """.trimIndent()

        val channels = KickHtmlParsers.parseChannelLinks(page)

        assertEquals(listOf("gl0rg", "xqc"), channels.map { it.slug })
    }
}
```

- [ ] **Step 2: Run parser tests to verify they fail**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.kick.KickHtmlParsersTest`

Expected: fails because `KickHtmlParsers` is undefined.

- [ ] **Step 3: Create parser implementation**

Create `app/src/main/java/tv/gl0rg/kick/kick/KickHtmlParsers.kt`:

```kotlin
package tv.gl0rg.kick.kick

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup

object KickHtmlParsers {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseChannel(html: String): KickChannel? {
        val nextData = Jsoup.parse(html)
            .selectFirst("script#__NEXT_DATA__")
            ?.data()
            ?.trim()
            .orEmpty()

        if (nextData.isBlank()) return null

        val root = runCatching { json.parseToJsonElement(nextData).jsonObject }.getOrNull() ?: return null
        val channel = root.findObject("channel") ?: return null

        val slug = channel.string("slug") ?: return null
        val displayName = channel.objectAt("user")?.string("username")
            ?: channel.string("username")
            ?: slug

        val streamObj = channel.objectAt("stream")
        val stream = streamObj?.let {
            val isLive = it.boolean("is_live") ?: false
            if (!isLive) {
                null
            } else {
                KickStream(
                    slug = slug,
                    title = it.string("stream_title") ?: it.string("title") ?: "Untitled stream",
                    category = it.objectAt("category")?.string("name"),
                    thumbnailUrl = it.string("thumbnail"),
                    viewerCount = it.int("viewer_count"),
                    isMature = it.boolean("is_mature") ?: it.boolean("has_mature_content") ?: false,
                    hlsUrl = it.string("playback_url") ?: it.string("source")
                )
            }
        }

        return KickChannel(
            slug = slug,
            displayName = displayName,
            avatarUrl = channel.string("profile_picture"),
            bannerUrl = channel.string("banner_picture"),
            stream = stream
        )
    }

    fun parseChannelLinks(html: String): List<KickChannel> {
        val blocked = setOf(
            "about",
            "terms-of-service",
            "privacy-policy",
            "community-guidelines",
            "jobs",
            "support",
            "search",
            "following",
            "categories"
        )

        return Jsoup.parse(html)
            .select("a[href^=/]")
            .mapNotNull { element ->
                val slug = element.attr("href").trim('/').substringBefore("?").substringBefore("/")
                val label = element.text().trim()
                when {
                    slug.isBlank() -> null
                    slug in blocked -> null
                    slug.length > 25 -> null
                    !slug.matches(Regex("[A-Za-z0-9_][A-Za-z0-9_-]*")) -> null
                    else -> KickChannel(
                        slug = slug,
                        displayName = label.ifBlank { slug },
                        avatarUrl = null,
                        bannerUrl = null,
                        stream = null
                    )
                }
            }
            .distinctBy { it.slug }
    }

    private fun JsonObject.findObject(key: String): JsonObject? {
        this[key]?.jsonObjectOrNull()?.let { return it }
        for (value in values) {
            val found = value.jsonObjectOrNull()?.findObject(key)
            if (found != null) return found
        }
        return null
    }

    private fun JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject

    private fun JsonObject.objectAt(key: String): JsonObject? = this[key]?.jsonObjectOrNull()

    private fun JsonObject.string(key: String): String? =
        this[key]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }

    private fun JsonObject.int(key: String): Int? =
        this[key]?.jsonPrimitive?.intOrNull

    private fun JsonObject.boolean(key: String): Boolean? =
        this[key]?.jsonPrimitive?.booleanOrNull
}
```

- [ ] **Step 4: Run parser tests to verify they pass**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.kick.KickHtmlParsersTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit parsers**

Run:

```powershell
git add app/src/main/java/tv/gl0rg/kick/kick/KickHtmlParsers.kt app/src/test/java/tv/gl0rg/kick/kick/KickHtmlParsersTest.kt
git commit -m "feat: parse Kick channel pages"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 4: Kick Public JSON Parsers

**Files:**
- Create: `app/src/main/java/tv/gl0rg/kick/kick/KickJsonParsers.kt`
- Create: `app/src/test/java/tv/gl0rg/kick/kick/KickJsonParsersTest.kt`

- [ ] **Step 1: Write failing JSON parser tests**

Create `app/src/test/java/tv/gl0rg/kick/kick/KickJsonParsersTest.kt`:

```kotlin
package tv.gl0rg.kick.kick

import org.junit.Assert.assertEquals
import org.junit.Test

class KickJsonParsersTest {
    @Test
    fun parsesPublicChannelApiJson() {
        val json = """
            {
              "slug": "xqc",
              "playback_url": "https://video.example/live.m3u8",
              "banner_image": {"url": "https://img.example/banner.webp"},
              "livestream": {
                "session_title": "Ranked games",
                "viewer_count": 1234,
                "thumbnail": {"url": "https://img.example/thumb.webp"},
                "is_mature": false,
                "categories": [{"name": "Just Chatting"}]
              },
              "user": {
                "username": "xQc",
                "profile_pic": "https://img.example/avatar.webp"
              }
            }
        """.trimIndent()

        val channel = KickJsonParsers.parsePublicChannel(json)

        requireNotNull(channel)
        assertEquals("xqc", channel.slug)
        assertEquals("xQc", channel.safeDisplayName)
        assertEquals("Ranked games", channel.stream?.title)
        assertEquals("https://video.example/live.m3u8", channel.stream?.hlsUrl)
        assertEquals(1234, channel.stream?.viewerCount)
        assertEquals("Just Chatting", channel.stream?.category)
    }
}
```

- [ ] **Step 2: Run JSON parser tests to verify they fail**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.kick.KickJsonParsersTest`

Expected: fails because `KickJsonParsers` is undefined.

- [ ] **Step 3: Create JSON parser implementation**

Create `app/src/main/java/tv/gl0rg/kick/kick/KickJsonParsers.kt`:

```kotlin
package tv.gl0rg.kick.kick

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object KickJsonParsers {
    private val json = Json { ignoreUnknownKeys = true }

    fun parsePublicChannel(jsonText: String): KickChannel? {
        val root = runCatching { json.parseToJsonElement(jsonText).jsonObject }.getOrNull() ?: return null
        val slug = root.string("slug") ?: return null
        val user = root.objectAt("user")
        val livestream = root.objectAt("livestream")
        val playbackUrl = root.string("playback_url")

        val stream = livestream?.let {
            KickStream(
                slug = slug,
                title = it.string("session_title") ?: it.string("stream_title") ?: "Untitled stream",
                category = it.arrayObject("categories", 0)?.string("name"),
                thumbnailUrl = it.objectAt("thumbnail")?.string("url") ?: it.string("thumbnail"),
                viewerCount = it.int("viewer_count"),
                isMature = it.boolean("is_mature") ?: false,
                hlsUrl = playbackUrl
            )
        }

        return KickChannel(
            slug = slug,
            displayName = user?.string("username") ?: slug,
            avatarUrl = user?.string("profile_pic"),
            bannerUrl = root.objectAt("banner_image")?.string("url"),
            stream = stream
        )
    }

    private fun JsonObject.objectAt(key: String): JsonObject? =
        this[key] as? JsonObject

    private fun JsonObject.arrayObject(key: String, index: Int): JsonObject? =
        runCatching { this[key]?.jsonArray?.get(index)?.jsonObject }.getOrNull()

    private fun JsonObject.string(key: String): String? =
        this[key]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }

    private fun JsonObject.int(key: String): Int? =
        this[key]?.jsonPrimitive?.intOrNull

    private fun JsonObject.boolean(key: String): Boolean? =
        this[key]?.jsonPrimitive?.booleanOrNull
}
```

- [ ] **Step 4: Run JSON parser tests to verify they pass**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.kick.KickJsonParsersTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit JSON parser**

Run:

```powershell
git add app/src/main/java/tv/gl0rg/kick/kick/KickJsonParsers.kt app/src/test/java/tv/gl0rg/kick/kick/KickJsonParsersTest.kt
git commit -m "feat: parse Kick public channel JSON"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 5: Local Favorites and Recents

**Files:**
- Create: `app/src/main/java/tv/gl0rg/kick/library/LibraryEntities.kt`
- Create: `app/src/main/java/tv/gl0rg/kick/library/LibraryDao.kt`
- Create: `app/src/main/java/tv/gl0rg/kick/library/LibraryDatabase.kt`
- Create: `app/src/main/java/tv/gl0rg/kick/library/LibraryRepository.kt`
- Create: `app/src/test/java/tv/gl0rg/kick/library/LibraryRepositoryTest.kt`

- [ ] **Step 1: Write failing repository tests**

Create `app/src/test/java/tv/gl0rg/kick/library/LibraryRepositoryTest.kt`:

```kotlin
package tv.gl0rg.kick.library

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryRepositoryTest {
    @Test
    fun favoriteToggleAddsAndRemovesSlug() = runTest {
        val repo = InMemoryLibraryRepository()

        repo.setFavorite("gl0rg", "Gl0rg", "https://img.example/a.png", true)
        assertEquals(listOf("gl0rg"), repo.favoriteSlugs())

        repo.setFavorite("gl0rg", "Gl0rg", "https://img.example/a.png", false)
        assertTrue(repo.favoriteSlugs().isEmpty())
    }

    @Test
    fun recentChannelsKeepNewestFirst() = runTest {
        val repo = InMemoryLibraryRepository()

        repo.markWatched("first", "First", null, watchedAtMillis = 1L)
        repo.markWatched("second", "Second", null, watchedAtMillis = 2L)

        assertEquals(listOf("second", "first"), repo.recentSlugs())
    }
}
```

- [ ] **Step 2: Run library tests to verify they fail**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.library.LibraryRepositoryTest`

Expected: fails because `InMemoryLibraryRepository` is undefined.

- [ ] **Step 3: Create repository contract and in-memory implementation**

Create `app/src/main/java/tv/gl0rg/kick/library/LibraryRepository.kt`:

```kotlin
package tv.gl0rg.kick.library

interface LibraryRepository {
    suspend fun setFavorite(slug: String, displayName: String, avatarUrl: String?, favorite: Boolean)
    suspend fun favoriteSlugs(): List<String>
    suspend fun markWatched(slug: String, displayName: String, thumbnailUrl: String?, watchedAtMillis: Long)
    suspend fun recentSlugs(): List<String>
}

class InMemoryLibraryRepository : LibraryRepository {
    private val favorites = linkedMapOf<String, FavoriteEntity>()
    private val recents = linkedMapOf<String, RecentEntity>()

    override suspend fun setFavorite(slug: String, displayName: String, avatarUrl: String?, favorite: Boolean) {
        if (favorite) {
            favorites[slug] = FavoriteEntity(slug, displayName, avatarUrl)
        } else {
            favorites.remove(slug)
        }
    }

    override suspend fun favoriteSlugs(): List<String> = favorites.keys.toList()

    override suspend fun markWatched(slug: String, displayName: String, thumbnailUrl: String?, watchedAtMillis: Long) {
        recents[slug] = RecentEntity(slug, displayName, thumbnailUrl, watchedAtMillis)
    }

    override suspend fun recentSlugs(): List<String> =
        recents.values.sortedByDescending { it.watchedAtMillis }.map { it.slug }
}

```

Create `app/src/main/java/tv/gl0rg/kick/library/LibraryEntities.kt`:

```kotlin
package tv.gl0rg.kick.library

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val slug: String,
    val displayName: String,
    val avatarUrl: String?
)

@Entity(tableName = "recents")
data class RecentEntity(
    @PrimaryKey val slug: String,
    val displayName: String,
    val thumbnailUrl: String?,
    val watchedAtMillis: Long
)
```

- [ ] **Step 4: Run library tests to verify they pass**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.library.LibraryRepositoryTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Create Room DAO and database**

Create `app/src/main/java/tv/gl0rg/kick/library/LibraryDao.kt`:

```kotlin
package tv.gl0rg.kick.library

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LibraryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorite(entity: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE slug = :slug")
    suspend fun deleteFavoriteBySlug(slug: String)

    @Query("SELECT slug FROM favorites ORDER BY displayName COLLATE NOCASE ASC")
    suspend fun favoriteSlugs(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecent(entity: RecentEntity)

    @Query("SELECT slug FROM recents ORDER BY watchedAtMillis DESC LIMIT 50")
    suspend fun recentSlugs(): List<String>
}
```

Create `app/src/main/java/tv/gl0rg/kick/library/LibraryDatabase.kt`:

```kotlin
package tv.gl0rg.kick.library

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class, RecentEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
}
```

Append `RoomLibraryRepository` to `app/src/main/java/tv/gl0rg/kick/library/LibraryRepository.kt`:

```kotlin
class RoomLibraryRepository(
    private val dao: LibraryDao
) : LibraryRepository {
    override suspend fun setFavorite(slug: String, displayName: String, avatarUrl: String?, favorite: Boolean) {
        if (favorite) {
            dao.upsertFavorite(FavoriteEntity(slug, displayName, avatarUrl))
        } else {
            dao.deleteFavoriteBySlug(slug)
        }
    }

    override suspend fun favoriteSlugs(): List<String> = dao.favoriteSlugs()

    override suspend fun markWatched(slug: String, displayName: String, thumbnailUrl: String?, watchedAtMillis: Long) {
        dao.upsertRecent(RecentEntity(slug, displayName, thumbnailUrl, watchedAtMillis))
    }

    override suspend fun recentSlugs(): List<String> = dao.recentSlugs()
}
```

- [ ] **Step 6: Run all library tests**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.library.*`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit library**

Run:

```powershell
git add app/src/main/java/tv/gl0rg/kick/library app/src/test/java/tv/gl0rg/kick/library
git commit -m "feat: add local favorites and recents"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 6: Settings Repository

**Files:**
- Create: `app/src/main/java/tv/gl0rg/kick/settings/SettingsRepository.kt`
- Create: `app/src/test/java/tv/gl0rg/kick/settings/SettingsRepositoryTest.kt`

- [ ] **Step 1: Write failing settings tests**

Create `app/src/test/java/tv/gl0rg/kick/settings/SettingsRepositoryTest.kt`:

```kotlin
package tv.gl0rg.kick.settings

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsRepositoryTest {
    @Test
    fun defaultsPreferNativePlayerAutoQualityAndMatureGate() = runTest {
        val repo = InMemorySettingsRepository()

        val settings = repo.load()

        assertEquals(PlayerMode.NativeFirst, settings.playerMode)
        assertEquals(StreamQuality.Auto, settings.quality)
        assertEquals(true, settings.showMatureGate)
    }

    @Test
    fun savesPlayerMode() = runTest {
        val repo = InMemorySettingsRepository()

        repo.save(repo.load().copy(playerMode = PlayerMode.WebViewOnly))

        assertEquals(PlayerMode.WebViewOnly, repo.load().playerMode)
    }
}
```

- [ ] **Step 2: Run settings tests to verify they fail**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.settings.SettingsRepositoryTest`

Expected: fails because settings types are undefined.

- [ ] **Step 3: Create settings implementation**

Create `app/src/main/java/tv/gl0rg/kick/settings/SettingsRepository.kt`:

```kotlin
package tv.gl0rg.kick.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class PlayerMode {
    NativeFirst,
    WebViewOnly,
    Auto
}

enum class StreamQuality {
    Auto,
    Source,
    High,
    Medium,
    Low
}

data class AppSettings(
    val playerMode: PlayerMode = PlayerMode.NativeFirst,
    val quality: StreamQuality = StreamQuality.Auto,
    val autoplay: Boolean = true,
    val showMatureGate: Boolean = true,
    val startScreen: String = "home"
)

interface SettingsRepository {
    suspend fun load(): AppSettings
    suspend fun save(settings: AppSettings)
}

class InMemorySettingsRepository : SettingsRepository {
    private var settings = AppSettings()

    override suspend fun load(): AppSettings = settings

    override suspend fun save(settings: AppSettings) {
        this.settings = settings
    }
}
```

- [ ] **Step 4: Run settings tests to verify they pass**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.settings.SettingsRepositoryTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Add DataStore-backed settings repository**

Append `DataStoreSettingsRepository` to `app/src/main/java/tv/gl0rg/kick/settings/SettingsRepository.kt`:

```kotlin
class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    override suspend fun load(): AppSettings =
        dataStore.data.map { preferences ->
            AppSettings(
                playerMode = preferences[PLAYER_MODE]?.let(PlayerMode::valueOf) ?: PlayerMode.NativeFirst,
                quality = preferences[QUALITY]?.let(StreamQuality::valueOf) ?: StreamQuality.Auto,
                autoplay = preferences[AUTOPLAY] ?: true,
                showMatureGate = preferences[MATURE_GATE] ?: true,
                startScreen = preferences[START_SCREEN] ?: "home"
            )
        }.first()

    override suspend fun save(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[PLAYER_MODE] = settings.playerMode.name
            preferences[QUALITY] = settings.quality.name
            preferences[AUTOPLAY] = settings.autoplay
            preferences[MATURE_GATE] = settings.showMatureGate
            preferences[START_SCREEN] = settings.startScreen
        }
    }

    private companion object {
        val PLAYER_MODE = stringPreferencesKey("player_mode")
        val QUALITY = stringPreferencesKey("quality")
        val AUTOPLAY = booleanPreferencesKey("autoplay")
        val MATURE_GATE = booleanPreferencesKey("mature_gate")
        val START_SCREEN = stringPreferencesKey("start_screen")
    }
}
```

- [ ] **Step 6: Build with DataStore repository**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.settings.SettingsRepositoryTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit settings**

Run:

```powershell
git add app/src/main/java/tv/gl0rg/kick/settings app/src/test/java/tv/gl0rg/kick/settings
git commit -m "feat: add app settings model"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 7: Session Provider and Login Screen Boundary

**Files:**
- Create: `app/src/main/java/tv/gl0rg/kick/kick/KickSessionProvider.kt`
- Create: `app/src/main/java/tv/gl0rg/kick/ui/LoginScreen.kt`
- Create: `app/src/test/java/tv/gl0rg/kick/kick/KickSessionProviderTest.kt`

- [ ] **Step 1: Write failing session tests**

Create `app/src/test/java/tv/gl0rg/kick/kick/KickSessionProviderTest.kt`:

```kotlin
package tv.gl0rg.kick.kick

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KickSessionProviderTest {
    @Test
    fun sessionDetectsKickCookie() {
        val provider = FakeKickSessionProvider("session=abc; path=/")

        assertTrue(provider.hasSession())
        assertEquals("session=abc; path=/", provider.cookieHeader())
    }

    @Test
    fun clearRemovesSession() {
        val provider = FakeKickSessionProvider("session=abc")

        provider.clear()

        assertFalse(provider.hasSession())
        assertEquals("", provider.cookieHeader())
    }
}
```

- [ ] **Step 2: Run session tests to verify they fail**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.kick.KickSessionProviderTest`

Expected: fails because session provider types are undefined.

- [ ] **Step 3: Create session provider**

Create `app/src/main/java/tv/gl0rg/kick/kick/KickSessionProvider.kt`:

```kotlin
package tv.gl0rg.kick.kick

import android.webkit.CookieManager

interface KickSessionProvider {
    fun hasSession(): Boolean
    fun cookieHeader(): String
    fun clear()
}

class WebViewKickSessionProvider(
    private val cookieManager: CookieManager = CookieManager.getInstance()
) : KickSessionProvider {
    override fun hasSession(): Boolean = cookieHeader().isNotBlank()

    override fun cookieHeader(): String = cookieManager.getCookie("https://kick.com").orEmpty()

    override fun clear() {
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }
}

class FakeKickSessionProvider(initialCookieHeader: String = "") : KickSessionProvider {
    private var cookieHeader = initialCookieHeader

    override fun hasSession(): Boolean = cookieHeader.isNotBlank()

    override fun cookieHeader(): String = cookieHeader

    override fun clear() {
        cookieHeader = ""
    }
}
```

- [ ] **Step 4: Run session tests to verify they pass**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.kick.KickSessionProviderTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Create login screen**

Create `app/src/main/java/tv/gl0rg/kick/ui/LoginScreen.kt`:

```kotlin
package tv.gl0rg.kick.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun LoginScreen(
    onLoginObserved: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Text("Login uses Kick web session. This unofficial mode can stop working when Kick changes its site.")
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            if (url.contains("kick.com", ignoreCase = true)) {
                                onLoginObserved()
                            }
                        }
                    }
                    loadUrl("https://kick.com")
                }
            }
        )
    }
}
```

- [ ] **Step 6: Commit session and login boundary**

Run:

```powershell
git add app/src/main/java/tv/gl0rg/kick/kick/KickSessionProvider.kt app/src/main/java/tv/gl0rg/kick/ui/LoginScreen.kt app/src/test/java/tv/gl0rg/kick/kick/KickSessionProviderTest.kt
git commit -m "feat: add WebView session login boundary"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 8: Kick Client and Stream Resolver

**Files:**
- Create: `app/src/main/java/tv/gl0rg/kick/kick/KickClient.kt`
- Create: `app/src/main/java/tv/gl0rg/kick/player/StreamResolver.kt`
- Create: `app/src/test/java/tv/gl0rg/kick/player/StreamResolverTest.kt`

- [ ] **Step 1: Write failing resolver tests**

Create `app/src/test/java/tv/gl0rg/kick/player/StreamResolverTest.kt`:

```kotlin
package tv.gl0rg.kick.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tv.gl0rg.kick.kick.KickStream

class StreamResolverTest {
    @Test
    fun directHlsUsesNativePlayback() {
        val stream = KickStream(
            slug = "gl0rg",
            title = "Live",
            category = null,
            thumbnailUrl = null,
            viewerCount = 10,
            isMature = false,
            hlsUrl = "https://video.example/live.m3u8"
        )

        val result = StreamResolver.resolve(stream)

        assertEquals(PlaybackRoute.Native("https://video.example/live.m3u8"), result)
    }

    @Test
    fun missingHlsUsesWebViewFallback() {
        val stream = KickStream(
            slug = "gl0rg",
            title = "Live",
            category = null,
            thumbnailUrl = null,
            viewerCount = null,
            isMature = false,
            hlsUrl = null
        )

        val result = StreamResolver.resolve(stream)

        assertTrue(result is PlaybackRoute.WebViewFallback)
    }
}
```

- [ ] **Step 2: Run resolver tests to verify they fail**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.player.StreamResolverTest`

Expected: fails because `StreamResolver` and `PlaybackRoute` are undefined.

- [ ] **Step 3: Create stream resolver**

Create `app/src/main/java/tv/gl0rg/kick/player/StreamResolver.kt`:

```kotlin
package tv.gl0rg.kick.player

import tv.gl0rg.kick.kick.KickStream

sealed interface PlaybackRoute {
    data class Native(val hlsUrl: String) : PlaybackRoute
    data class WebViewFallback(val url: String) : PlaybackRoute
}

object StreamResolver {
    fun resolve(stream: KickStream): PlaybackRoute {
        val hlsUrl = stream.hlsUrl
        return if (hlsUrl != null && hlsUrl.endsWith(".m3u8", ignoreCase = true)) {
            PlaybackRoute.Native(hlsUrl)
        } else {
            PlaybackRoute.WebViewFallback("https://kick.com/${stream.slug}")
        }
    }
}
```

- [ ] **Step 4: Run resolver tests to verify they pass**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.player.StreamResolverTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Create Kick client**

Create `app/src/main/java/tv/gl0rg/kick/kick/KickClient.kt`:

```kotlin
package tv.gl0rg.kick.kick

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

interface KickClient {
    suspend fun getChannel(slug: String): KickResult<KickChannel>
    suspend fun searchChannels(query: String): KickResult<KickSearchResults>
    suspend fun getLiveStreams(): KickResult<List<KickStream>>
    suspend fun getFollowedChannels(): KickResult<List<KickChannel>>
}

class WebKickClient(
    private val httpClient: OkHttpClient,
    private val sessionProvider: KickSessionProvider
) : KickClient {
    override suspend fun getChannel(slug: String): KickResult<KickChannel> = withContext(Dispatchers.IO) {
        val apiRequest = Request.Builder()
            .url("https://kick.com/api/v2/channels/$slug")
            .header("User-Agent", USER_AGENT)
            .applySessionCookie()
            .build()

        val apiResult = runCatching {
            httpClient.newCall(apiRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                KickJsonParsers.parsePublicChannel(body)
                    ?.let { KickResult.Success(it) }
                    ?: KickResult.Failure("channel_json_parse_failed")
            }
        }.getOrElse { KickResult.Failure("channel_json_request_failed", it) }

        if (apiResult is KickResult.Success) return@withContext apiResult

        val pageRequest = Request.Builder()
            .url("https://kick.com/$slug")
            .header("User-Agent", USER_AGENT)
            .applySessionCookie()
            .build()

        runCatching {
            httpClient.newCall(pageRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                KickHtmlParsers.parseChannel(body)
                    ?.let { KickResult.Success(it) }
                    ?: KickResult.Failure("channel_html_parse_failed")
            }
        }.getOrElse { KickResult.Failure("channel_html_request_failed", it) }
    }

    override suspend fun searchChannels(query: String): KickResult<KickSearchResults> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        val request = Request.Builder()
            .url("https://kick.com/search?query=$encoded")
            .header("User-Agent", USER_AGENT)
            .applySessionCookie()
            .build()

        runCatching {
            httpClient.newCall(request).execute().use { response ->
                val channels = KickHtmlParsers.parseChannelLinks(response.body?.string().orEmpty())
                KickResult.Success(KickSearchResults(liveChannels = channels.filter { it.stream != null }, channels = channels))
            }
        }.getOrElse { KickResult.Failure("search_request_failed", it) }
    }

    override suspend fun getLiveStreams(): KickResult<List<KickStream>> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://kick.com")
            .header("User-Agent", USER_AGENT)
            .applySessionCookie()
            .build()

        runCatching {
            httpClient.newCall(request).execute().use { response ->
                val streams = KickHtmlParsers.parseChannelLinks(response.body?.string().orEmpty()).mapNotNull { it.stream }
                KickResult.Success(streams)
            }
        }.getOrElse { KickResult.Failure("live_streams_request_failed", it) }
    }

    override suspend fun getFollowedChannels(): KickResult<List<KickChannel>> = withContext(Dispatchers.IO) {
        if (!sessionProvider.hasSession()) return@withContext KickResult.Failure("not_logged_in")

        val request = Request.Builder()
            .url("https://kick.com/following")
            .header("User-Agent", USER_AGENT)
            .applySessionCookie()
            .build()

        runCatching {
            httpClient.newCall(request).execute().use { response ->
                val channels = KickHtmlParsers.parseChannelLinks(response.body?.string().orEmpty())
                if (channels.isEmpty()) KickResult.Failure("followed_channels_parse_failed") else KickResult.Success(channels)
            }
        }.getOrElse { KickResult.Failure("followed_channels_request_failed", it) }
    }

    private fun Request.Builder.applySessionCookie(): Request.Builder {
        val cookieHeader = sessionProvider.cookieHeader()
        if (cookieHeader.isNotBlank()) {
            header("Cookie", cookieHeader)
        }
        return this
    }

    private companion object {
        const val USER_AGENT = "Mozilla/5.0 (Android TV; Gl0rgTV) AppleWebKit/537.36"
    }
}
```

- [ ] **Step 6: Run Kick and player tests**

Run: `.\gradlew testDebugUnitTest --tests tv.gl0rg.kick.kick.* --tests tv.gl0rg.kick.player.*`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit client and resolver**

Run:

```powershell
git add app/src/main/java/tv/gl0rg/kick/kick/KickClient.kt app/src/main/java/tv/gl0rg/kick/player app/src/test/java/tv/gl0rg/kick/player
git commit -m "feat: add Kick web client and playback resolver"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 9: Player Screen

**Files:**
- Create: `app/src/main/java/tv/gl0rg/kick/player/PlayerScreen.kt`

- [ ] **Step 1: Create player route preview input**

Create `app/src/main/java/tv/gl0rg/kick/player/PlayerScreen.kt`:

```kotlin
package tv.gl0rg.kick.player

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun PlayerScreen(route: PlaybackRoute, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        when (route) {
            is PlaybackRoute.Native -> NativePlayerPlaceholder(route.hlsUrl)
            is PlaybackRoute.WebViewFallback -> WebViewPlayer(route.url)
        }
    }
}

@Composable
private fun NativePlayerPlaceholder(hlsUrl: String) {
    Text("Native player: $hlsUrl")
}

@Composable
private fun WebViewPlayer(url: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}
```

- [ ] **Step 2: Replace native placeholder with Media3**

Modify `NativePlayerPlaceholder` in `PlayerScreen.kt`:

```kotlin
@Composable
private fun NativePlayerPlaceholder(hlsUrl: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = androidx.compose.runtime.remember(hlsUrl) {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            setMediaItem(androidx.media3.common.MediaItem.fromUri(hlsUrl))
            prepare()
            playWhenReady = true
        }
    }

    androidx.compose.runtime.DisposableEffect(player) {
        onDispose { player.release() }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            androidx.media3.ui.PlayerView(it).apply {
                this.player = player
                useController = true
            }
        }
    )
}
```

- [ ] **Step 3: Build app**

Run: `.\gradlew assembleDebug`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit player screen**

Run:

```powershell
git add app/src/main/java/tv/gl0rg/kick/player/PlayerScreen.kt
git commit -m "feat: add native and WebView player screen"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 10: App Navigation and TV Screens

**Files:**
- Create: `app/src/main/java/tv/gl0rg/kick/Gl0rgTvApp.kt`
- Modify: `app/src/main/java/tv/gl0rg/kick/MainActivity.kt`
- Create: `app/src/main/java/tv/gl0rg/kick/ui/HomeScreen.kt`
- Create: `app/src/main/java/tv/gl0rg/kick/ui/SearchScreen.kt`
- Create: `app/src/main/java/tv/gl0rg/kick/ui/ChannelScreen.kt`
- Create: `app/src/main/java/tv/gl0rg/kick/ui/SettingsScreen.kt`

- [ ] **Step 1: Create app shell**

Create `app/src/main/java/tv/gl0rg/kick/Gl0rgTvApp.kt`:

```kotlin
package tv.gl0rg.kick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import tv.gl0rg.kick.ui.HomeScreen
import tv.gl0rg.kick.ui.LoginScreen
import tv.gl0rg.kick.ui.SearchScreen
import tv.gl0rg.kick.ui.SettingsScreen

sealed interface AppRoute {
    data object Home : AppRoute
    data object Search : AppRoute
    data object Login : AppRoute
    data object Settings : AppRoute
}

@Composable
fun Gl0rgTvApp() {
    val route = remember { mutableStateOf<AppRoute>(AppRoute.Home) }

    when (route.value) {
        AppRoute.Home -> HomeScreen(
            onSearch = { route.value = AppRoute.Search },
            onLogin = { route.value = AppRoute.Login },
            onSettings = { route.value = AppRoute.Settings }
        )
        AppRoute.Search -> SearchScreen(onBack = { route.value = AppRoute.Home })
        AppRoute.Login -> LoginScreen(onLoginObserved = { route.value = AppRoute.Home })
        AppRoute.Settings -> SettingsScreen(onBack = { route.value = AppRoute.Home })
    }
}
```

- [ ] **Step 2: Create home screen**

Create `app/src/main/java/tv/gl0rg/kick/ui/HomeScreen.kt`:

```kotlin
package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onSearch: () -> Unit,
    onLogin: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(32.dp)) {
        Text("Gl0rgTV")
        Spacer(Modifier.height(24.dp))
        Row {
            Button(onClick = onSearch) { Text("Search") }
            Button(onClick = onLogin) { Text("Login") }
            Button(onClick = onSettings) { Text("Settings") }
        }
        Spacer(Modifier.height(32.dp))
        Text("Followed Live")
        Text("Login to load followed channels. Local favorites work without login.")
        Spacer(Modifier.height(24.dp))
        Text("Favorites")
        Text("No favorites yet.")
        Spacer(Modifier.height(24.dp))
        Text("Trending")
        Text("Streams load after Kick integration is connected.")
    }
}
```

- [ ] **Step 3: Create search and settings screens**

Create `app/src/main/java/tv/gl0rg/kick/ui/SearchScreen.kt`:

```kotlin
package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val query = remember { mutableStateOf("") }
    Column(modifier = modifier.padding(32.dp)) {
        Button(onClick = onBack) { Text("Back") }
        OutlinedTextField(
            value = query.value,
            onValueChange = { query.value = it },
            label = { Text("Channel or category") }
        )
        Text("Results for ${query.value}")
    }
}
```

Create `app/src/main/java/tv/gl0rg/kick/ui/SettingsScreen.kt`:

```kotlin
package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(32.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Text("Player mode: Native first")
        Text("Quality: Auto")
        Text("Mature gate: On")
        Text("Unofficial Kick viewer. Not affiliated with Kick.")
    }
}
```

- [ ] **Step 4: Create channel screen placeholder**

Create `app/src/main/java/tv/gl0rg/kick/ui/ChannelScreen.kt`:

```kotlin
package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.gl0rg.kick.kick.KickChannel

@Composable
fun ChannelScreen(
    channel: KickChannel,
    onWatch: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(32.dp)) {
        Text(channel.safeDisplayName)
        Text(channel.stream?.title ?: "Offline")
        Button(onClick = onWatch) { Text("Watch") }
        Button(onClick = onFavorite) { Text("Favorite") }
    }
}
```

- [ ] **Step 5: Wire main activity to app shell**

Modify `app/src/main/java/tv/gl0rg/kick/MainActivity.kt`:

```kotlin
package tv.gl0rg.kick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Gl0rgTvApp()
        }
    }
}
```

- [ ] **Step 6: Build app**

Run: `.\gradlew assembleDebug`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit UI shell**

Run:

```powershell
git add app/src/main/java/tv/gl0rg/kick/Gl0rgTvApp.kt app/src/main/java/tv/gl0rg/kick/MainActivity.kt app/src/main/java/tv/gl0rg/kick/ui
git commit -m "feat: add TV navigation shell"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 11: Kick-Green App Icon

**Files:**
- Create: `app/src/main/res/drawable/ic_launcher_background.xml`
- Create: `app/src/main/res/drawable/ic_launcher_foreground.xml`
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

- [ ] **Step 1: Create icon background**

Create `app/src/main/res/drawable/ic_launcher_background.xml`:

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/kick_green" />
</shape>
```

- [ ] **Step 2: Create icon foreground**

Create `app/src/main/res/drawable/ic_launcher_foreground.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#090B0A"
        android:pathData="M18,18h72v72h-72z" />
    <path
        android:fillColor="#53FC18"
        android:pathData="M29,28h14v52h-14zM47,28h14v14h-14zM61,42h14v14h-14zM47,56h14v24h-14zM61,66h14v14h-14z" />
</vector>
```

- [ ] **Step 3: Create adaptive icon XML**

Create `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`:

```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

Create `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`:

```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

- [ ] **Step 4: Build app**

Run: `.\gradlew assembleDebug`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit icon**

Run:

```powershell
git add app/src/main/res/drawable app/src/main/res/mipmap-anydpi-v26
git commit -m "feat: add Kick-green app icon"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 12: Sideload Release Docs

**Files:**
- Create: `docs/sideload.md`

- [ ] **Step 1: Create sideload instructions**

Create `docs/sideload.md`:

````markdown
# Gl0rgTV Sideload Install

Gl0rgTV is an unofficial Kick viewer for Android TV. It is not affiliated with Kick.

## Install

1. Download the latest `app-release.apk`.
2. Copy it to the Android TV device or install through `adb`.
3. Enable installing apps from unknown sources for the file manager or `adb`.
4. Install the APK.
5. Open Gl0rgTV from the Android TV launcher.

## ADB Install

```powershell
adb install app-release.apk
```

## Login

Login opens Kick in an embedded WebView. Gl0rgTV does not store Kick passwords. Sign out from Settings clears the local WebView session.

## Known Limit

Kick website changes can break login, followed streamers, stream discovery, or playback fallback.
````

- [ ] **Step 2: Build release APK**

Run: `.\gradlew assembleRelease`

Expected: `BUILD SUCCESSFUL` and APK exists at `app/build/outputs/apk/release/app-release-unsigned.apk` unless signing config is added.

- [ ] **Step 3: Commit docs**

Run:

```powershell
git add docs/sideload.md
git commit -m "docs: add sideload install guide"
```

Expected: commit succeeds in git repo, or skip note is recorded outside git.

## Task 13: Final Verification

**Files:**
- Read: all app files
- Read: `docs/sideload.md`

- [ ] **Step 1: Run unit tests**

Run: `.\gradlew testDebugUnitTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run debug build**

Run: `.\gradlew assembleDebug`

Expected: `BUILD SUCCESSFUL` and APK exists at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 3: Run release build**

Run: `.\gradlew assembleRelease`

Expected: `BUILD SUCCESSFUL` and APK exists at `app/build/outputs/apk/release/app-release-unsigned.apk`.

- [ ] **Step 4: Manual Android TV smoke test**

Run:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p tv.gl0rg.kick 1
```

Expected: app launches to Home screen named Gl0rgTV. Remote D-pad can focus Search, Login, and Settings. Login opens Kick WebView. Settings shows unofficial disclaimer.

- [ ] **Step 5: Record verification**

Add a short verification note to work log or final response:

```text
Verification:
- testDebugUnitTest: passed
- assembleDebug: passed
- assembleRelease: passed
- Android TV smoke test: passed or skipped with reason
```

- [ ] **Step 6: Final commit**

Run:

```powershell
git status --short
git add .
git commit -m "feat: build Gl0rgTV sideload app"
```

Expected: commit succeeds only when uncommitted implementation changes remain and workspace is a git repo. If no `.git` exists, record `commit skipped: not a git repository`.
