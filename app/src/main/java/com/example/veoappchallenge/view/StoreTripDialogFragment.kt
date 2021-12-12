package com.example.veoappchallenge.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.veoappchallenge.R

// A simple dialog fragment that prompts the user to store the most recent Trip
class StoreTripDialogFragment(private val listener: DialogInterface.OnClickListener) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.trip_complete))
            .setMessage(getString(R.string.store_trip_question))
            .setPositiveButton(getString(R.string.confirm), listener)
            .setNegativeButton(getString(R.string.decline), null)
            .create()
    }

    companion object {
        const val TAG = "StoreTripDialog"
    }
}