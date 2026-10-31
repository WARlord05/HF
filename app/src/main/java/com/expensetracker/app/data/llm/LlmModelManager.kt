package com.expensetracker.app.data.llm

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LlmModelState {
    data object Missing : LlmModelState
    data class Downloading(val downloaded: Long, val total: Long) : LlmModelState
    data object Ready : LlmModelState
    data class Failed(val message: String) : LlmModelState
}

@Singleton
class LlmModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val modelFile = File(context.filesDir, "models/qwen2.5-0.5b-instruct.task")
    private val partFile = File(modelFile.path + ".part")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var downloadJob: Job? = null

    private val _state = MutableStateFlow<LlmModelState>(
        if (isReady()) LlmModelState.Ready else LlmModelState.Missing
    )
    val state: StateFlow<LlmModelState> = _state.asStateFlow()

    fun isReady(): Boolean = modelFile.length() >= MIN_MODEL_BYTES

    fun modelPath(): String? = modelFile.takeIf { isReady() }?.absolutePath

    fun download() {
        if (downloadJob?.isActive == true || isReady()) return
        if (context.filesDir.usableSpace < MIN_MODEL_BYTES) {
            _state.value = LlmModelState.Failed("Not enough free storage")
            return
        }
        downloadJob = scope.launch { downloadInternal() }
    }

    fun deleteModel() {
        downloadJob?.cancel()
        downloadJob = null
        modelFile.delete()
        partFile.delete()
        _state.value = LlmModelState.Missing
    }

    private suspend fun downloadInternal() {
        modelFile.parentFile?.mkdirs()
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(MODEL_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "ExpenseTracker/1.0")
            }
            val code = connection.responseCode
            if (code !in 200..299) error("HTTP $code")
            val total = connection.contentLengthLong
            if (total > MAX_MODEL_BYTES) error("Model file is larger than expected")

            connection.inputStream.use { input ->
                FileOutputStream(partFile).use { output ->
                    val buffer = ByteArray(BUFFER_BYTES)
                    var downloaded = 0L
                    var lastUpdate = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (downloaded > MAX_MODEL_BYTES) error("Model file is larger than expected")
                        val now = System.currentTimeMillis()
                        if (now - lastUpdate >= PROGRESS_INTERVAL_MS) {
                            lastUpdate = now
                            _state.value = LlmModelState.Downloading(downloaded, total)
                        }
                    }
                }
            }

            if (partFile.length() < MIN_MODEL_BYTES || !hasTaskMagic(partFile)) {
                error("Downloaded model is incomplete")
            }
            modelFile.delete()
            if (!partFile.renameTo(modelFile)) error("Could not save the model")
            _state.value = LlmModelState.Ready
        } catch (e: CancellationException) {
            partFile.delete()
            throw e
        } catch (e: Exception) {
            partFile.delete()
            _state.value = LlmModelState.Failed(e.message ?: "Download failed")
        } finally {
            connection?.disconnect()
        }
    }

    private fun hasTaskMagic(file: File): Boolean = runCatching {
        file.inputStream().use { input ->
            val header = ByteArray(8)
            val read = input.read(header)
            read >= 4 && String(header, 0, read, Charsets.ISO_8859_1).contains("PK")
        }
    }.getOrDefault(false)

    companion object {
        private const val MODEL_URL =
            "https://huggingface.co/litert-community/Qwen2.5-0.5B-Instruct/resolve/main/" +
                "Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task?download=true"
        private const val MIN_MODEL_BYTES = 100L * 1024 * 1024
        private const val MAX_MODEL_BYTES = 700L * 1024 * 1024
        private const val CONNECT_TIMEOUT_MS = 30_000
        private const val READ_TIMEOUT_MS = 60_000
        private const val BUFFER_BYTES = 256 * 1024
        private const val PROGRESS_INTERVAL_MS = 250L

        const val MODEL_SIZE_LABEL = "547 MB"
    }
}
