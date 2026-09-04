package com.hermes.promptpad

import android.content.Context
import android.content.SharedPreferences

// ponytail: SharedPreferences, not DataStore/Room. Single process, tiny key set.
class Prefs(ctx: Context) {
    private val p: SharedPreferences = ctx.getSharedPreferences("promptpad", Context.MODE_PRIVATE)

    // ponytail: offline device has no weather feed; off by default, shows the last cached value if any.
    var showWeather: Boolean by BoolPref(p, "showWeather", false)
    var showBattery: Boolean by BoolPref(p, "showBattery", true)
    var peakRight: Boolean by BoolPref(p, "peakRight", false)
    var peakVariant: Int by IntPref(p, "peakVariant", 0)          // 0..2
    var leftHanded: Boolean by BoolPref(p, "leftHanded", false)
    var reduceMotion: Boolean by BoolPref(p, "reduceMotion", true)
    var fontProfile: Int by IntPref(p, "fontProfile", 0)          // 0 sans, 1 serif, 2 easy-read
    var textScale: Int by IntPref(p, "textScale", 100)
    var restriction: Int by IntPref(p, "restriction", 0)          // 0 none, 1 focus, 2 monk
    var dailyLimitMin: Int by IntPref(p, "dailyLimitMin", 30)
    var grayscale: Boolean by BoolPref(p, "grayscale", false)
    var sealed: Boolean by BoolPref(p, "sealed", false)

    var tiles: List<String>
        get() = p.getString("tiles", "")!!.split(",").filter { it.isNotBlank() }
        set(v) = p.edit().putString("tiles", v.joinToString(",")).apply()

    var distracting: Set<String>
        get() = p.getStringSet("distracting", DEFAULT_DISTRACTING)!!
        set(v) = p.edit().putStringSet("distracting", v).apply()

    /** key char (lowercase) -> package name, for physical-keyboard long-press launches. */
    var keyMap: Map<String, String>
        get() = p.getString("keymap", "")!!.split(";").filter { it.contains("=") }
            .associate { val (k, v) = it.split("=", limit = 2); k to v }
        set(v) = p.edit().putString("keymap", v.entries.joinToString(";") { "${it.key}=${it.value}" }).apply()

    fun setTile(index: Int, pkg: String) {
        val t = tiles.toMutableList()
        while (t.size <= index) t.add("")
        t[index] = pkg
        tiles = t
    }

    companion object {
        val DEFAULT_DISTRACTING = setOf(
            "com.google.android.youtube", "com.instagram.android", "com.zhiliaoapp.musically",
            "com.facebook.katana", "com.twitter.android", "com.reddit.frontpage",
            "com.android.chrome", "org.mozilla.firefox"
        )
        val BROWSERS_AND_SOCIAL = DEFAULT_DISTRACTING
    }
}

private class BoolPref(val p: SharedPreferences, val k: String, val d: Boolean) {
    operator fun getValue(t: Any?, pr: Any?) = p.getBoolean(k, d)
    operator fun setValue(t: Any?, pr: Any?, v: Boolean) = p.edit().putBoolean(k, v).apply()
}

private class IntPref(val p: SharedPreferences, val k: String, val d: Int) {
    operator fun getValue(t: Any?, pr: Any?) = p.getInt(k, d)
    operator fun setValue(t: Any?, pr: Any?, v: Int) = p.edit().putInt(k, v).apply()
}
