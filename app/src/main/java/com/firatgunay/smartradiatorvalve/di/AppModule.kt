package com.firatgunay.smartradiatorvalve.di

import android.content.Context
import com.firatgunay.smartradiatorvalve.data.local.AppDatabase
import com.firatgunay.smartradiatorvalve.data.local.dao.ScheduleDao
import com.firatgunay.smartradiatorvalve.data.local.dao.ValveDataDao
import com.firatgunay.smartradiatorvalve.data.repository.ScheduleRepository
import com.firatgunay.smartradiatorvalve.ml.TemperaturePredictor
import com.firatgunay.smartradiatorvalve.data.repository.ValveDataRepository
import com.firatgunay.smartradiatorvalve.data.repository.ValveRepository
import com.firatgunay.smartradiatorvalve.mqtt.MqttClient
import com.firatgunay.smartradiatorvalve.websocket.WebSocketClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideMqttClient(@ApplicationContext context: Context): MqttClient {
        return MqttClient(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideScheduleDao(database: AppDatabase): ScheduleDao {
        return database.scheduleDao()
    }

    @Provides
    @Singleton
    fun provideTemperaturePredictor(
        @ApplicationContext context: Context
    ): TemperaturePredictor {
        return TemperaturePredictor(context)
    }

    @Provides
    @Singleton
    fun provideValveDataDao(database: AppDatabase): ValveDataDao {
        return database.valveDataDao()
    }

    @Provides
    @Singleton
    fun provideValveDataRepository(
        valveDataDao: ValveDataDao,
        mqttClient: MqttClient
    ): ValveDataRepository {
        return ValveDataRepository(valveDataDao, mqttClient)
    }

    @Provides
    @Singleton
    fun provideWebSocketClient(@ApplicationContext context: Context): WebSocketClient {
        return WebSocketClient(context)
    }

    @Provides
    @Singleton
    fun provideValveRepository(
        mqttClient: MqttClient,
        scheduleDao: ScheduleDao,
        temperaturePredictor: TemperaturePredictor
    ): ValveRepository {
        return ValveRepository(mqttClient, scheduleDao, temperaturePredictor)
    }

    @Provides
    @Singleton
    fun provideScheduleRepository(
        scheduleDao: ScheduleDao
    ): ScheduleRepository {
        return ScheduleRepository(scheduleDao)
    }
} 