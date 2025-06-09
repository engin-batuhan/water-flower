package com.samet.waterflower.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.samet.waterflower.R
import com.samet.waterflower.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Firebase
    private lateinit var database: FirebaseDatabase
    private lateinit var moistureRef: DatabaseReference
    private lateinit var motorLogRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database    = FirebaseDatabase.getInstance()
        moistureRef = database.getReference("soil_moisture")
        motorLogRef = database.getReference("motor")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ——— 1) Toprak nemi log’larını dinle ———
        moistureRef
            .orderByKey()
            .limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { child ->
                        val map = child.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                            ?: return@forEach
                        val moistureValue = (map["moisture_value"] as? Long)?.toInt() ?: 0

                        if (moistureValue == 1) {
                            // Nem varsa → Yeşil "OK"
                            binding.textConnectionStatus.text = "OK"
                            binding.textConnectionStatus.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.green_500)
                            )
                            binding.iconConnection.setColorFilter(
                                ContextCompat.getColor(requireContext(), R.color.green_500)
                            )
                        } else {
                            // Nem yok → Kırmızı "Need Water!"
                            binding.textConnectionStatus.text = "Need Water!"
                            binding.textConnectionStatus.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.red_500)
                            )
                            binding.iconConnection.setColorFilter(
                                ContextCompat.getColor(requireContext(), R.color.red_500)
                            )
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Nem verisi okunamadı", Toast.LENGTH_SHORT).show()
                }
            })

        // ——— 2) Motor log’larını dinle ———
// ——— 2) Motor log’larını dinle ———
        motorLogRef
            .orderByKey()
            .limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { child ->
                        if (!child.hasChild("running_value")) return@forEach

                        val runningValue = child.child("running_value")
                            .getValue(Int::class.java) ?: 0

                        if (runningValue == 1) {
                            // Running: Yeşil metin
                            binding.textWateringStatus.text = "Active"
                            val green = ContextCompat.getColor(requireContext(), R.color.green_500)
                            binding.textWateringStatus.setTextColor(green)
                            binding.iconWatering.setColorFilter(green)
                        } else {
                            // Inactive: Kırmızı metin
                            binding.textWateringStatus.text = "Inactive"
                            val red = ContextCompat.getColor(requireContext(), R.color.red_500)
                            binding.textWateringStatus.setTextColor(red)
                            binding.iconWatering.setColorFilter(red)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Motor verisi okunamadı", Toast.LENGTH_SHORT).show()
                }
            })


        // ——— 3) Manuel sulama: her komutu push ile yaz ———
        binding.btnStartWatering.setOnClickListener {
            val data = mapOf<String, Any>(
                "running_value" to 1,
                "timestamp" to ServerValue.TIMESTAMP
            )
            motorLogRef.push()
                .setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Watering started", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Komut gönderilemedi", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnStopWatering.setOnClickListener {
            val data = mapOf<String, Any>(
                "running_value" to 0,
                "timestamp" to ServerValue.TIMESTAMP
            )
            motorLogRef.push()
                .setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Watering stopped", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Komut gönderilemedi", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
