package br.ufc.smd.geoplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


/**
 * Adapter para RecyclerViews de [Song].
 */
class SongListAdapter(
    var list: List<Song>,
    var context: Context
) : RecyclerView.Adapter<ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.song_item_layout, parent, false)
        return ItemViewHolder(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.title.text = list[position].title
        holder.album.text = list[position].album
        holder.artist.text = list[position].artist

        //Marquee
        holder.title.isSelected = true
        holder.album.isSelected = true
        holder.artist.isSelected = true
    }

}

/**
 * ViewHolder de um elemento de lista.
 *
 * @see SongListAdapter
 */
class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
    var title: TextView = itemView.findViewById(R.id.songTitle)
    var album: TextView = itemView.findViewById(R.id.songAlbum)
    var artist: TextView = itemView.findViewById(R.id.songArtist)
}