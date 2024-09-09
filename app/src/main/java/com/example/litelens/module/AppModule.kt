package com.example.litelens.module

import android.content.Context
import com.example.litelens.data.manager.bingVisualSearch.BingVisualSearchRepositoryImpl
import com.example.litelens.data.manager.languageIdentification.LanguageIdentificationManagerImpl
import com.example.litelens.data.manager.objectDetection.ObjectDetectionManagerImpl
import com.example.litelens.data.manager.textRecognition.TextRecognitionManagerImpl
import com.example.litelens.data.manager.textTranslation.TextTranslationManagerImpl
import com.example.litelens.domain.repository.bingVisualSearch.BingVisualSearchRepository
import com.example.litelens.domain.usecases.objectDetection.DetectObjectManager
import com.example.litelens.domain.repository.languageIdentification.LanguageIdentificationManager
import com.example.litelens.domain.repository.objectDetection.ObjectDetectionManager
import com.example.litelens.domain.repository.textRecognition.TextRecognitionManager
import com.example.litelens.domain.repository.textTranslation.TextTranslationManager
import com.example.litelens.utils.FirebaseStorageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module(includes = [ApplicationContextModule::class])
@InstallIn(SingletonComponent::class)
class AppModule {

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
    fun provideDetectObjectUseCase(
        objectDetectionManager: ObjectDetectionManager
    ): DetectObjectManager = DetectObjectManager(
        objectDetectionManager = objectDetectionManager
    )

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    fun provideBingVisualSearchRepository(
        client: OkHttpClient,
        @ApplicationContext context: Context
    ): BingVisualSearchRepository {
        return BingVisualSearchRepositoryImpl(client, context)
    }

    @Provides
    @Singleton
    fun provideFirebaseStorageManager(
        @ApplicationContext context: Context
    ): FirebaseStorageManager {
        return FirebaseStorageManager()
    }
}