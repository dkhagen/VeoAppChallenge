package com.example.veoappchallenge.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.veoappchallenge.R
import com.example.veoappchallenge.model.TripEntity
import com.example.veoappchallenge.utils.LocationUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * This adapter class hooks into the recycler view to show all trips in the database to the user.
 * It also allows the user to delete some or all of their trips and updates the recycler view in
 * real time.
 */
class TripsAdapter(
    private var tripList: List<TripEntity>,
    private val onClickListener: TripClickListener
) :
    RecyclerView.Adapter<TripsAdapter.TripsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.trip_row, parent, false)
        return TripsViewHolder(view)
    }

    // To display the data properly to the user, we utilize a number of helper functions.
    override fun onBindViewHolder(holder: TripsViewHolder, position: Int) {
        val tripItem = tripList[position]
        holder.tvStartTime.text = getDateString(tripItem.startTime)
        holder.tvDistance.text = LocationUtils.updateDistanceText(tripItem.totalDistance, false)
        holder.tvDuration.text = getTimeString(tripItem.duration)
        holder.tvAvgSpeed.text = getAvgSpeed(tripItem)
        holder.itemView.setOnClickListener {
            onClickListener.onTripClick(tripList[position])
        }
    }

    // Converts seconds to minutes and seconds in a XX:XX format.
    private fun getTimeString(duration: Int?): String {
        return if (duration != null) {
            val seconds = duration.mod(60)
            val minutes = duration / 60
            if (seconds < 10) {
                return "$minutes:0$seconds"
            }
            "$minutes:$seconds"
        } else {
            NULL_TIME
        }
    }

    // Converts the timestamp in millis to a date format.
    private fun getDateString(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return formatter.format(calendar.time)
    }

    // Calculates the average speed in mph from the duration and total distance travelled.
    private fun getAvgSpeed(trip: TripEntity): String {
        // duration in seconds
        val duration = trip.duration
        // distance in meters
        val distance = trip.totalDistance
        return if (duration != null && distance != null) {
            val metersPerSecond = distance.div(duration.toFloat())
            val milesPerHour = metersPerSecond.times(METERS_PER_SECOND_TO_MILES_PER_HOUR_FACTOR)
            "%.2f mph".format(milesPerHour)
        } else {
            NULL_SPEED
        }
    }

    override fun getItemCount(): Int {
        return tripList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<TripEntity>) {
        this.tripList = list
        notifyDataSetChanged()
    }

    companion object {
        const val NULL_TIME = "-:-"
        const val NULL_SPEED = "- -"
        const val METERS_PER_SECOND_TO_MILES_PER_HOUR_FACTOR = 2.23694f
    }

    // ViewHolder class that binds the text views
    class TripsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvDistance: TextView = view.findViewById(R.id.tv_trip_distance)
        var tvStartTime: TextView = view.findViewById(R.id.tv_start_time)
        var tvDuration: TextView = view.findViewById(R.id.tv_trip_duration)
        var tvAvgSpeed: TextView = view.findViewById(R.id.tv_avg_speed)
    }

    interface TripClickListener {
        fun onTripClick(data: TripEntity) {}
    }
}