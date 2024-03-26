package cz.dzubera.qrprint

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.Color.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import honeywell.printer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    fun callback() {
        GlobalScope.launch(Dispatchers.Main) {
            view?.findViewById<TextView>(R.id.date_time)?.text =
                SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(System.currentTimeMillis())
                    .toString()

            val current = view?.findViewById<TextView>(R.id.production_code)?.text.toString()
            if (current != prodCodeFull()) {
                view?.findViewById<TextView>(R.id.production_code)?.text = prodCodeFull()
            }

            view?.findViewById<TextView>(R.id.product_number)?.text =
                StaticStorage.scannedTextProductNumber.toString()
            view?.findViewById<TextView>(R.id.production_command_number)?.text =
                StaticStorage.scannedTextProductionCommandNumber.toString()

            view?.findViewById<TextView>(R.id.product_count)?.text =
                StaticStorage.productCount.toString()

            view?.findViewById<TextView>(R.id.printed_count)?.text =
                StaticStorage.printedCount.toString()




            if (StaticStorage.productTimer != 0 && App.timeManager.countdown >= StaticStorage.productTimer - 2) {
                view?.findViewById<Button>(R.id.button_print_datamax)?.let {
                    animateButton(it)

                }

            } else {
                view?.findViewById<Button>(R.id.button_print_datamax)
                    ?.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.han_purple_light
                    )
                )
            }

            if (StaticStorage.productCount == 1) {
                view?.findViewById<LinearLayout>(R.id.layout_product_count)?.let {
                    shakeAnimation(it)
                }

            }

            view?.findViewById<TextView>(R.id.package_count)?.let {
                if(StaticStorage.productCount == 0){
                    it.text = "Počet kusů v balení"
                } else {
                    it.text = "Chybí kusů v balení"
                }
            }

            if(StaticStorage.productCount > 0 && StaticStorage.printedCount > 0) {
                if (StaticStorage.printedCount % StaticStorage.productCount == 0) {
                    VibratorUtils.vibrate(requireContext())
                    view?.findViewById<LinearLayout>(R.id.layout_message_last_product)?.let {
                        it.visibility = View.VISIBLE
                    }
                } else {
                    view?.findViewById<LinearLayout>(R.id.layout_message_last_product)?.let {
                        it.visibility = View.GONE
                    }
                }
            } else {
                view?.findViewById<LinearLayout>(R.id.layout_message_last_product)?.let {
                    it.visibility = View.GONE
                }
            }


            onConnectionChanged(App.printerConnectionManager.connectionStatus)

        }
    }

    private fun onConnectionChanged(status: PrinterConnectionManager.Status) {
        view?.let {
            view?.findViewById<TextView>(R.id.connected_ip_address)?.text =
                "${StaticStorage.datamaxIp}"
            view?.findViewById<TextView>(R.id.connection_status)?.text = when (status) {
                PrinterConnectionManager.Status.CONNECTED -> "Připojeno"
                PrinterConnectionManager.Status.DISCONNECTED -> "Odpojeno"
            }

            /*   when (status) {
                   PrinterConnectionManager.Status.CONNECTED -> view?.findViewById<TextView>(R.id.connection_status)
                       ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                   PrinterConnectionManager.Status.DISCONNECTED -> view?.findViewById<TextView>(R.id.connection_status)
                       ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
               }*/


            when (status) {
                PrinterConnectionManager.Status.CONNECTED -> view?.findViewById<CardView>(R.id.connection_status_card)
                    ?.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green))
                PrinterConnectionManager.Status.DISCONNECTED -> view?.findViewById<CardView>(R.id.connection_status_card)
                    ?.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.timeManager.registerCallback(::callback)

        view.findViewById<TextView>(R.id.date_time).text =
            SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(System.currentTimeMillis()).toString()
        view.findViewById<TextView>(R.id.product_count).text = StaticStorage.productCount.toString()


        view?.findViewById<TextView>(R.id.production_code)?.isSelected = true


        StaticStorage.datamaxIp =
            requireContext().getSharedPreferences("STORAGE", Context.MODE_PRIVATE)
                .getString("address", "0.0.0.0")
                .toString()
        StaticStorage.datamaxPort =
            requireContext().getSharedPreferences("STORAGE", Context.MODE_PRIVATE)
                .getString("port", "0").toString()
                .toIntOrNull() ?: 0




        onConnectionChanged(App.printerConnectionManager.connectionStatus)



        view.findViewById<Button>(R.id.button_print_datamax).setOnClickListener {


            if (StaticStorage.pressLimit > 0) {
                shakeAnimation(view.findViewById<Button>(R.id.button_print_datamax))
                return@setOnClickListener
            }

            StaticStorage.pressLimit = 5

            val bitmap = createQR()!!
            val isDisconnected =
                App.printerConnectionManager.connectionStatus == PrinterConnectionManager.Status.DISCONNECTED
            goPrint(bitmap, isDisconnected)

        }


    }

    private fun goPrint(bitmap: Bitmap?, isDisconnected: Boolean) {

        GlobalScope.launch {

            if (isDisconnected) {
                try {
                    App.printerConnectionManager.connect(
                        StaticStorage.datamaxIp,
                        StaticStorage.datamaxPort
                    )
                    delay(2000)
                } catch (e: Exception) {
                    GlobalScope.launch(Dispatchers.Main) {

                        context?.let {
                            Toast.makeText(
                                it,
                                e.message ?: e.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                    return@launch

                }

            }
            try {
                val documentDPL = DocumentDPL()
                val parametersDPL = ParametersDPL()
                parametersDPL.alignment = ParametersDPL.Alignment.Center
                documentDPL.writeImage(
                    bitmap,
                    StaticStorage.yCalibration,
                    StaticStorage.xCalibration,
                    parametersDPL
                ) //row je vyska //col sirka
                App.printerConnectionManager.write(documentDPL.documentData)

                GlobalScope.launch(Dispatchers.Main) {

                    StaticStorage.printedCount += 1
                    StaticStorage.productCount -= 1
                    if (StaticStorage.productCount <= 0) {
                        StaticStorage.productCount = StaticStorage.productCountTotal
                    }


                    App.timeManager.resetCountDown()
                }


            } catch (e: Exception) {
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        e.message ?: "Neznámá chyba",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        }

    }

    fun createQR(): Bitmap? {

        val productionCode = ProductionCode(
            StaticStorage.scannedTextProductNumber,
            StaticStorage.scannedTextProductionCommandNumber,
            System.currentTimeMillis()
        )
        return generateQrCodeWithOverlay(
            productionCode.toString(), productionCode.getProductPartNumber()
        )
    }

    fun showImageDialog(bitmap: Bitmap?) {
        val image = ImageView(context)
        image.setImageBitmap(bitmap)
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context)
                .setPositiveButton(
                    "OK"
                ) { dialog, which -> dialog.dismiss() }.setView(image)
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        App.timeManager.unregister(::callback)
    }


    private fun generateQrCodeWithOverlay(qrCodeData: String, overlayText: Int?): Bitmap? {

        val hints = HashMap<EncodeHintType?, Any?>()
        // The Error Correction level H provide a QR Code that can be covered by 30%
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H

        val writer = QRCodeWriter()



        try {
            // Create a QR Code from qrCodeData and 512 by 512 pixels, same size as my Logo
            val encodedQrCode = writer.encode(
                qrCodeData,
                BarcodeFormat.QR_CODE,
                StaticStorage.qrCodeSize,
                StaticStorage.qrCodeSize,
                hints
            )

            var qrCodeBitmap: Bitmap = createBitmap(encodedQrCode)!!
            if (overlayText != null) {
                val logo = textAsBitmap(
                    overlayText.toString(),
                    StaticStorage.qrCodeTextSize,
                    Color.BLACK
                )!!
                val qrCodeCanvas = Canvas(qrCodeBitmap)

                // Used to resize the image
                val scaleFactor = 1

                // Resizing the logo increasing the density to keep it sharp
                logo.density = logo.density * scaleFactor

                val xLogo = (StaticStorage.qrCodeSize - logo.width / scaleFactor) / 2f
                val yLogo = (StaticStorage.qrCodeSize - logo.height / scaleFactor) / 2f


                qrCodeCanvas.drawBitmap(logo, xLogo, yLogo, null)


            }

            return qrCodeBitmap


        } catch (e: Exception) {
            Log.d("XXX CCC", e.message ?: "none")
            return null
        }
    }

    fun textAsBitmap(text: String?, textSize: Float, textColor: Int): Bitmap? {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize

        paint.textAlign = Paint.Align.LEFT
        val baseline = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.5f).toInt() + 1// round
        val height = (baseline + paint.descent()).toInt() + 1
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(image)
        paint.color = WHITE

        canvas.drawRect(Rect(0, 0, width, height), paint)
        paint.color = RED
        canvas.drawText(text!!, 0f, baseline, paint)
        return image
    }

    fun createBitmap(matrix: BitMatrix): Bitmap? {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix[x, y]) BLACK else WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    fun animateButton(button: Button) {

        val colorAnimation = ValueAnimator.ofObject(
            ArgbEvaluator(),
            Color.BLUE,
            Color.CYAN,
            Color.RED,
            ContextCompat.getColor(requireContext(), R.color.han_purple_light)
        )
        colorAnimation.duration = 200 // milliseconds

        colorAnimation.addUpdateListener { animator -> button.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.repeatCount = 4
        colorAnimation.start()
    }

    fun shakeAnimation(view: View) {
        ObjectAnimator
            .ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
            .setDuration(500)
            .start();
    }

    fun prodCodeFull(): String {
        val date = SimpleDateFormat("yyyyMMdd").format(Date(System.currentTimeMillis())).toString()
        val time = SimpleDateFormat("HHmm").format(System.currentTimeMillis()).toString()
        return "${StaticStorage.scannedTextProductNumber}.$date.$time.VP-${StaticStorage.scannedTextProductionCommandNumber}"
    }
}