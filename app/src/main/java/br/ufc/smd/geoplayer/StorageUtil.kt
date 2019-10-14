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

    fun storeSong(arrayList: ArrayList<Song>) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)

        val editor = preferences!!.edit()
        val gson = Gson()
        val json = gson.toJson(arrayList)
        editor.putString("audioArrayList", json)
        editor.apply()
    }

    fun loadSong(): ArrayList<Song> {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = preferences!!.getString("audioArrayList", null)
        val type = object : TypeToken<ArrayList<Song>>() {

        }.type
        return gson.fromJson(json, type)
    }

    fun storeSongIndex(index: Int) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.putInt("songIndex", index)
        editor.apply()
    }

    fun loadSongIndex(): Int {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences!!.getInt("songIndex", -1)//return -1 if no data found
    }

    fun clearCachedSongPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.clear()
        editor.apply()
    }
}