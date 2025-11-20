package ca.qolt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isCurrentlyBlocked: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val totalBlockedTimeMs: Long = 0L
)
