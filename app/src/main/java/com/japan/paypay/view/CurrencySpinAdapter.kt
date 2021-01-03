package com.japan.paypay.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import com.japan.paypay.model.data.Currency

class CurrencySpinAdapter(
    private val context: Context,
    private val list: ArrayList<Currency>
) : BaseAdapter(), SpinnerAdapter {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = View.inflate(context, android.R.layout.simple_spinner_item, null)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = "${list[position].code} : ${list[position].name}"
        return view
    }

    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long =
        if (list[position].id < 1) position.toLong() else list[position].id

    override fun getCount(): Int = list.size
}