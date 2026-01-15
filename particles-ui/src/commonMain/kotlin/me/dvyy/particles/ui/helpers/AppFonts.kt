package me.dvyy.particles.ui.helpers

import de.fabmax.kool.util.MsdfFont

object AppFonts {
    suspend fun tryLoad(path: String): MsdfFont =
        MsdfFont(path).getOrNull() ?: MsdfFont("assets/$path").getOrNull() ?: MsdfFont.Companion.DEFAULT_FONT

    private var loaded = false
    var MONOSPACED = MsdfFont.Companion.DEFAULT_FONT
        private set

    suspend fun loadAll() {
        if (loaded) return
        MONOSPACED = tryLoad("fonts/hack/font-hack-regular")
        loaded = true
    }
}