package com.example.chatapplication

import android.app.Application
import android.content.Context
import com.example.chatapplication.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zegocloud.zimkit.services.ZIMKit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import im.zego.zim.ZIM
import im.zego.zim.entity.ZIMAppConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore
    ): AuthRepository = AuthRepository(auth, db)

    @Provides
    @Singleton
    fun provideZIM(application: Application): ZIM{
        val appConfig = ZIMAppConfig().apply {
            appID = 123456
            appSign = "123456"
        }
        return ZIM.create(appConfig,application)
    }

    @Provides
    @Singleton
    fun provideZIMKit(): ZIMKit = ZIMKit()

    @Provides
    @Singleton
    fun provideAppContext(@ApplicationContext context: Context): Context = context



}