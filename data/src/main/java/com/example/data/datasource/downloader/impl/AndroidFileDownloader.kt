package com.example.data.datasource.downloader.impl

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.data.datasource.downloader.FileDownloader

class AndroidFileDownloader(context: Context): FileDownloader {
    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    override suspend fun downloadFile(url: String) {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
            .setDescription("Downloading file...") // Set the description for the notification
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Show notification
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                url.substringAfterLast("/")
            ) // Save to the public Downloads folder
        downloadManager.enqueue(request)
    }

}