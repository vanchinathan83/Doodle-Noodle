package com.vanchi.doodlenoodle

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.android.material.snackbar.Snackbar
import com.vanchi.doodlenoodle.databinding.ActivityMainBinding
import com.vanchi.doodlenoodle.databinding.DialogBrushSizeBinding

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var binding : ActivityMainBinding? = null
    private var mCurrentImageButtonPaint: ImageButton? = null
    private var openGalleryLauncher:ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode == RESULT_OK && result.data != null){
                binding?.ivBackground?.setImageURI(result.data?.data)
            }
        }
    private var storageActivityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ results ->
            results.entries.forEach{entry ->
                    val permission = entry.key
                    val isGranted = entry.value
                    if (isGranted) {
                        when (permission) {
                            Manifest.permission.READ_EXTERNAL_STORAGE ->
                                Toast.makeText(
                                    applicationContext,
                                    "Storage Permission Granted!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }else {
                        when (permission) {
                            Manifest.permission.READ_EXTERNAL_STORAGE ->
                                Toast.makeText(
                                    applicationContext,
                                    "Storage Permission Denied!",
                                    Toast.LENGTH_SHORT
                                ).show()
                        }
                    }
                }
            }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        val linearLayout = binding?.colorPallet
        mCurrentImageButtonPaint = linearLayout?.get(3) as ImageButton
        mCurrentImageButtonPaint!!.setImageDrawable (
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )
        drawingView = binding!!.drawingView
        drawingView!!.setBrushSize(30.toFloat())
        binding?.paintBrush?.setOnClickListener {
            showBrushSizeDialog()
        }
        binding?.undo?.setOnClickListener {
            drawingView?.undoLastPath()
        }
        binding?.redo?.setOnClickListener {
            drawingView?.redoLastPath()
        }

        binding?.ibPhotoChoose?.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                showReasonDialog("Storage Permission", "Storage Permission Denied!")
            }else {
                storageActivityResultLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                val storageIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(storageIntent)
            }
        }

    }

    private fun showReasonDialog(title: String, message: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK"){dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
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