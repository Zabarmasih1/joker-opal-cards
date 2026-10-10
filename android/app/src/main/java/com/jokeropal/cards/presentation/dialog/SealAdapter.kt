package com.jokeropal.cards.presentation.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jokeropal.cards.R
import com.jokeropal.cards.databinding.ItemSealBinding
import com.jokeropal.cards.domain.model.SealRecord

class SealAdapter : RecyclerView.Adapter<SealAdapter.SealHolder>() {

    private val items = ArrayList<SealRecord>()

    fun submit(records: List<SealRecord>) {
        items.clear()
        items.addAll(records)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SealHolder {
        val binding = ItemSealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SealHolder(binding)
    }

    override fun onBindViewHolder(holder: SealHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SealHolder(private val binding: ItemSealBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: SealRecord) {
            val context = binding.root.context
            if (record.earned) {
                binding.imageSealItem.setImageResource(R.drawable.sprite_seal_gold)
                binding.imageSealItem.alpha = 1f
                binding.imageSealItem.contentDescription =
                    context.getString(R.string.fmt_seal_earned, record.chapterTitle)
                binding.textSealItem.text = record.chapterTitle
                binding.textSealItem.setTextColor(
                    context.getColor(R.color.text_primary)
                )
            } else {
                binding.imageSealItem.setImageResource(R.drawable.sprite_lock_cage)
                binding.imageSealItem.alpha = 0.35f
                binding.imageSealItem.contentDescription =
                    context.getString(R.string.fmt_seal_locked, record.chapterTitle)
                binding.textSealItem.setText(R.string.seal_locked)
                binding.textSealItem.setTextColor(
                    context.getColor(R.color.text_faint)
                )
            }
        }
    }
}
