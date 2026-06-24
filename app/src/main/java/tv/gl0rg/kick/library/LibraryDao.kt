package tv.gl0rg.kick.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LibraryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE slug = :slug")
    suspend fun deleteFavoriteBySlug(slug: String)

    @Query("SELECT slug FROM favorites ORDER BY displayName COLLATE NOCASE ASC, slug COLLATE NOCASE ASC, slug ASC")
    suspend fun favoriteSlugs(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecent(entity: RecentEntity)

    @Query("SELECT slug FROM recents ORDER BY watchedAtMillis DESC, slug COLLATE NOCASE ASC, slug ASC LIMIT 50")
    suspend fun recentSlugs(): List<String>
}
