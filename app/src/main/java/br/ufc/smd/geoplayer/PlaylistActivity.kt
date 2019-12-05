package br.ufc.smd.geoplayer

class PlaylistActivity (var name: String) {
    var songs: ArrayList<Song> = arrayListOf()

    constructor() : this("Empty playlist")
    constructor(name: String, songs: ArrayList<Song>) : this(name) {
        this.songs = songs
    }
}