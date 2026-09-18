package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.hardware.HardwareMonitor
import com.example.hud.HudStateManager
import com.example.model.HudSizeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Hardware HUD", appName)
    }

    @Test
    fun `hud state manager cycle size mode`() {
        HudStateManager.updateConfig { it.copy(sizeMode = HudSizeMode.NORMAL) }
        val next = HudStateManager.cycleSizeMode()
        assertEquals(HudSizeMode.EXPANDED, next)
        val after = HudStateManager.cycleSizeMode()
        assertEquals(HudSizeMode.COMPACT, after)
    }

    @Test
    fun `hud state manager toggle visibility`() {
        HudStateManager.updateConfig { it.copy(isVisible = true) }
        val isVis = HudStateManager.toggleVisibility()
        assertEquals(false, isVis)
        val isVisAgain = HudStateManager.toggleVisibility()
        assertEquals(true, isVisAgain)
    }

    @Test
    fun `hardware monitor initializes properly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val monitor = HardwareMonitor.getInstance(context)
        assertNotNull(monitor)
        assertNotNull(monitor.cpuInfo.value)
        assertNotNull(monitor.displayInfo.value)
        assertNotNull(monitor.batteryInfo.value)
        assertTrue(monitor.cpuInfo.value.coreCount > 0)
    }
}
