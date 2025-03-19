package com.example.autoclean.ui.auth.kits

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.autoclean.R
import com.example.autoclean.ui.auth.documents.AlertModal

class AlertVerificationModal: DialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_upload_confirmation_fragment, container, false)
    }

    companion object {
        fun newInstance(): AlertVerificationModal {
            return AlertVerificationModal()
        }
    }
}