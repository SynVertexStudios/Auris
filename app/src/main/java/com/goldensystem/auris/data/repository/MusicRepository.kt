package com.goldensystem.auris.data.repository

import android.net.Uri
import androidx.paging.PagingData
import com.goldensystem.auris.data.model.Album
import com.goldensystem.auris.data.model.Artist
import com.goldensystem.auris.data.model.Lyrics
import com.goldensystem.auris.data.model.LyricsSourcePreference
import com.goldensystem.auris.data.model.Playlist
import com.goldensystem.auris.data.model.SearchFilterType
import com.goldensystem.auris.data.model.SearchHistoryItem
import com.goldensystem.auris.data.model.SearchResultItem
import com.goldensystem.auris.data.model.Song
import kotlinx.coroutines.flow.Flow
import com.goldensystem.auris.data.database.TelegramChannelEntity

interface MusicRepository {
    fun getAudioFiles(): Flow<List<Song>>
    fun getPaginatedSongs(sortOption: com.goldensystem.auris.data.model.SortOption, storageFilter: com.goldensystem.auris.data.model.StorageFilter): Flow<PagingData<Song>>
    fun getPaginatedAlbums(sortOption: com.goldensystem.auris.data.model.SortOption, storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL, minTracks: Int = 1): Flow<PagingData<Album>>
    fun getPaginatedArtists(sortOption: com.goldensystem.auris.data.model.SortOption, storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL): Flow<PagingData<Artist>>
    fun getPaginatedFavoriteSongs(sortOption: com.goldensystem.auris.data.model.SortOption, storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL): Flow<PagingData<Song>>
    suspend fun getFavoriteSongsOnce(storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL): List<Song>
    suspend fun getFavoriteSongsPage(limit: Int, offset: Int, sortOption: com.goldensystem.auris.data.model.SortOption = com.goldensystem.auris.data.model.SortOption.LikedSongTitleAZ, storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL): List<Song>
    fun getFavoriteSongCountFlow(storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL): Flow<Int>
    fun getSongCountFlow(): Flow<Int>
    suspend fun getRandomSongs(limit: Int): List<Song>
    suspend fun getSongsPage(limit: Int, offset: Int, sortOption: com.goldensystem.auris.data.model.SortOption = com.goldensystem.auris.data.model.SortOption.SongDefaultOrder, storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL): List<Song>
    suspend fun getAlbumsPage(limit: Int, offset: Int, sortOption: com.goldensystem.auris.data.model.SortOption = com.goldensystem.auris.data.model.SortOption.AlbumTitleAZ, storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL, minTracks: Int = 1): List<Album>
    suspend fun getArtistsPage(limit: Int, offset: Int, sortOption: com.goldensystem.auris.data.model.SortOption = com.goldensystem.auris.data.model.SortOption.ArtistNameAZ, storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL): List<Artist>
    suspend fun getFirstPlayableSong(): Song?
    fun getAlbums(storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL, minTracks: Int = 1): Flow<List<Album>>
    fun getArtists(storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL): Flow<List<Artist>>
    suspend fun getAllSongsOnce(): List<Song>
    fun getDistinctAlbumArtSongs(): Flow<List<Song>>
    fun getHomeMixPreviewSongs(limit: Int): Flow<List<Song>>
    suspend fun getAllAlbumsOnce(storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL, minTracks: Int = 1): List<Album>
    suspend fun getAllArtistsOnce(): List<Artist>
    fun getAlbumById(id: Long): Flow<Album?>
    fun getSongsForAlbum(albumId: Long): Flow<List<Song>>
    fun getSongsForArtist(artistId: Long): Flow<List<Song>>
    fun getSongsByIds(songIds: List<String>): Flow<List<Song>>
    suspend fun getSongByPath(path: String): Song?
    suspend fun getAllUniqueAudioDirectories(): Set<String>
    fun getAllUniqueAlbumArtUris(): Flow<List<Uri>>
    suspend fun invalidateCachesDependentOnAllowedDirectories()
    fun searchSongs(query: String): Flow<List<Song>>
    fun searchAlbums(query: String, minTracks: Int = 1): Flow<List<Album>>
    fun searchArtists(query: String): Flow<List<Artist>>
    suspend fun searchPlaylists(query: String): List<Playlist>
    fun searchAll(query: String, filterType: SearchFilterType): Flow<List<SearchResultItem>>
    suspend fun addSearchHistoryItem(query: String)
    suspend fun getRecentSearchHistory(limit: Int): List<SearchHistoryItem>
    suspend fun deleteSearchHistoryItemByQuery(query: String)
    suspend fun clearSearchHistory()
    fun getMusicByGenre(genreId: String): Flow<List<Song>>
    suspend fun toggleFavoriteStatus(songId: String): Boolean
    suspend fun setFavoriteStatus(songId: String, isFavorite: Boolean)
    suspend fun incrementPlayCount(songId: Long)
    suspend fun getFavoriteSongIdsOnce(): Set<String>
    fun getFavoriteSongIdsFlow(): Flow<Set<String>>
    fun getSong(songId: String): Flow<Song?>
    fun getArtistById(artistId: Long): Flow<Artist?>
    fun getArtistsForSong(songId: Long): Flow<List<Artist>>
    fun getGenres(): Flow<List<com.goldensystem.auris.data.model.Genre>>
    suspend fun getLyrics(song: Song, sourcePreference: LyricsSourcePreference = LyricsSourcePreference.EMBEDDED_FIRST, forceRefresh: Boolean = false): Lyrics?
    suspend fun getStoredLyrics(song: Song): Pair<Lyrics, String>?
    suspend fun getLyricsFromRemote(song: Song): Result<Pair<Lyrics, String>>
    suspend fun searchRemoteLyrics(song: Song): Result<Pair<String, List<LyricsSearchResult>>>
    suspend fun searchRemoteLyricsByQuery(title: String, artist: String? = null): Result<Pair<String, List<LyricsSearchResult>>>
    suspend fun updateLyrics(songId: Long, lyrics: String)
    suspend fun resetLyrics(songId: Long)
    suspend fun resetAllLyrics()
    fun getMusicFolders(storageFilter: com.goldensystem.auris.data.model.StorageFilter = com.goldensystem.auris.data.model.StorageFilter.ALL): Flow<List<com.goldensystem.auris.data.model.MusicFolder>>
    suspend fun deleteById(id: Long)
    suspend fun saveTelegramSongs(songs: List<Song>)
    suspend fun replaceTelegramSongsForChannel(chatId: Long, songs: List<Song>)
    suspend fun clearTelegramData()
    suspend fun saveTelegramChannel(channel: TelegramChannelEntity)
    fun getAllTelegramChannels(): Flow<List<TelegramChannelEntity>>
    suspend fun deleteTelegramChannel(chatId: Long)
    suspend fun saveTelegramTopics(chatId: Long, topics: List<com.goldensystem.auris.data.database.TelegramTopicEntity>)
    suspend fun replaceTopicsForChannel(chatId: Long, freshTopics: List<com.goldensystem.auris.data.database.TelegramTopicEntity>)
    suspend fun getTopicsForChannel(chatId: Long): List<com.goldensystem.auris.data.database.TelegramTopicEntity>
    fun getAllTelegramTopics(): Flow<List<com.goldensystem.auris.data.database.TelegramTopicEntity>>
    suspend fun replaceTelegramSongsForTopic(chatId: Long, threadId: Long, topicName: String, songs: List<Song>)
    suspend fun remapTelegramChatIds(remapping: Map<Long, Long>)
    val telegramRepository: com.goldensystem.auris.data.telegram.TelegramRepository
    suspend fun getSongIdsSorted(sortOption: com.goldensystem.auris.data.model.SortOption, storageFilter: com.goldensystem.auris.data.model.StorageFilter): List<Long>
    suspend fun getFavoriteSongIdsSorted(sortOption: com.goldensystem.auris.data.model.SortOption, storageFilter: com.goldensystem.auris.data.model.StorageFilter): List<Long>
    suspend fun getSongIdByContentUri(contentUri: String): Long?
    fun requestTelegramUnifiedSync()
}