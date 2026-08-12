package com.goldensystem.auris.di

import com.goldensystem.auris.data.ai.AiOrchestrator
import com.goldensystem.auris.data.ai.AiPlaylistGenerator  // 👈 ADICIONE
import com.goldensystem.auris.data.repository.MusicRepository  // 👈 ADICIONE
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AiEntryPoint {
    fun aiOrchestrator(): AiOrchestrator
    fun aiPlaylistGenerator(): AiPlaylistGenerator
    fun musicRepository(): MusicRepository
}