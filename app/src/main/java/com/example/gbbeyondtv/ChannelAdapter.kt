// ChannelAdapter.kt
package com.example.gbbeyondtv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gbbeyondtv.R

class ChannelAdapter(
    private val channels: List<Channel>,
    private val onItemClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.channel_list_item, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.bind(channel, onItemClick)
    }

    override fun getItemCount(): Int = channels.size

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val channelNameTextView: TextView = itemView.findViewById(R.id.channel_name)

        init {
            // Set up focus change listener
            itemView.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.setBackgroundColor(view.context.getColor(R.color.selected_item_background))
                } else {
                    view.setBackgroundColor(view.context.getColor(R.color.default_item_background))
                }
            }

            // Set up click listener
            itemView.setOnClickListener {
                onItemClick(channels[adapterPosition])
            }
        }

        fun bind(channel: Channel, onItemClick: (Channel) -> Unit) {
            channelNameTextView.text = "${channel.name} - ${channel.currentlyPlaying()?.name}"
            // Ensure item is focusable
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true
        }
    }
}
