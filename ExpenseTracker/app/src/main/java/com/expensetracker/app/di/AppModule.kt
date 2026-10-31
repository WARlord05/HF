package com.expensetracker.app.di

import android.content.Context
import androidx.room.Room
import com.expensetracker.app.data.AppDatabase
import com.expensetracker.app.data.AutopayDao
import com.expensetracker.app.data.TransactionDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "expense-tracker.db").build()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideAutopayDao(database: AppDatabase): AutopayDao = database.autopayDao()
}
