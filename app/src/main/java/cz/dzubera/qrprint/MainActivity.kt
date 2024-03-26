package cz.dzubera.qrprint

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        if (!StaticStorage.autoConnectTry && StaticStorage.datamaxPort != 0) {
            GlobalScope.launch {
                try {
                    if (App.printerConnectionManager.connectionStatus == PrinterConnectionManager.Status.DISCONNECTED) {
                        App.printerConnectionManager.connect(
                            StaticStorage.datamaxIp,
                            StaticStorage.datamaxPort
                        )
                    }
                } catch (e: Exception) {
                    GlobalScope.launch(Dispatchers.Main) {

                        Toast.makeText(
                            this@MainActivity,
                            e.message ?: e.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
            StaticStorage.autoConnectTry = true
        }

        setupActionBarWithNavController(this, navHostFragment.navController)
        ActivityCompat.requestPermissions(
            this,
            listOf(Manifest.permission.CAMERA).toTypedArray(), 11
        );
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    override fun onSupportNavigateUp(): Boolean {

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }

}