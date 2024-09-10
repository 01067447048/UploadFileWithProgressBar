package com.jaehyeon.basic.upload_file_with_progress_bar

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import okio.FileNotFoundException

/**
 * Created by jaehyeon.
 * Date: 2024. 9. 10.
 */
class UploadFileViewModel(
    private val repository: FileRepository
): ViewModel() {

    var state by mutableStateOf(UploadState())
        private set

    private var uploadJob: Job? = null

    fun uploadFile(contentUri: Uri) {
        uploadJob = repository
            .uploadFile(contentUri)
            .onStart {
                state = state.copy(
                    isUploading = true,
                    isUploadComplete = false,
                    errorMessage = null,
                    progress = 0f
                )
            }.onEach { progressUpdate ->
                state = state.copy(
                    progress = progressUpdate.byteSent / progressUpdate.totalBytes.toFloat()
                )
            }
            .onCompletion { cause ->
                if (cause == null) {
                    state = state.copy(
                        isUploading = false,
                        isUploadComplete = true
                    )
                } else if (cause is CancellationException) {
                    state = state.copy(
                        isUploading = false,
                        errorMessage = "The Upload was cancelled!",
                        isUploadComplete = false,
                        progress = 0f
                    )
                }
            }.catch { cause ->
                val message = when(cause) {
                    is OutOfMemoryError -> "File too large!"
                    is FileNotFoundException -> "File not found!"
                    is UnresolvedAddressException -> "No internet"
                    else -> "Something went wrong!"
                }

                state = state.copy(
                    isUploading = false,
                    errorMessage = message
                )
            }
            .launchIn(viewModelScope)
    }

    fun cancelUpload() {
        uploadJob?.cancel()
    }
}

data class UploadState(
    val isUploading: Boolean = false,
    val isUploadComplete: Boolean = false,
    val progress: Float = 0f,
    val errorMessage: String? = null
)