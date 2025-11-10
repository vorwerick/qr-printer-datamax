package cz.dzubera.qrprint.zebra

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)



        return view
    }


    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.version).text =
            BuildConfig.VERSION_NAME + "\n" + "build " + BuildConfig.VERSION_CODE

        val type = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("code_type", 0)

        val address = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getString("address", null)
        val port =
            requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getString("port", "6101")?.toIntOrNull()

        var size = 10
        var offsetX = 80
        var offsetY = 80

        when (type) {
            1 -> {
                size = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("size1", 10)
                offsetX = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("offsetX1", 80)
                offsetY = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("offsetY1", 80)
            }

            else -> {
                size = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("size", 10)
                offsetX = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("offsetX", 80)
                offsetY = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("offsetY", 80)
            }
        }



        view.findViewById<EditText>(R.id.size).setText(size.toString())
        view.findViewById<EditText>(R.id.offsetX).setText(offsetX.toString())
        view.findViewById<EditText>(R.id.offsetY).setText(offsetY.toString())
        view.findViewById<EditText>(R.id.ip_address).setText(address)
        view.findViewById<EditText>(R.id.ip_port).setText(port.toString())



        view.findViewById<Button>(R.id.button_second).setOnClickListener {


            requireContext().getSharedPreferences("STORAGE", Context.MODE_PRIVATE).edit() {
                val type = requireContext().getSharedPreferences("STORAGE", MODE_PRIVATE).getInt("code_type", 0)

                when (type) {
                    1 -> {
                        putString(
                            "address",
                            view.findViewById<EditText>(R.id.ip_address).text.toString()
                        ).putString("port", view.findViewById<EditText>(R.id.ip_port).text.toString()).putInt(
                                "size1", view.findViewById<EditText>(R.id.size).text.toString().toInt()
                            ).putInt(
                                "offsetX1", view.findViewById<EditText>(R.id.offsetX).text.toString().toInt()
                            ).putInt(
                                "offsetY1", view.findViewById<EditText>(R.id.offsetY).text.toString().toInt()
                            )

                    }

                    else -> {
                        putString(
                            "address",
                            view.findViewById<EditText>(R.id.ip_address).text.toString()
                        ).putString("port", view.findViewById<EditText>(R.id.ip_port).text.toString()).putInt(
                                "size", view.findViewById<EditText>(R.id.size).text.toString().toInt()
                            ).putInt(
                                "offsetX", view.findViewById<EditText>(R.id.offsetX).text.toString().toInt()
                            ).putInt(
                                "offsetY", view.findViewById<EditText>(R.id.offsetY).text.toString().toInt()
                            )
                    }
                }

            }



            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            App.timeManager.countdown = 0
        }


    }


    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()

    }
}