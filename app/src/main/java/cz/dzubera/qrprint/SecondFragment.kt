package cz.dzubera.qrprint

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)



        return view
    }


    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.product_number).text =
            StaticStorage.scannedTextProductNumber
        view.findViewById<TextView>(R.id.production_command_number).text =
            StaticStorage.scannedTextProductionCommandNumber
        view.findViewById<TextView>(R.id.pieces_count).text =
            StaticStorage.productCountTotal.toString()
        view.findViewById<TextView>(R.id.time_count).text =
            StaticStorage.productTimer.toString()
        view.findViewById<TextView>(R.id.version).text = BuildConfig.VERSION_NAME

        onConnectionChanged(App.printerConnectionManager.connectionStatus)

        view.findViewById<TextView>(R.id.ip_address).text = StaticStorage.datamaxIp
        view.findViewById<TextView>(R.id.ip_port).text = StaticStorage.datamaxPort.toString()

        view.findViewById<Button>(R.id.reset_counter).setOnClickListener {
            StaticStorage.printedCount = 0
            Toast.makeText(requireContext(), "Vyunlováno", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<ImageButton>(R.id.button_scan).setOnClickListener {
            val intent = Intent(context, ScanQRActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.button_second).setOnClickListener {


            StaticStorage.datamaxIp =
                view.findViewById<TextView>(R.id.ip_address).text.toString()

            StaticStorage.datamaxPort =
                view.findViewById<TextView>(R.id.ip_port).text.toString().toInt()

            requireContext().getSharedPreferences("STORAGE", Context.MODE_PRIVATE).edit()
                .putString("address", StaticStorage.datamaxIp)
                .putString("port", StaticStorage.datamaxPort.toString()).apply()


            StaticStorage.productTimer =
                view.findViewById<EditText>(R.id.time_count).editableText.toString().toIntOrNull()
                    ?: 0
            val prodCount =
                view.findViewById<EditText>(R.id.pieces_count).editableText.toString().toIntOrNull()
                    ?: 0
            StaticStorage.productCountTotal = prodCount
            StaticStorage.productCount = prodCount

            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            App.timeManager.countdown = 0
        }

        view.findViewById<Button>(R.id.connect_datamax).setOnClickListener {
            val ip = view.findViewById<EditText>(R.id.ip_address).text.toString()
            val port = view.findViewById<EditText>(R.id.ip_port).text.toString()
            GlobalScope.launch {
                try {
                    if (App.printerConnectionManager.connectionStatus == PrinterConnectionManager.Status.DISCONNECTED) {
                        App.printerConnectionManager.connect(ip, port.toInt())
                    } else {
                        App.printerConnectionManager.close()
                    }
                } catch (e: Exception) {
                    GlobalScope.launch(Dispatchers.Main) {
                        this@SecondFragment.context?.let {
                            Toast.makeText(
                                it,
                                e.message ?: e.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                }
            }
        }
    }

    private fun onConnectionChanged(status: PrinterConnectionManager.Status) {
        view?.let {
            it.findViewById<TextView>(R.id.connection_settings_status)?.text = when (status) {
                PrinterConnectionManager.Status.CONNECTED -> "Připojeno"
                PrinterConnectionManager.Status.DISCONNECTED -> "Odpojeno"
            }

            it.findViewById<Button>(R.id.connect_datamax).text = when (status) {
                PrinterConnectionManager.Status.CONNECTED -> "Odpojit"
                PrinterConnectionManager.Status.DISCONNECTED -> "Připojit"
            }

        }
    }


    override fun onResume() {
        super.onResume()

        App.printerConnectionManager.callbacks.add(::onConnectionChanged)
    }

    override fun onPause() {
        super.onPause()
        App.printerConnectionManager.callbacks.remove(::onConnectionChanged)

    }
}