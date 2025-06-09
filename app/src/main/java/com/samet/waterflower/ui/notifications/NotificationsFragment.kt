package com.samet.waterflower.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.samet.waterflower.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bu fragment için layout olarak fragment_notifications.xml dosyasını kullanıyoruz
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }
}
