package com.firatgunay.smartradiatorvalve.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.firatgunay.smartradiatorvalve.data.repository.RoomRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance("https://smart-radiator-valve-default-rtdb.europe-west1.firebasedatabase.app").apply {
            setPersistenceEnabled(true)
        }
    }

    @Provides
    @Singleton
    fun provideRoomRepository(database: FirebaseDatabase): RoomRepository {
        return RoomRepository(database)
    }
} 