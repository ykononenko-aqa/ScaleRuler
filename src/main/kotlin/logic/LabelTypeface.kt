package com.ykononenko.logic

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface

/**
 * Общий подбор системного жирного шрифта для подписей.
 *
 * Перебирает типичные для целевых платформ семейства в порядке приоритета,
 * затем пробует обобщённое имя `sans-serif` и, в последнюю очередь, шрифт по
 * умолчанию. Нужен потому, что неявный шрифт (`Font(null, size)`) на некоторых
 * системах оказывается пустым и не рисует ни одного глифа.
 */
internal object LabelTypeface {

    /** Найденный жирный шрифт или `null`, если ничего не найдено. */
    val bold: Typeface? by lazy { resolveBold() }

    private fun resolveBold(): Typeface? {
        val candidates = arrayOf("Segoe UI", "Arial", "Helvetica", "DejaVu Sans", "Liberation Sans")
        for (family in candidates) {
            FontMgr.default.matchFamilyStyle(family, FontStyle.BOLD)?.let { return it }
        }
        FontMgr.default.legacyMakeTypeface("sans-serif", FontStyle.BOLD)?.let { return it }
        return FontMgr.default.matchFamilyStyle(null, FontStyle.BOLD)
    }
}
