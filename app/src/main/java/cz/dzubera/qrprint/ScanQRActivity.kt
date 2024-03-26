package cz.dzubera.qrprint

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class ScanQRActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var mScannerView: ZXingScannerView? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mScannerView = ZXingScannerView(this) // Programmatically initialize the scanner view
        setContentView(mScannerView) // Set the scanner view as
        supportActionBar?.setTitle("Skenování")
/*        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)// the content view*/
    }

    override fun onResume() {
        super.onResume()
        mScannerView?.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView?.startCamera() // Start camera on resume
    }

    override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera() // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        val result = rawResult.text
        val split = result.split(";")
        if (split.isNotEmpty()) {
            StaticStorage.scannedTextProductNumber = split[0]
            StaticStorage.scannedTextProductionCommandNumber = split[1]

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Scanned code is: $result", Toast.LENGTH_LONG).show()
        }


    }

    private fun setPreferences(context: Context, address: String): Boolean? {
        return context.getSharedPreferences("APP", Context.MODE_PRIVATE).edit()
            ?.putString("address", address)?.commit()
    }
}