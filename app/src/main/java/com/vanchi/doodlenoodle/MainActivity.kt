package com.vanchi.doodlenoodle

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.vanchi.doodlenoodle.databinding.ActivityMainBinding
import com.vanchi.doodlenoodle.databinding.DialogBrushSizeBinding

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var binding : ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        drawingView = binding!!.drawingView
        drawingView!!.setBrushSize(30.toFloat())
        binding?.paintBrush?.setOnClickListener {
            showBrushSizeDialog()
        }
    }

    fun showBrushSizeDialog(){
        val brushDialog = Dialog(this)
        val brushDialogBinding: DialogBrushSizeBinding = DialogBrushSizeBinding.inflate(layoutInflater)
        brushDialog.setContentView(brushDialogBinding.root)
        val smallBrush = brushDialogBinding.smallBrush
        val mediumBrush = brushDialogBinding.mediumBrush
        val largeBrush = brushDialogBinding.largeBrush

        smallBrush.setOnClickListener {
            drawingView?.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        }
        mediumBrush.setOnClickListener {
            drawingView?.setBrushSize(25.toFloat())
            brushDialog.dismiss()
        }
        largeBrush.setOnClickListener {
            drawingView?.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }
}