package kenchovi.androiddev.gnavirestaurants

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.lang.Exception

class CallConfirmDialogFragment(private val tel: String, private val button: View) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create confirmation dialog
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.confirm_dialog_title)
        builder.setMessage(getString(R.string.call_confirm_dialog) + "\r\n$tel")
        builder.setPositiveButton(R.string.dialog_pos, DialogButtonClickListener())
        builder.setNegativeButton(R.string.dialog_neg, DialogButtonClickListener())
        return builder.create()
    }

    private inner class DialogButtonClickListener : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface, which: Int) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                try {
                    // Making a call
                    val uri = Uri.parse("tel:$tel")
                    val intentTel = Intent(Intent.ACTION_CALL, uri)
                    // to call
                    startActivity(intentTel)
                } catch (e: Exception) {
                    // Processing on error; Toast display
                    Toast.makeText(activity, getString(R.string.toast_failed), Toast.LENGTH_SHORT).show()
                }
            } else {
                // Processing on cancel; Toast display
                Toast.makeText(activity, getString(R.string.toast_canceled), Toast.LENGTH_SHORT).show()
            }
            // Button available
            button.isEnabled = true
        }
    }
}