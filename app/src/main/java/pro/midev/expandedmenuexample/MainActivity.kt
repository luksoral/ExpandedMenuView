package pro.midev.expandedmenuexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import pro.midev.expandedmenulibrary.ExpandedMenuClickListener
import pro.midev.expandedmenulibrary.ExpandedMenuItem

class MainActivity : AppCompatActivity(), ExpandedMenuClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expMenu.setIcons(
            ExpandedMenuItem(R.drawable.ic_home, "Home", ContextCompat.getColor(this, R.color.colorAccent)),
            ExpandedMenuItem(R.drawable.ic_qr, "QR-code"),
            ExpandedMenuItem(R.drawable.ic_main_menu, "Menu"),
            ExpandedMenuItem(R.drawable.ic_profile, "Profile")
        )

        expMenu.setOnItemClickListener(this)
    }

    override fun onItemClick(position: Int) {
        var currentString = "Nothing to show"

        when(position) {
            0 -> {
                currentString = "Home"
            }
            1 -> {
                currentString = "Qr-code"
            }
            2 -> {
                currentString = "Menu"
            }
            3 -> {
                currentString = "Profile"
            }
        }

        tvMenu.text = currentString
    }
}
