package wykwey.yiclass.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import wykwey.yiclass.data.repository.AiConfigRepositoryImpl
import wykwey.yiclass.data.repository.RepositoryConfigRepositoryImpl
import wykwey.yiclass.data.repository.SettingsRepositoryImpl
import wykwey.yiclass.data.repository.TimetableRepositoryImpl
import wykwey.yiclass.domain.repository.AiConfigRepository
import wykwey.yiclass.domain.repository.RepositoryConfigRepository
import wykwey.yiclass.domain.repository.SettingsRepository
import wykwey.yiclass.domain.repository.TimetableRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTimetableRepository(
        impl: TimetableRepositoryImpl
    ): TimetableRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindRepositoryConfigRepository(
        impl: RepositoryConfigRepositoryImpl
    ): RepositoryConfigRepository

    @Binds
    @Singleton
    abstract fun bindAiConfigRepository(
        impl: AiConfigRepositoryImpl
    ): AiConfigRepository
}
