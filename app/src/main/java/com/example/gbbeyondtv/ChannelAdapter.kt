package com.example.gbbeyondtv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChannelAdapter(
    var channels: MutableList<Channel>,
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

    fun updateChannels(newChannels: List<Channel>) {
        notifyDataSetChanged() // Notify adapter of data change
    }

    fun addChannels(newChannels: List<Channel>) {
        channels.addAll(newChannels)
        notifyDataSetChanged() // Notify adapter of data change
    }

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val channelNameTextView: TextView = itemView.findViewById(R.id.channel_name)

        init {
            itemView.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.setBackgroundColor(view.context.getColor(R.color.selected_item_background))
                } else {
                    view.setBackgroundColor(view.context.getColor(R.color.default_item_background))
                }
            }

            itemView.setOnClickListener {
                onItemClick(channels[adapterPosition])
            }
        }

        fun bind(channel: Channel, onItemClick: (Channel) -> Unit) {
            channelNameTextView.text = "${channel.name} - ${channel.currentlyPlaying()?.name}"
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true
        }
    }
}
