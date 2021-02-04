package com.sainivik.floatingbuttonoverotherapps.service

import android.annotation.SuppressLint
import android.app.*
import android.app.ActivityManager.RecentTaskInfo
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.sainivik.floatingbuttonoverotherapps.R
import com.sainivik.floatingbuttonoverotherapps.databinding.AppOverlayBinding


class OverlayServiceStartJob : Service() {

    private var mDisplay: Display? = null
    private var touchListener: OnTouchListener? = null
    lateinit var binding: AppOverlayBinding
    var windowManager: WindowManager? = null
    private fun calculateDisplayMetrics(): DisplayMetrics? {
        val mDisplayMetrics = DisplayMetrics()
        mDisplay!!.getMetrics(mDisplayMetrics)
        return mDisplayMetrics
    }

    var overlay: View? = null;
    override fun onCreate() {
        super.onCreate()
        windowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        binding = AppOverlayBinding.inflate(inflater);
        overlay = binding.root
        mDisplay = windowManager!!.defaultDisplay
        val w = resources.getDimension(R.dimen._50sdp).toInt()
        val h = resources.getDimension(R.dimen._50sdp).toInt()
        val params: WindowManager.LayoutParams =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    w,
                    h,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    w,
                    h,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }
        params.gravity = Gravity.START or Gravity.TOP
        setActionListener(params, overlay!!, windowManager!!);
        windowManager!!.addView(overlay, params)

    }

    private fun setActionListener(
        params: WindowManager.LayoutParams,
        overlay: View,
        windowManager: WindowManager
    ) {
        overlay.setOnTouchListener(OnTouchListener { view, arg1 -> false })
        binding.llNavigateBtn.setOnClickListener {

            moveAppTaskToFront()


        }
        setTouchListener(params, overlay, windowManager)
    }

    fun setTouchListener(
        params: WindowManager.LayoutParams,
        overlay: View,
        windowManager: WindowManager
    ) {


        touchListener = object : OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var downTime: Long = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downTime = SystemClock.elapsedRealtime()
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val currentTime = SystemClock.elapsedRealtime()
                        if (currentTime - downTime < 200) {
                            v.performClick()
                        } else {
                            updateViewLocation()
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(overlay, params)
                        return true
                    }
                }
                return false
            }

            private fun updateViewLocation() {
                val metrics = calculateDisplayMetrics()
                val width = metrics!!.widthPixels / 2
                if (params.x >= width) params.x =
                    width * 2 - 10 else if (params.x <= width) params.x = 10
                windowManager.updateViewLayout(overlay, params)
            }
        }
        binding.llNavigateBtn.setOnTouchListener(touchListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    var jobID = "";

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService();
        return START_NOT_STICKY
    }


    private fun startForegroundService() {
        // Create notification default intent.
        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder =
            NotificationCompat.Builder(this, "MY OVERLAY BUTTON")
        val bigTextStyle =
            NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle("App is running in background.")

        builder.setStyle(bigTextStyle)
        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.mipmap.ic_launcher_round)

        builder.priority = Notification.PRIORITY_MAX

        builder.setSound(null)
        builder.setVibrate(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                "MY OVERLAY BUTTON",
                "MY APP OVERLAY BUTTON", NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.setSound(null, null)
            notificationChannel.setShowBadge(false)
            notificationManager?.createNotificationChannel(notificationChannel)
        }

        val notification = builder.build()
        startForeground(123, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager!!.removeView(overlay);
    }

    @SuppressLint("MissingPermission")
    fun moveAppTaskToFront() {

        try {
            val activityManager =
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val recentTasks = activityManager.getRecentTasks(
                Int.MAX_VALUE,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE
            )

            var recentTaskInfo: RecentTaskInfo? = null

            for (i in recentTasks.indices) {
                if (recentTasks[i].baseIntent.component!!.packageName == packageName) {
                    recentTaskInfo = recentTasks[i]
                    break
                }
            }

            if (recentTaskInfo != null && recentTaskInfo.id > -1) {
                activityManager.moveTaskToFront(
                    recentTaskInfo.persistentId,
                    ActivityManager.MOVE_TASK_WITH_HOME
                )
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }


}