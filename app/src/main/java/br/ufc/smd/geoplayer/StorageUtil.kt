package br.ufc.smd.geoplayer

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * StorageUtil class.
 *
 * Classe responsável por manipular o armazenamento de dados do aplicativo.
 */
class StorageUtil(private val context: Context) {

    private val STORAGE = "br.ufc.smd.geoplayer.STORAGE"
    private var preferences: SharedPreferences? = null

    fun storeSongs(arrayList: ArrayList<Song>) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        val gson = Gson()
        val json = gson.toJson(arrayList)
        editor.putString("audioArrayList", json)
        editor.apply()
    }

    fun loadSongs(): ArrayList<Song> {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = preferences!!.getString("audioArrayList", null)
        val type = object : TypeToken<ArrayList<Song>>() {}.type
        return gson.fromJson(json, type) ?: arrayListOf()
    }

    fun storeSongIndex(index: Int) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.putInt("songIndex", index)
        editor.apply()
    }

    fun loadSongIndex(): Int {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences!!.getInt("songIndex", -1) //return -1 if no data is found
    }

    fun clearCachedSongPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.clear()
        editor.apply()
    }

    fun storeEnqueuedSongs(arrayList: ArrayList<Song>) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        val gson = Gson()
        val json = gson.toJson(arrayList)
        editor.putString("enqueuedAudioList", json)
        editor.apply()
    }

    fun loadEnqueuedSongs(): ArrayList<Song> {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = preferences!!.getString("enqueuedAudioList", null)
        val type = object : TypeToken<ArrayList<Song>>() {}.type
        return gson.fromJson(json, type) ?: arrayListOf()
    }

    fun storeZones(arrayList: ArrayList<Zone>) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        val gson = Gson()
        val json = gson.toJson(arrayList)
        editor.putString("zoneArrayList", json)
        editor.apply()
    }

    fun loadZones() : ArrayList<Zone> {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = preferences!!.getString("zoneArrayList", null)
        val type = object : TypeToken<ArrayList<Zone>>() {}.type
        return gson.fromJson(json, type) ?: arrayListOf()
    }

    fun loadZone(index: Int): Zone {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val zones = loadZones()
        return zones[index]
    }

    fun loadZone(fenceKey: String): Zone? {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val zones = loadZones()
        var zone : Zone? = null
        zones.forEach { z ->
            if (z.fenceId == fenceKey)
                zone = z
        }
        return zone
    }

    fun updateZone(index: Int, zone: Zone) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val zones = loadZones()
        zones[index] = zone
        storeZones(zones)
    }

    fun storeFences(fences: Set<String>) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        preferences!!.edit().putStringSet("fences", fences).apply()
    }

    fun loadFences() : MutableSet<String> {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences!!.getStringSet("fences", setOf())!!
    }
}