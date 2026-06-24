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
