package com.example.talks.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.example.talks.database.db.TalksDatabase
import com.example.talks.constants.LocalConstants.DATABASE_NAME
import com.example.talks.constants.LocalConstants.SHARED_PREFERENCES_NAME
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseStorage() = FirebaseStorage.getInstance()

    @Singleton
    @Provides
    fun provideRunningDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context, TalksDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideDatabaseDao(db: TalksDatabase) = db.talksDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

}