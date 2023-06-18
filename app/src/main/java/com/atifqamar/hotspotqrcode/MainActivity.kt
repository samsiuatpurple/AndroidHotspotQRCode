package com.atifqamar.hotspotqrcode

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import org.w3c.dom.Text
import java.lang.reflect.Method

@SuppressLint("LongLogTag")
class MainActivity : AppCompatActivity() {
    private val TAG = "com.atifqamar.hotspotqrcode.MainActivity"
    private lateinit var btQrCode: Button
    private lateinit var imgQrCode: ImageView
    private lateinit var tvSSid: TextView
    private lateinit var tvPassword: TextView
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(TAG, "${it.key} = ${it.value}")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (it.key.equals("android.permission.NEARBY_WIFI_DEVICES") && !it.value) {
                        Toast.makeText(this, "Please allow nearby permission", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        createHotStop()
                    }
                } else {
                    if (it.key.equals("android.permission.ACCESS_FINE_LOCATION") && !it.value) {
                        Toast.makeText(this, "Please allow nearby permission", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        createHotStop()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }

    private fun initUI() {
        btQrCode = findViewById(R.id.btQrCode)
        imgQrCode = findViewById(R.id.imgQrCode)
        tvSSid = findViewById(R.id.tvSSid)
        tvPassword = findViewById(R.id.tvPassword)
        btQrCode.setOnClickListener {

            requestMultiplePermissions.launch(
                arrayOf(
                    android.Manifest.permission.NEARBY_WIFI_DEVICES,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )

        }
    }

    private fun createHotStop() {
        Log.d(TAG, "createHotStop()")
        val apManager = APManager.getApManager(this)
        apManager.turnOnHotspot(this,
            { ssid, password ->
                Log.d(TAG, "ssid $ssid password : $password")
                val qrCode = generateQRCode(ssid, password)
                imgQrCode.setImageBitmap(qrCode)
            }) { failureCode, e ->
            Log.e(TAG, "Error : ${e?.message} failureCode : ${failureCode}")
        }
    }


    @Throws(WriterException::class)
    fun generateQRCode(ssid: String, password: String): Bitmap? {
        Log.d(TAG, "generateQRCode() ssid : ${ssid} password : ${password}")
        tvSSid.text = "SSID : ${ssid}"
        tvPassword.text = "Password : ${password}"


        val qrCodeCanvas: BitMatrix
        val size = 800 //pixels
        val qrCodeContent = "WIFI:S:$ssid;T:WPA;P:$password;;"
        qrCodeCanvas = MultiFormatWriter().encode(
            qrCodeContent,
            BarcodeFormat.QR_CODE,
            size,
            size,

            )
        val w = qrCodeCanvas.width
        val h = qrCodeCanvas.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] =
                    if (qrCodeCanvas[x, y]) Color.BLACK else Color.WHITE
            }
        }
        val qrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        qrBitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return qrBitmap
    }


}