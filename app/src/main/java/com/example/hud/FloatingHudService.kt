package com.example.hud

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.hardware.HardwareMonitor
import com.example.model.HudSizeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FloatingHudService : Service() {

    companion object {
        const val CHANNEL_ID = "hardware_hud_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_HUD = "com.example.hud.ACTION_START"
        const val ACTION_STOP_HUD = "com.example.hud.ACTION_STOP"
        const val ACTION_TOGGLE_VISIBILITY = "com.example.hud.ACTION_TOGGLE_VISIBILITY"
        const val ACTION_CYCLE_SIZE = "com.example.hud.ACTION_CYCLE_SIZE"

        fun start(context: Context) {
            val intent = Intent(context, FloatingHudService::class.java).apply {
                action = ACTION_START_HUD
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingHudService::class.java).apply {
                action = ACTION_STOP_HUD
            }
            context.startService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var overlayComposeView: ComposeView? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null
    private val overlayLifecycleOwner = OverlayLifecycleOwner()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var hardwareMonitor: HardwareMonitor
    private var isViewAttached = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        hardwareMonitor = HardwareMonitor.getInstance(this)
        overlayLifecycleOwner.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("HUD Starting...", isVisible = true))

        if (Settings.canDrawOverlays(this)) {
            initOverlayView()
        }

        observeMetricsForNotification()
        observeHudVisibility()
        HudStateManager.setEnabled(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_HUD -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_VISIBILITY -> {
                val isVis = HudStateManager.toggleVisibility()
                updateOverlayVisibility(isVis)
                updateNotification()
            }
            ACTION_CYCLE_SIZE -> {
                HudStateManager.cycleSizeMode()
                updateNotification()
            }
            ACTION_START_HUD -> {
                if (!isViewAttached && Settings.canDrawOverlays(this)) {
                    initOverlayView()
                }
            }
        }
        return START_STICKY
    }

    private fun initOverlayView() {
        if (isViewAttached) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val density = resources.displayMetrics.density
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (20 * density).toInt()
            y = (90 * density).toInt()
        }
        windowLayoutParams = params

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(overlayLifecycleOwner)
            setViewTreeViewModelStoreOwner(overlayLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(overlayLifecycleOwner)

            setContent {
                FloatingHudView(
                    hardwareMonitor = hardwareMonitor,
                    onDrag = { dx, dy ->
                        windowLayoutParams?.let { p ->
                            p.x = (p.x + dx.toInt()).coerceAtLeast(0)
                            p.y = (p.y + dy.toInt()).coerceAtLeast(0)
                            windowManager?.updateViewLayout(this@apply, p)
                        }
                    },
                    onResize = { dw, dh ->
                        windowLayoutParams?.let { p ->
                            val currentW = if (p.width > 0) p.width else (240 * density).toInt()
                            val currentH = if (p.height > 0) p.height else (130 * density).toInt()
                            p.width = (currentW + dw.toInt()).coerceIn((170 * density).toInt(), (380 * density).toInt())
                            p.height = (currentH + dh.toInt()).coerceIn((100 * density).toInt(), (320 * density).toInt())
                            windowManager?.updateViewLayout(this@apply, p)
                        }
                    },
                    onClose = {
                        stopSelf()
                    }
                )
            }
        }

        try {
            windowManager?.addView(composeView, params)
            overlayComposeView = composeView
            isViewAttached = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOverlayVisibility(visible: Boolean) {
        overlayComposeView?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun observeHudVisibility() {
        serviceScope.launch {
            HudStateManager.hudConfig.collectLatest { config ->
                updateOverlayVisibility(config.isVisible)
            }
        }
    }

    private fun observeMetricsForNotification() {
        serviceScope.launch {
            while (isActive) {
                delay(1200)
                updateNotification()
            }
        }
    }

    private fun updateNotification() {
        val metrics = hardwareMonitor.liveMetrics.value
        val config = HudStateManager.hudConfig.value
        val statusText = "FPS: ${metrics.fps.toInt()} • CPU: ${metrics.cpuUsagePercent}% • GPU: ${metrics.gpuUsagePercent}%"
        val notification = buildNotification(statusText, config.isVisible)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(contentText: String, isVisible: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleVisIntent = Intent(this, FloatingHudService::class.java).apply {
            action = ACTION_TOGGLE_VISIBILITY
        }
        val toggleVisPendingIntent = PendingIntent.getService(
            this, 1, toggleVisIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cycleSizeIntent = Intent(this, FloatingHudService::class.java).apply {
            action = ACTION_CYCLE_SIZE
        }
        val cycleSizePendingIntent = PendingIntent.getService(
            this, 2, cycleSizeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FloatingHudService::class.java).apply {
            action = ACTION_STOP_HUD
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 3, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val visLabel = if (isVisible) "Hide HUD" else "Show HUD"
        val sizeLabel = "Size: ${HudStateManager.hudConfig.value.sizeMode.label}"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Gaming HUD Active")
            .setContentText(contentText)
            .setSubText("Live Hardware Overlay")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_view, visLabel, toggleVisPendingIntent)
            .addAction(android.R.drawable.ic_menu_crop, sizeLabel, cycleSizePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop HUD", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Gaming Hardware HUD",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls and status updates for the floating hardware gaming HUD"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        overlayLifecycleOwner.onDestroy()

        if (isViewAttached && overlayComposeView != null) {
            try {
                windowManager?.removeView(overlayComposeView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayComposeView = null
            isViewAttached = false
        }

        HudStateManager.setEnabled(false)
    }
}
