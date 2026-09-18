package com.example.hud

import android.content.Context
import com.example.model.HudConfig
import com.example.model.HudSizeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object HudStateManager {
    private val _hudConfig = MutableStateFlow(HudConfig())
    val hudConfig: StateFlow<HudConfig> = _hudConfig.asStateFlow()

    fun updateConfig(update: (HudConfig) -> HudConfig) {
        _hudConfig.update(update)
    }

    fun setEnabled(enabled: Boolean) {
        _hudConfig.update { it.copy(isEnabled = enabled) }
    }

    fun toggleVisibility(): Boolean {
        var isVis = true
        _hudConfig.update {
            isVis = !it.isVisible
            it.copy(isVisible = isVis)
        }
        return isVis
    }

    fun toggleMinimized() {
        _hudConfig.update { it.copy(isMinimized = !it.isMinimized) }
    }

    fun cycleSizeMode(): HudSizeMode {
        val nextMode = when (_hudConfig.value.sizeMode) {
            HudSizeMode.COMPACT -> HudSizeMode.NORMAL
            HudSizeMode.NORMAL -> HudSizeMode.EXPANDED
            HudSizeMode.EXPANDED -> HudSizeMode.COMPACT
        }
        _hudConfig.update { it.copy(sizeMode = nextMode, isMinimized = false) }
        return nextMode
    }

    fun toggleLock() {
        _hudConfig.update { it.copy(isLocked = !it.isLocked) }
    }

    fun cycleOpacity(): Float {
        val nextOpacity = when {
            _hudConfig.value.opacity < 0.65f -> 0.85f
            _hudConfig.value.opacity < 0.90f -> 0.98f
            else -> 0.55f
        }
        _hudConfig.update { it.copy(opacity = nextOpacity) }
        return nextOpacity
    }
}
