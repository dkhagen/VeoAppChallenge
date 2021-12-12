package com.example.veoappchallenge.view

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.veoappchallenge.R
import com.example.veoappchallenge.model.TripEntity
import com.example.veoappchallenge.viewmodel.TripsActivityViewModel

/**
 * This activity simply shows the user all the stored Trips in the database and allows them to
 * delete them if they tap on them in the recycler view or they can delete all at once with the
 * Clear All button.
 */
class ViewTripsActivity : AppCompatActivity(), TripsAdapter.TripClickListener {
    private lateinit var tripsActivityViewModel: TripsActivityViewModel
    private lateinit var rvTrips: RecyclerView
    private lateinit var btnClearAll: Button
    private lateinit var adapter: TripsAdapter
    private var tripList: ArrayList<TripEntity> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_view_trips)
        adapter = TripsAdapter(tripList, this)
        tripsActivityViewModel = ViewModelProvider(this).get(TripsActivityViewModel::class.java)
        bindViews()
        btnClearAll.setOnClickListener(getClearAllOnClickListener())
        tripsActivityViewModel.getAllTrips()
    }

    override fun onResume() {
        super.onResume()
        tripsActivityViewModel.getAllTrips()
    }

    private fun bindViews() {
        rvTrips = findViewById(R.id.rv_trips)
        rvTrips.layoutManager = LinearLayoutManager(this)
        tripsActivityViewModel.liveDataTripList.observe(this, {
            it.let(adapter::setData)
        })
        rvTrips.adapter = adapter
        btnClearAll = findViewById(R.id.btn_clear_all)
    }

    override fun onTripClick(data: TripEntity) {
        val dialog = getDeleteDialogFragment(data)
        dialog.show()
    }

    private fun getClearAllOnClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val dialog = getDeleteDialogFragment(null)
            dialog.show()
        }
    }

    /**
     * We use the same dialog fragment template for both a single deletion and deleting all trips,
     * just with different strings passed in.
     */
    private fun getDeleteDialogFragment(data: TripEntity?): Dialog {
        val dialog = AlertDialog
            .Builder(this)
        return if (data != null) {
            dialog
                .setTitle(getString(R.string.delete_trip_prompt))
                .setMessage(getString(R.string.delete_trip_message))
                .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    tripsActivityViewModel.deleteTrip(data)
                    tripsActivityViewModel.getAllTrips()
                }
                .setNegativeButton(getString(R.string.decline), null)
                .create()
        } else {
            dialog
                .setTitle(getString(R.string.delete_trip_prompt))
                .setMessage(getString(R.string.delete_all_trips_message))
                .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    tripsActivityViewModel.deleteAllTrips()
                }
                .setNegativeButton(getString(R.string.decline), null)
                .create()
        }
    }
}