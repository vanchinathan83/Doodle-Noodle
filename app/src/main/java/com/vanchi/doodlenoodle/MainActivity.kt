package com.vanchi.doodlenoodle

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.vanchi.doodlenoodle.databinding.ActivityMainBinding
import com.vanchi.doodlenoodle.databinding.DialogBrushSizeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.logging.Logger

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

        binding?.save?.setOnClickListener {
            if(isReadStorageAllowed()){
                lifecycleScope.launch{
                    val frameLayout = binding?.flDrawingView
                    val bitmap = frameLayout?.let { fl -> getBitmapFromView(fl) }
                    bitmap?.let { bm -> saveImage(bm)}
                }
            }else{
                storageActivityResultLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }

        binding?.ibPhotoChoose?.setOnClickListener {
            requestPermission()
        }

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && shouldShowRequestPermissionRationale(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {
            showReasonDialog("Storage Permission", "Storage Permission Denied!")
        } else {
            storageActivityResultLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
            val storageIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            openGalleryLauncher.launch(storageIntent)
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
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

    private fun getBitmapFromView(view: View) : Bitmap {
        val bitmap: Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas: Canvas = Canvas(bitmap)
        val backGround = view.background
        if(backGround != null){
            backGround.draw(canvas)
        }else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)

        return bitmap
    }

    private suspend fun saveImage(bitmap: Bitmap) : String {
        var result = ""
        withContext(Dispatchers.IO){
            try{
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG,90, baos)
                val path = cacheDir?.absolutePath?.toString()
                val f = File(path + File.separator +
                "KidDrawingApp_"+ System.currentTimeMillis() /1000 + ".png")
                val fo = FileOutputStream(f)
                fo.write(baos.toByteArray())
                fo.close()
                result = f.absolutePath
                runOnUiThread{
                    if(result.isNotEmpty()){
                        Toast.makeText(this@MainActivity,
                        "File Saved at $result",
                        Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@MainActivity,
                            "Error saving file!!",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e: Exception){
                result = ""
                Log.e(Log.ERROR.toString(),e.printStackTrace().toString())
            }
        }
        return  result
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