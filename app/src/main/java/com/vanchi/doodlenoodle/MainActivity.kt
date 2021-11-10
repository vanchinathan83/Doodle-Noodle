package com.vanchi.doodlenoodle

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.vanchi.doodlenoodle.databinding.ActivityMainBinding
import com.vanchi.doodlenoodle.databinding.DialogBrushSizeBinding

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var binding : ActivityMainBinding? = null
    private var mCurrentImageButtonPaint: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        val linearLayout = binding?.colorPallet
        mCurrentImageButtonPaint = linearLayout?.get(2) as ImageButton
        mCurrentImageButtonPaint!!.setImageDrawable (
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )
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

    fun paintPalletClicked(view: View) {
        if(view !== mCurrentImageButtonPaint){
            val imageButton = view as ImageButton
            val color = imageButton.tag.toString()
            drawingView!!.setColor(color)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )

            mCurrentImageButtonPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            mCurrentImageButtonPaint = view
        }
    }
}