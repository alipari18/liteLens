package com.example.litelens.module

import android.app.Application
import android.content.Context
import com.example.litelens.data.manager.datastore.LocalUserConfigManagerImpl
import com.example.litelens.data.manager.languageIdentification.LanguageIdentificationManagerImpl
import com.example.litelens.data.manager.objectDetection.ObjectDetectionManagerImpl
import com.example.litelens.data.manager.textRecognition.TextRecognitionManagerImpl
import com.example.litelens.data.manager.textTranslation.TextTranslationManagerImpl
import com.example.litelens.domain.usecases.datastore.ReadUserConfig
import com.example.litelens.domain.usecases.datastore.UserConfigData
import com.example.litelens.domain.usecases.datastore.WriteUserConfig
import com.example.litelens.domain.usecases.objectDetection.DetectObjectManager
import com.example.litelens.domain.repository.datastore.LocalUserConfigManager
import com.example.litelens.domain.repository.languageIdentification.LanguageIdentificationManager
import com.example.litelens.domain.repository.objectDetection.ObjectDetectionManager
import com.example.litelens.domain.repository.textRecognition.TextRecognitionManager
import com.example.litelens.domain.repository.textTranslation.TextTranslationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(includes = [ApplicationContextModule::class])
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideLocalUserConfigManager(
        application: Application
    ): LocalUserConfigManager = LocalUserConfigManagerImpl(application)

    @Provides
    @Singleton
    fun provideObjectDetectionManager(
        @ApplicationContext context: Context
    ): ObjectDetectionManager = ObjectDetectionManagerImpl(
        context
    )

    @Provides
    @Singleton
    fun provideTextRecognitionManager(
        @ApplicationContext context: Context
    ): TextRecognitionManager = TextRecognitionManagerImpl(
        context
    )

    @Provides
    @Singleton
    fun provideLanguageIdentificationManager(): LanguageIdentificationManager = LanguageIdentificationManagerImpl()

    @Provides
    @Singleton
    fun provideTextTranslationManager(): TextTranslationManager = TextTranslationManagerImpl()

    @Provides
    @Singleton
    fun provideUserConfigUseCases(
        localUserConfigManager: LocalUserConfigManager
    ): UserConfigData = UserConfigData(
        readUserConfig = ReadUserConfig(localUserConfigManager),
        writeUserConfig = WriteUserConfig(localUserConfigManager)
    )

    @Provides
    @Singleton
    fun provideDetectObjectUseCase(
        objectDetectionManager: ObjectDetectionManager
    ): DetectObjectManager = DetectObjectManager(
        objectDetectionManager = objectDetectionManager
    )
}