package ca.qolt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ca.qolt.data.local.dao.BlockedAppDao
import ca.qolt.data.local.dao.UsageSessionDao
import ca.qolt.data.local.entity.BlockedAppEntity
import ca.qolt.data.local.entity.UsageSessionEntity
import ca.qolt.util.Converters

@Database(
    entities = [
        BlockedAppEntity::class,
        UsageSessionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class QoltDatabase : RoomDatabase() {

    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun usageSessionDao(): UsageSessionDao

    companion object {
        const val DATABASE_NAME = "qolt_database"
    }
}
