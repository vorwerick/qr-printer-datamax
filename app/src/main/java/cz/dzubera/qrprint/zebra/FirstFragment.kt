package cz.dzubera.qrprint.zebra

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
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

            .setView(scrollView)
            .setPositiveButton("Uložit") { dialog, _ ->
                val code = inputCode.text.toString()
                val password = inputPassword.text.toString()
                if (password == "1239") {
                    onPrintConfirmed(code)
                } else {
                    Toast.makeText(context, "Nesprávné heslo", Toast.LENGTH_SHORT).show()
                    //VibratorUtils.vibrate(context)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Zpět") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    fun getCode(): String {
        return requireContext().getSharedPreferences("STORAGE", Context.MODE_PRIVATE)
            .getString("code", "není nastaveno").toString()
    }

    fun getDimensions(context: Context): Dimensions {
        return Dimensions(
            context.getSharedPreferences("STORAGE", Context.MODE_PRIVATE)
                .getInt("size", 10),
            context.getSharedPreferences("STORAGE", Context.MODE_PRIVATE)
                .getInt("offsetX", 80),
            context.getSharedPreferences("STORAGE", Context.MODE_PRIVATE)
                .getInt("offsetY", 80)
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

        val address = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE)
            .getString("address", null)
        val port =
            requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getString("port", "6101")
                ?.toIntOrNull()



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


        view.findViewById<Button>(R.id.button_change_code).setOnClickListener {

            showPrintDialog(requireContext(), { s ->

                view.findViewById<TextView>(R.id.code_text).text = s
                requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).edit() {
                    putString(
                        "code",
                        s
                    )
                }
            })

        }
        view.findViewById<Button>(R.id.button_print_datamax).setOnClickListener {
            val c = getCode()

            context?.let { context ->
                val size = getDimensions(context)
                port?.let { p ->
                    address?.let { a ->
                        ZebraPrinterUtils.printDataMatrix(a, p, c, size) {
                            Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            }


        }

        view.findViewById<Button>(R.id.button_print_10).setOnClickListener {
            val c = getCode()

            context?.let { context ->
                val size = getDimensions(context)
                port?.let { p ->
                    address?.let { a ->
                        ZebraPrinterUtils.printDataMatrix(a, p, c, size, 10) {
                            Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            }


        }

        view.findViewById<Button>(R.id.button_print_20).setOnClickListener {
            val c = getCode()

            context?.let { context ->
                val size = getDimensions(context)
                port?.let { p ->
                    address?.let { a ->
                        ZebraPrinterUtils.printDataMatrix(a, p, c, size, 20) {
                            Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            }

        }

        view.findViewById<Button>(R.id.button_print_30).setOnClickListener {
            val c = getCode()

            context?.let { context ->
                val size = getDimensions(context)
                port?.let { p ->
                    address?.let { a ->
                        ZebraPrinterUtils.printDataMatrix(a, p, c, size, 30) {
                            Toast.makeText(context, "Chyba připojení: $it", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
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