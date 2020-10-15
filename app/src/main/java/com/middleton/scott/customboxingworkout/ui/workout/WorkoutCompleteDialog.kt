package com.middleton.scott.customboxingworkout.ui.workout

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.middleton.scott.commandMeBoxing.R
import kotlinx.android.synthetic.main.dialog_workout_comlete.*

class WorkoutCompleteDialog(
    private val timeElapsedSecs: Int,
    private val combinationsThrown: Int,
    private val onRestart: (() -> Unit),
    private val onExit: (() -> Unit)
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_workout_comlete, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
        setClickListeners()

        elapsed_tv.text = timeElapsedSecs.toString()
        combos_thrown_tv.text = combinationsThrown.toString()
    }

    private fun setClickListeners() {
        restart_btn.setOnClickListener {
            onRestart()
            dismiss()
        }

        exit_btn.setOnClickListener {
            onExit()
            dismiss()
        }
    }
}