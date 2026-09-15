package com.nicolascarmona.paytrack.di

import android.app.Application
import androidx.room.Room
import com.nicolascarmona.paytrack.data.PayTrackDatabase
import com.nicolascarmona.paytrack.data.SubscriptionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// @Module y InstallIn(SingletonComponent::class) le dicen a Hilt que este es un manual de instrucciones
// global. Los objetos que se creen aquí vivirán durante toda la ejecución de la app

// Cada función @Provides es una "receta". Cuando Hilt vea que necesitas un SubscriptionDao en algún
// sitio, vendrá a este archivo, verá que primero necesita la PayTrackDatabase, la construirá
// pasándole la Application, sacará el DAO y te lo entregará

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Le enseñamos a Hilt cómo construir la base de datos
    @Provides
    @Singleton
    fun provideDatabase(app: Application): PayTrackDatabase {
        return Room.databaseBuilder(
            app,
            PayTrackDatabase::class.java,
            "paytrack_db"
        ).build()
    }

    // Le enseñamos a Hilt cómo sacar el DAO de esa base de datos
    @Provides
    @Singleton
    fun provideSubscriptionDao(db: PayTrackDatabase): SubscriptionDao {
        return db.dao
    }

}