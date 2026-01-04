package com.example.data.datasource.downloader

interface FileDownloader {
    suspend fun downloadFile(url: String)
}