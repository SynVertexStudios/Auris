package com.goldensystem.auris.di

import com.goldensystem.auris.data.ai.AiOrchestrator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AiEntryPoint {
    fun aiOrchestrator(): AiOrchestrator
    fun aiPlaylistGenerator(): AiPlaylistGenerator  // 👈 NOVO
    fun musicRepository(): MusicRepository           // 👈 NOVO
}