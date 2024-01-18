package com.example.louetoutfacile.network


import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.feedarticlesjetpack.network.UserRepository
import com.example.louetoutfacile.network.AppDatabase
import com.example.louetoutfacile.network.EquipmentDao
import com.example.louetoutfacile.network.ReservationDao
import com.example.louetoutfacile.network.StatusDao
import com.example.louetoutfacile.network.UserDao
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideUserRepository(sharedPreferences: SharedPreferences): UserRepository {
        return UserRepository(sharedPreferences)
    }
}



@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "louetoutfacile_bdd")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }
    @Provides
    fun provideEquipmentDao(appDatabase: AppDatabase): EquipmentDao {
        return appDatabase.equipmentDao()
    }
    @Provides
    fun provideStatusDao(appDatabase: AppDatabase): StatusDao {
        return appDatabase.statusDao()
    }
    @Provides
    @Singleton
    fun provideReservationDao(appDatabase: AppDatabase): ReservationDao {
        return appDatabase.reservationDao()
    }
}