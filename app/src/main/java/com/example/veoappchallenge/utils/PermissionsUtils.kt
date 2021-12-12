package com.example.veoappchallenge.utils

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.example.veoappchallenge.R

// This class was taken from Google's documentation on Permissions
class PermissionsUtils {
    fun requestPermission(
        activity: AppCompatActivity,
        requestId: Int,
        permission: String,
        finishActivity: Boolean
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            RationaleDialog().newInstance(requestId, finishActivity)
                .show(activity.supportFragmentManager, "dialog")
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestId)
        }
    }

    fun isPermissionGranted(grantPermissions: Array<String>, grantResults: IntArray): Boolean {
        grantPermissions.forEachIndexed { index, per ->
            if (per == grantPermissions[index]) {
                return grantResults[index] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    inner class PermissionDeniedDialog : DialogFragment() {
        private val finishActivityKey = "finish"
        private var finishActivity = false

        fun newInstance(finishActivity: Boolean): PermissionDeniedDialog {
            val arguments = Bundle()
            arguments.putBoolean(finishActivityKey, finishActivity)
            val dialog = PermissionDeniedDialog()
            dialog.arguments = arguments
            return dialog
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            finishActivity = arguments?.getBoolean(finishActivityKey) ?: false
            return AlertDialog.Builder(activity)
                .setMessage("Permission denied")
                .setPositiveButton("Ok", null)
                .create()
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            if (finishActivity) {
                Toast.makeText(activity, "GPS permission required", Toast.LENGTH_LONG).show()
            }
        }
    }

    inner class RationaleDialog : DialogFragment() {
        private val permissionRequestCodeKey = "requestCode"
        private val finishActivityKey = "finish"
        private var finishActivity = false

        fun newInstance(requestCode: Int, finishActivity: Boolean): RationaleDialog {
            val arguments = Bundle()
            arguments.putInt(permissionRequestCodeKey, requestCode)
            arguments.putBoolean(finishActivityKey, finishActivity)
            val dialog = RationaleDialog()
            dialog.arguments = arguments
            return dialog
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val arguments = arguments
            val requestCode = arguments?.getInt(permissionRequestCodeKey)
            finishActivity = arguments?.getBoolean(finishActivityKey) ?: false

            return AlertDialog.Builder(activity)
                .setMessage(getString(R.string.permission_rationale))
                .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    activity?.let {
                        ActivityCompat.requestPermissions(
                            it,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            requestCode!!
                        )
                    }
                    finishActivity = false
                }
                .setNegativeButton(getString(R.string.decline), null)
                .create()
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            if (finishActivity) {
                activity?.finish()
            }
        }

    }
}