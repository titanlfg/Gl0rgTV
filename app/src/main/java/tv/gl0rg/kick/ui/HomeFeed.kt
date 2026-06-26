package tv.gl0rg.kick.ui

import tv.gl0rg.kick.kick.KickChannel

/** Pure, unit-testable rules for what the Home screen shows. */
object HomeFeed {
    /**
     * Channels for the top "Followed — Live" row: only favorited channels that are
     * currently live. Never random/top streamers (those live in their own row).
     */
    fun liveFollowed(favorites: List<KickChannel>): List<KickChannel> =
        favorites.filter { it.stream != null }
}
