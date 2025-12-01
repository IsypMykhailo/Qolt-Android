package ca.qolt.di

import android.content.Context
import androidx.room.Room
import ca.qolt.data.local.QoltDatabase
import ca.qolt.data.local.dao.BlockedAppDao
import ca.qolt.data.local.dao.PresetDao
import ca.qolt.data.local.dao.UsageSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQoltDatabase(
        @ApplicationContext context: Context
    ): QoltDatabase {
        return Room.databaseBuilder(
            context,
            QoltDatabase::class.java,
            QoltDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideBlockedAppDao(database: QoltDatabase): BlockedAppDao {
        return database.blockedAppDao()
    }

    @Provides
    @Singleton
    fun provideUsageSessionDao(database: QoltDatabase): UsageSessionDao {
        return database.usageSessionDao()
    }

    @Provides
    @Singleton
    fun providePresetDao(database: QoltDatabase): PresetDao {
        return database.presetDao()
    }
}
