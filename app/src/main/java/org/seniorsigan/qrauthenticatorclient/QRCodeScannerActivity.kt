package org.seniorsigan.qrauthenticatorclient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.zxing.Result
import com.google.zxing.BarcodeFormat
import me.dm7.barcodescanner.zxing.ZXingScannerView

class QRCodeScannerActivity: AppCompatActivity(), ZXingScannerView.ResultHandler {
    val formats = listOf(BarcodeFormat.QR_CODE)
    lateinit var mScannerView: ZXingScannerView

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        when(requestCode) {
            App.CAN_USE_CAMERA -> {
                if (grantResults != null && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permissions on Camera obtained")
                    startScanner()
                }
                else {
                    Toast.makeText(
                            applicationContext,
                            "You have not permission to read from external storage!",
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun startScanner() {
        mScannerView = ZXingScannerView(this)
        mScannerView.setFormats(formats)
        setContentView(mScannerView)
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state);
        setContentView(R.layout.activity_qrcodescanner)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), App.CAN_USE_CAMERA)
        } else {
            Log.d(TAG, "Permissions on Camera obtained")
            startScanner()
        }
    }

    override fun onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    override fun onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        // Do something with the result here
        Log.v(TAG, rawResult.text); // Prints scan results
        Log.v(TAG, rawResult.barcodeFormat.toString()); // Prints the scan format (qrcode, pdf417 etc.)
        val intent = Intent(this, TokenParserActivity::class.java)
        intent.putExtra(RAW_TOKEN_INTENT, rawResult.text)
        startActivity(intent)
    }
}