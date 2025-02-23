package com.plezha.videoplayer.data.dao

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.plezha.videoplayer.data.model.Video
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT COUNT(*) FROM videos")
    fun getVideoCount(): Int

    @Query("SELECT * FROM videos WHERE id = :videoId")
    fun getVideoById(videoId: String): Video

    @Query("SELECT * FROM videos")
    fun getAll(): Flow<List<Video>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<Video>)
}

@Database(
    entities = [Video::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}