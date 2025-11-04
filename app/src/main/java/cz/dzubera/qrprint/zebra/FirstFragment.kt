package cz.dzubera.qrprint.zebra

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.provider.Telephony.Carriers.PASSWORD
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.view.ContextThemeWrapper
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    companion object{
        const val PASSWORD = "1239"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    fun showTextDialog(context: Context, onPrintConfirmed: (String) -> Unit) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputCode = EditText(context).apply {
            hint = "Text nad kódem"
        }

        val inputPassword = EditText(context).apply {
            hint = "Heslo"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        layout.addView(inputCode)
        layout.addView(inputPassword)

        val scrollView = ScrollView(context).apply {
            addView(layout)
        }

        AlertDialog.Builder(ContextThemeWrapper(context, R.style.Theme_Material3_DayNight_Dialog_Alert))
            .setTitle("Změna textu")

            .setView(scrollView).setPositiveButton("Uložit") { dialog, _ ->
                val code = inputCode.text.toString()
                val password = inputPassword.text.toString()
                if (password == PASSWORD) {
                    onPrintConfirmed(code)
                } else {
                    Toast.makeText(context, "Nesprávné heslo", Toast.LENGTH_SHORT).show()
                    //VibratorUtils.vibrate(context)
                }
                dialog.dismiss()
            }.setNegativeButton("Zpět") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    fun showTypeChangeDialog(context: Context, onActionConfirmed: () -> Unit) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }


        val inputPassword = EditText(context).apply {
            hint = "Heslo"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        layout.addView(inputPassword)

        val scrollView = ScrollView(context).apply {
            addView(layout)
        }

        AlertDialog.Builder(ContextThemeWrapper(context, R.style.Theme_Material3_DayNight_Dialog_Alert))
            .setTitle("Změna typu")

            .setView(scrollView).setPositiveButton("OK") { dialog, _ ->
                val password = inputPassword.text.toString()
                if (password == PASSWORD) {
                    onActionConfirmed()
                } else {
                    Toast.makeText(context, "Nesprávné heslo", Toast.LENGTH_SHORT).show()
                    //VibratorUtils.vibrate(context)
                }
                dialog.dismiss()
            }.setNegativeButton("Zpět") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    fun showPrintDialog(context: Context, onPrintConfirmed: (String) -> Unit) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputCode = EditText(context).apply {
            hint = "Kód k tisku"
        }

        val inputPassword = EditText(context).apply {
            hint = "Heslo"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        layout.addView(inputCode)
        layout.addView(inputPassword)

        val scrollView = ScrollView(context).apply {
            addView(layout)
        }

        AlertDialog.Builder(ContextThemeWrapper(context, R.style.Theme_Material3_DayNight_Dialog_Alert))
            .setTitle("Změna kódu")

            .setView(scrollView).setPositiveButton("Uložit") { dialog, _ ->
                val code = inputCode.text.toString()
                val password = inputPassword.text.toString()
                if (password == PASSWORD) {
                    onPrintConfirmed(code)
                } else {
                    Toast.makeText(context, "Nesprávné heslo", Toast.LENGTH_SHORT).show()
                    //VibratorUtils.vibrate(context)
                }
                dialog.dismiss()
            }.setNegativeButton("Zpět") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    fun getCode(): String {
        return requireContext().getSharedPreferences("STORAGE", Context.MODE_PRIVATE)
            .getString("code", "není nastaveno").toString()
    }

    fun getDimensions(context: Context): Dimensions {
        return Dimensions(
            context.getSharedPreferences("STORAGE", Context.MODE_PRIVATE).getInt("size", 12),
            context.getSharedPreferences("STORAGE", Context.MODE_PRIVATE).getInt("offsetX", 80),
            context.getSharedPreferences("STORAGE", Context.MODE_PRIVATE).getInt("offsetY", 80)
        )
    }

    @SuppressLint("SetTextI18n")
    fun changeStateView(view: View, context: Context?, address: String, port: Int) {
        context?.let {
            view.findViewById<TextView>(R.id.ip_address_text)
                .setText(address + ":" + port.toString() + "\n" + (if (ZebraPrinterUtils.connected) "PŘIPOJENO" else "NEPŘIPOJENO"))

            if (ZebraPrinterUtils.connected) {
                view.findViewById<CardView>(R.id.layout_ip_address).backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.green)
            } else {
                view.findViewById<CardView>(R.id.layout_ip_address).backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.red)

            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val code = getCode()

        view.findViewById<TextView>(R.id.code_text).text = code
        view.findViewById<TextView>(R.id.text_over_code).text = getTextOverCode()

        val address = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getString("address", null)
        val port =
            requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getString("port", "6101")?.toIntOrNull()

        val type = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("code_type", 0)

        val buttonType1 = view.findViewById<Button>(R.id.button_type1)
        buttonType1.setOnClickListener {
            showTypeChangeDialog(requireContext()){
                requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).edit() {
                    putInt("code_type", 0)
                }
                applyButtonsView(0, view)
            }

        }
        val buttonType2 = view.findViewById<Button>(R.id.button_type2)
        buttonType2.setOnClickListener {
            showTypeChangeDialog(requireContext()){
                requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).edit() {
                    putInt("code_type", 1)
                }
                applyButtonsView(1, view)
            }
        }

        applyButtonsView(type, view)




        address?.let { a ->
            port?.let { p ->
                changeStateView(view, context, address, port)
                ZebraPrinterUtils.testConnection(a, p, {
                    changeStateView(view, context, address, port)
                }, { e ->
                    changeStateView(view, context, address, port)
                })
            }
        }

        view.findViewById<ImageButton>(R.id.button_change_text).setOnClickListener {

            showTextDialog(requireContext(), { s ->

                view.findViewById<TextView>(R.id.text_over_code).text = s
                requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).edit() {
                    putString(
                        "text_over_code", s
                    )
                }
            })

        }
        view.findViewById<ImageButton>(R.id.button_change_code).setOnClickListener {

            showPrintDialog(requireContext(), { s ->

                view.findViewById<TextView>(R.id.code_text).text = s
                requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).edit() {
                    putString(
                        "code", s
                    )
                }
            })

        }

        view.findViewById<Button>(R.id.button_print_datamax).setOnClickListener {
            val c = getCode()
            val type = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("code_type", 0)

            context?.let { context ->
                val size = getDimensions(context)
                port?.let { p ->
                    address?.let { a ->
                        when (type) {
                            1 -> {
                                ZebraPrinterUtils.printDataMatrixWithText(a, p, c, getTextOverCode(), size) {
                                    Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG).show()
                                }
                            }

                            else -> {
                                ZebraPrinterUtils.printDataMatrix(a, p, c, size) {
                                    Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                    }
                }
            }


        }

        view.findViewById<Button>(R.id.button_print_10).setOnClickListener {
            val c = getCode()
            val type = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("code_type", 0)

            context?.let { context ->
                val size = getDimensions(context)
                port?.let { p ->
                    address?.let { a ->
                        when (type) {
                            1 -> {
                                ZebraPrinterUtils.printDataMatrixWithText(a, p, c, getTextOverCode(), size) {
                                    Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG).show()
                                }
                            }

                            else -> {
                                ZebraPrinterUtils.printDataMatrix(a, p, c, size) {
                                    Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                    }
                }
            }


        }

        view.findViewById<Button>(R.id.button_print_20).setOnClickListener {
            val c = getCode()
            val type = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("code_type", 0)

            context?.let { context ->
                val size = getDimensions(context)
                port?.let { p ->
                    address?.let { a ->
                        when (type) {
                            1 -> {
                                ZebraPrinterUtils.printDataMatrixWithText(a, p, c, getTextOverCode(), size) {
                                    Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG).show()
                                }
                            }

                            else -> {
                                ZebraPrinterUtils.printDataMatrix(a, p, c, size) {
                                    Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                    }
                }
            }

        }

        view.findViewById<Button>(R.id.button_print_30).setOnClickListener {
            val c = getCode()
            val type = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("code_type", 0)

            context?.let { context ->
                val size = getDimensions(context)
                port?.let { p ->
                    address?.let { a ->
                        when (type) {
                            1 -> {
                                ZebraPrinterUtils.printDataMatrixWithText(a, p, c, getTextOverCode(), size) {
                                    Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG).show()
                                }
                            }

                            else -> {
                                ZebraPrinterUtils.printDataMatrix(a, p, c, size) {
                                    Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                    }
                }
            }

        }


    }


    private fun getTextOverCode(): String {
        return requireContext().getSharedPreferences("STORAGE", Context.MODE_PRIVATE)
            .getString("text_over_code", "není nastaveno").toString()
    }

    fun applyButtonsView(type: Int, view: View = requireView()) {
        when (type) {
            1 -> {

                view.findViewById<Button>(R.id.button_type1).backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.brunswick_green)
                view.findViewById<Button>(R.id.button_type1)
                    .setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.ivory))
                view.findViewById<Button>(R.id.button_type2).backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.ivory)
                view.findViewById<Button>(R.id.button_type2)
                    .setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.brunswick_green))
                view.findViewById<CardView>(R.id.layout_text_over_code).visibility = View.VISIBLE
                view.findViewById<ImageButton>(R.id.button_change_text).visibility = View.VISIBLE
            }

            else -> {
                view.findViewById<Button>(R.id.button_type2).backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.brunswick_green)
                view.findViewById<Button>(R.id.button_type2)
                    .setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.ivory))
                view.findViewById<Button>(R.id.button_type1).backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.ivory)
                view.findViewById<Button>(R.id.button_type1)
                    .setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.brunswick_green))

                view.findViewById<CardView>(R.id.layout_text_over_code).visibility = View.GONE
                view.findViewById<ImageButton>(R.id.button_change_text).visibility = View.GONE

            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
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


}