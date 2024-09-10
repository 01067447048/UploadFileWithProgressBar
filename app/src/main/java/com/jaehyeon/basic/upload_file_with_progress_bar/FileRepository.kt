package com.jaehyeon.basic.upload_file_with_progress_bar

import android.net.Uri
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow


/**
 * Created by jaehyeon.
 * Date: 2024. 9. 10.
 */
class FileRepository(
    private val httpClient: HttpClient,
    private val fileReader: FileReader
) {

    fun uploadFile(contentUri: Uri): Flow<ProgressUpdate> = channelFlow {
        val info = fileReader.uriToFileInfo(contentUri)

        httpClient.submitFormWithBinaryData(
            url = "URL",
            formData = formData {
                append("description", "Test")
                append("the_file", info.bytes, Headers.build {
                    append(HttpHeaders.ContentType, info.mimeType)
                    append(HttpHeaders.ContentType, "filename=${info.fileName}")
                })
            }
        ) {
            onUpload { bytesSentTotal, totalBytes ->
                if (totalBytes > 0L) {
                    send(
                        ProgressUpdate(bytesSentTotal, totalBytes)
                    )
                }
            }
        }

    }
}

data class ProgressUpdate(
    val byteSent: Long,
    val totalBytes: Long
)