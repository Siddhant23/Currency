package com.japan.paypay.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.japan.paypay.R
import com.japan.paypay.model.data.CurrencyAmount
import com.japan.paypay.utils.display

class RatesAdapter(
    private val context: Context,
    private var list: ArrayList<CurrencyAmount>? = null
) : RecyclerView.Adapter<RatesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_rate, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(position)
    }

    fun update(newList: List<CurrencyAmount>?) {
        if (newList == null) return
        val result = DiffUtil.calculateDiff(DiffCallback(list ?: listOf(), newList))
        list?.clear()
        list?.addAll(newList)
        result.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(private val view: View) :
        RecyclerView.ViewHolder(view) {
        private val tvAmt: TextView = view.findViewById(R.id.tv_amt)
        private val tvCurrency: TextView = view.findViewById(R.id.tv_currency)
        fun bindData(position: Int) {
            val data = list?.get(position) ?: return
            tvAmt.text = display(data)
            tvCurrency.text = data.currency.name
        }
    }

    inner class DiffCallback(
        private val oldList: List<CurrencyAmount>,
        private val newList: List<CurrencyAmount>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}