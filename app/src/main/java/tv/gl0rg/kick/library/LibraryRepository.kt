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

    override suspend fun favoriteSlugs(): List<String> =
        favorites.values
            .sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER, FavoriteEntity::displayName)
                    .thenBy(String.CASE_INSENSITIVE_ORDER, FavoriteEntity::slug)
                    .thenBy { it.slug }
            )
            .map { it.slug }

    override suspend fun markWatched(slug: String, displayName: String, thumbnailUrl: String?, watchedAtMillis: Long) {
        recents[slug] = RecentEntity(slug, displayName, thumbnailUrl, watchedAtMillis)
    }

    override suspend fun recentSlugs(): List<String> =
        recents.values
            .sortedWith(
                compareByDescending<RecentEntity> { it.watchedAtMillis }
                    .thenBy(String.CASE_INSENSITIVE_ORDER, RecentEntity::slug)
                    .thenBy { it.slug }
            )
            .take(50)
            .map { it.slug }
}

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
