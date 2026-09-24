package com.linkpipe.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun auth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Android Firestore enables durable offline persistence by default.
    @Provides @Singleton fun firestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
