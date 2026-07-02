package com.pixelcam.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Repositories, use cases and the camera controller are constructor-injected
 * (@Inject + @Singleton), so no explicit @Provides bindings are required.
 * The module is kept as the single place to add future bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
