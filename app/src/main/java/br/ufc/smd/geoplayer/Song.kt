package br.ufc.smd.geoplayer

import java.io.Serializable

/**
 * Song class.
 *
 * Representa um áudio dentro do app.
 */
data class Song (
    var data: String,
    var title: String,
    var album: String,
    var artist: String
) : Serializable