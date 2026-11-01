package com.ykononenko.controls

import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertiesSettingsStoreTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun missingFileReturnsDefaults() {
        val store = PropertiesSettingsStore(File(tempDir, "settings.properties"))

        assertFalse(store.load().suppressWarnings)
    }

    @Test
    fun savesAndLoadsSettings() {
        val file = File(tempDir, "settings.properties")
        val store = PropertiesSettingsStore(file)

        store.save(AppSettings(suppressWarnings = true))

        assertTrue(store.load().suppressWarnings)
        assertTrue(file.isFile)
    }

    @Test
    fun savesDisabledSetting() {
        val file = File(tempDir, "settings.properties")
        val store = PropertiesSettingsStore(file)
        store.save(AppSettings(suppressWarnings = true))

        store.save(AppSettings(suppressWarnings = false))

        assertFalse(store.load().suppressWarnings)
    }

    @Test
    fun createsMissingParentDirectory() {
        val file = File(File(tempDir, "nested/dir"), "settings.properties")
        val store = PropertiesSettingsStore(file)

        store.save(AppSettings(suppressWarnings = true))

        assertTrue(file.isFile)
        assertTrue(store.load().suppressWarnings)
    }

    @Test
    fun corruptedFileReturnsDefaults() {
        val file = File(tempDir, "settings.properties")
        file.writeText("suppressWarnings=\\uZZZZ")
        val store = PropertiesSettingsStore(file)

        assertFalse(store.load().suppressWarnings)
    }
}
