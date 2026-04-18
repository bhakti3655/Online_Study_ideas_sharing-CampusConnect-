package com.example.campusconnectadmin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.firebase.database.FirebaseDatabase

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NotificationFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        val title = view.findViewById<EditText>(R.id.etTitle)
        val msg = view.findViewById<EditText>(R.id.etMessage)
        val btn = view.findViewById<Button>(R.id.btnSend)

        btn.setOnClickListener {

            val id = FirebaseDatabase.getInstance().reference.push().key!!

            val data = mapOf(
                "title" to title.text.toString(),
                "message" to msg.text.toString()
            )

            FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(id)
                .setValue(data)
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}