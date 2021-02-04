package com.sainivik.floatingbuttonoverotherapps

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sainivik.floatingbuttonoverotherapps.databinding.ActivityMainBinding
import com.sainivik.floatingbuttonoverotherapps.service.OverlayServiceStartJob

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setClickListener()
    }

    private fun setClickListener() {

        binding.btnShow.setOnClickListener {
            requestOverlayPermission(this)


        }
    }

    fun appIndicator(
        show: Boolean
    ) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (Settings.canDrawOverlays(this)) {
                    var intent = Intent(this, OverlayServiceStartJob::class.java)
                    if (show) {
                        startService(intent)

                    } else {
                        stopService(intent)
                    }
                }
            }
        } catch (e: Exception) {
        }


    }

    override fun onStart() {
        super.onStart()
        appIndicator(false)
    }

    override fun onStop() {
        super.onStop()
        appIndicator(true)
    }

    fun checkOverlayPermission(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            return Settings.canDrawOverlays(activity)
        }
        return false
    }

    public fun requestOverlayPermission(context: Activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.packageName)
                )
                context.startActivityForResult(intent, 1234)

            }else{
                Toast.makeText(this, "Allready have permission.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}