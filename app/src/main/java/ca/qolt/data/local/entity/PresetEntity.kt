package ca.qolt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetEntity (
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val blockedApps: List<String>,
    val emoji: String = "\uD83D\uDCD6"
)