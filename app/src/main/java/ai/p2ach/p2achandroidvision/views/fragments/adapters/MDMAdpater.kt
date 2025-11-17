package ai.p2ach.p2achandroidvision.views.fragments.adapters

import ai.p2ach.p2achandroidvision.databinding.ItemMdmHeaderBinding
import ai.p2ach.p2achandroidvision.databinding.ItemMdmRowBinding
import ai.p2ach.p2achandroidvision.utils.MdmUiItem
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_ROW = 1

class MDMAdapter :
    ListAdapter<MdmUiItem, RecyclerView.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<MdmUiItem>() {
        override fun areItemsTheSame(oldItem: MdmUiItem, newItem: MdmUiItem): Boolean {
            return when {
                oldItem is MdmUiItem.Header && newItem is MdmUiItem.Header ->
                    oldItem.title == newItem.title
                oldItem is MdmUiItem.Row && newItem is MdmUiItem.Row ->
                    oldItem.key == newItem.key
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: MdmUiItem, newItem: MdmUiItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MdmUiItem.Header -> VIEW_TYPE_HEADER
            is MdmUiItem.Row -> VIEW_TYPE_ROW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemMdmHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemMdmRowBinding.inflate(inflater, parent, false)
                RowViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is MdmUiItem.Header -> (holder as HeaderViewHolder).bind(item)
            is MdmUiItem.Row -> (holder as RowViewHolder).bind(item)
        }
    }

    class HeaderViewHolder(
        private val binding: ItemMdmHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MdmUiItem.Header) {
            binding.tvTitle.text = item.title
        }
    }

    class RowViewHolder(
        private val binding: ItemMdmRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MdmUiItem.Row) {
            binding.tvKey.text = item.key
            binding.tvValue.text = item.value
        }
    }
}