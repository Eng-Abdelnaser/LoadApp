package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private val NOTIFICATION_ID = 0
    private lateinit var url: URL
    private var downloadStatus = "Fail"

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        createNotificationChannel()
        custom_button.setOnClickListener {
            if (this::url.isInitialized) {
                try {
                    val motionLayout=findViewById<MotionLayout>(R.id.main_layout)
                    motionLayout.transitionToEnd()
                    custom_button.buttonState = ButtonState.Loading
                    download()
                } catch (e: Exception) {
                    custom_button.buttonState = ButtonState.Completed
                    Toast.makeText(this, "Unable to download file", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "select library to download file", Toast.LENGTH_SHORT).show()
            }
        }
        RG_download_choose.setOnCheckedChangeListener { _, redioButton ->
            url = when (redioButton) {
                R.id.RB_retrofit -> URL.RETROFIT
                R.id.RB_udacity -> URL.UDACITY
                R.id.RB_glide -> URL.GLIDE
                else -> URL.RETROFIT
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(
                DownloadManager.EXTRA_DOWNLOAD_ID,
                -1
            )
            if (downloadID == id) {
                downloadStatus = "Success"
                custom_button.buttonState = ButtonState.Completed
                createNotification()
            }
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(url.uri))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
        if(cursor.moveToFirst()){
            when (cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)){
                DownloadManager.STATUS_FAILED -> {
                    downloadStatus = "Fail"
                    custom_button.buttonState = ButtonState.Completed }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    downloadStatus = "Success"
                }
            }
        }
    }

    companion object {
        private enum class URL(val uri: String, val title: String, val text: String) {
            GLIDE(
                path,
                "Glide: Image Loading Library By BumpTech",
                "Glide repository is downloaded"
            ),
            UDACITY(
                path,
                "Udacity: Android Kotlin Nanodegree",
                "Udacity repository is downloaded"
            ),
            RETROFIT(
                path,
                "Retrofit: Type-safe HTTP client by Square, Inc",
                "Retrofit repository is downloaded"
            ),
        }

        private const val path =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

    private fun createNotification() {
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        val detailIntent = Intent(this, DetailActivity::class.java)
        detailIntent.putExtra("fileName", url.title)
        detailIntent.putExtra("status", downloadStatus)
        pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(detailIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        } as PendingIntent
        action = NotificationCompat.Action(
            R.drawable.ic_assistant_black_24dp,
            getString(R.string.notification_button),
            pendingIntent
        )

        val contentIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(applicationContext, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(url.title)
            .setContentText(url.text)
            .setContentIntent(contentPendingIntent)
            .addAction(action)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                MainActivity.CHANNEL_ID,
                "LoadAppChannel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationChannel.description = "Download was complete!"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


}