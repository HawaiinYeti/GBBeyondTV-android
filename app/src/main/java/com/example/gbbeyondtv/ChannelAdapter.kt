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
    private var currentFocusedPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.channel_list_item, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.bind(channel)
        if (position == currentFocusedPosition) {
            holder.itemView.requestFocus()
        }
    }

    override fun getItemCount(): Int = channels.size

    fun updateChannels(keepFocus: Boolean = false) {
        if (!keepFocus) {
            currentFocusedPosition = null
        }

        channels.forEachIndexed { index, channel ->
            if (channel.hasQueueUpdate) {
                notifyItemChanged(index)
            }
        }
    }

    fun addChannels(newChannels: List<Channel>) {
        channels.clear()
        channels.addAll(newChannels)
        notifyDataSetChanged() // Notify adapter of data change
    }

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val channelNameTextView: TextView = itemView.findViewById(R.id.channel_name)

        init {
            itemView.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    currentFocusedPosition = getBindingAdapterPosition()
                    view.setBackgroundColor(view.context.getColor(R.color.selected_item_background))
                } else {
                    view.setBackgroundColor(view.context.getColor(R.color.default_item_background))
                }
            }

            itemView.setOnClickListener {
                onItemClick(channels[getBindingAdapterPosition()])
            }
        }

        fun bind(channel: Channel) {
            val channelText = "${channel.name} - ${channel.currentlyPlaying()?.name}"
            channelNameTextView.text = channelText
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true
        }
    }
}
