package com.nexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.database.entity.AIConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AIDao {
    @androidx.room.Query("SELECT * FROM ai_conversations")
    suspend fun getAllEntities(): List<AIConversationEntity>

    @Query("SELECT * FROM ai_conversations ORDER BY lastMessageAt DESC")
    fun getAllConversations(): Flow<List<AIConversationEntity>>

    @Query("SELECT * FROM ai_conversations WHERE id = :id")
    suspend fun getConversationById(id: String): AIConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AIConversationEntity)

    @Query("DELETE FROM ai_conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("UPDATE ai_conversations SET messages = :messages, lastMessageAt = :now WHERE id = :id")
    suspend fun updateMessages(id: String, messages: String, now: Long = System.currentTimeMillis())
}
