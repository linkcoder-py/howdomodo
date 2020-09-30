package com.ssafy.howdomodo.ui.theater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.howdomodo.R
import com.ssafy.howdomodo.data.datasource.model.Theater
import kotlinx.android.synthetic.main.item_theater.view.*

class TheaterAdapter(private val onclick: TheaterViewHolder.TheaterClickListener) :
    RecyclerView.Adapter<TheaterAdapter.TheaterViewHolder>() {

    val theaterData = ArrayList<Theater>()


    fun setTheaterData(newData: List<Theater>) {
        with(theaterData) {
            clear()
            addAll(newData)
        }
        notifyDataSetChanged()
    }

    fun getClicked(position: Int): Boolean {
        return theaterData[position].isClicked
    }

    fun getClickedTheater(): Int {
        for (i in theaterData.indices) {
            if (theaterData[i].isClicked) {
                return i
            }
        }
        return -1
    }

    fun setClicked(position: Int, status: Boolean) {
        theaterData[position].isClicked = status

        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TheaterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_theater, parent, false)
        return TheaterViewHolder(
            view,
            onclick
        )
    }

    override fun getItemCount(): Int {
        return theaterData.size
    }

    override fun onBindViewHolder(holder: TheaterViewHolder, position: Int) {
        holder.bind(theaterData[position],)
    }

    class TheaterViewHolder(
        itemView: View,
        private val theaterClickListener: TheaterClickListener
    ) : RecyclerView.ViewHolder(itemView) {

        interface TheaterClickListener {
            fun onclick(position: Int, textView: TextView)
        }

        init {
            itemView.item_theater_cl_box.setOnClickListener {
                theaterClickListener.onclick(
                    adapterPosition,
                    itemView.item_theater_tv_name
                )
            }
        }


        fun bind(data: Theater) {
            var name = data.theaterBrand + " " + data.theaterName
            var address = data.theaterAddress
            var theater_lat = data.theaterLat
            var theater_lng = data.theaterLng

            itemView.item_theater_tv_name.text = name
            itemView.item_theater_tv_desc.text = address

            if (data.theaterBrand == "CGV") {
                itemView.item_theater_iv_photo.setImageResource(R.drawable.cgv)
            } else if (data.theaterBrand == "메가박스") {
                itemView.item_theater_iv_photo.setImageResource(R.drawable.megabox)
            } else if (data.theaterBrand == "롯데시네마") {
                itemView.item_theater_iv_photo.setImageResource(R.drawable.lottecinema)
            }

        }
    }
}