package com.ykononenko.controls

import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

/** Пользовательские настройки приложения. */
data class AppSettings(
    val suppressWarnings: Boolean = false
)

/** Хранилище пользовательских настроек между запусками. */
interface SettingsStore {

    /** Загружает настройки; при отсутствии или ошибке возвращает значения по умолчанию. */
    fun load(): AppSettings

    /** Сохраняет настройки. */
    fun save(settings: AppSettings)
}

/** Хранит настройки в properties-файле пользователя. */
class PropertiesSettingsStore(
    private val file: File = defaultSettingsFile()
) : SettingsStore {

    override fun load(): AppSettings {
        if (!file.isFile) return AppSettings()
        return try {
            val properties = Properties()
            file.reader(StandardCharsets.UTF_8).use { properties.load(it) }
            AppSettings(
                suppressWarnings = properties
                    .getProperty(KEY_SUPPRESS_WARNINGS)
                    ?.toBooleanStrictOrNull()
                    ?: false
            )
        } catch (_: IOException) {
            AppSettings()
        } catch (_: IllegalArgumentException) {
            AppSettings()
        } catch (_: SecurityException) {
            AppSettings()
        }
    }

    override fun save(settings: AppSettings) {
        try {
            file.parentFile?.mkdirs()
            val properties = Properties().apply {
                setProperty(KEY_SUPPRESS_WARNINGS, settings.suppressWarnings.toString())
            }
            val tempFile = File(file.parentFile, "${file.name}.tmp")
            tempFile.writer(StandardCharsets.UTF_8).use { writer ->
                properties.store(writer, null)
            }
            try {
                Files.move(
                    tempFile.toPath(),
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
            } catch (_: IOException) {
                Files.move(
                    tempFile.toPath(),
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        } catch (_: IOException) {
            return
        } catch (_: SecurityException) {
            return
        }
    }

    companion object {

        private const val KEY_SUPPRESS_WARNINGS = "suppressWarnings"

        private fun defaultSettingsFile(): File {
            val userHome = File(System.getProperty("user.home"))
            val osName = System.getProperty("os.name").orEmpty().lowercase()
            val baseDir = when {
                osName.contains("win") -> File(
                    System.getenv("APPDATA")
                        ?.takeIf { it.isNotBlank() }
                        ?: File(userHome, ".config").path
                )
                osName.contains("mac") -> File(userHome, "Library/Application Support")
                else -> File(
                    System.getenv("XDG_CONFIG_HOME")
                        ?.takeIf { it.isNotBlank() }
                        ?: File(userHome, ".config").path
                )
            }
            return File(baseDir, "ScaleRuler/settings.properties")
        }
    }
}
