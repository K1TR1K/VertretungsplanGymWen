package de.codecrops.vertretungsplangymwen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import de.codecrops.vertretungsplangymwen.credentials.CredentialsManager
import de.codecrops.vertretungsplangymwen.network.HttpResponseCode
import de.codecrops.vertretungsplangymwen.settings.SettingsManager
import kotlinx.android.synthetic.main.activity_login.*
import java.net.HttpURLConnection

/**
 * @author K1TR1K
 * Die Start Activity. Hier gibt der Nutzer die Anmeldedaten ein, außer diese sind bereits im
 * CredentialsManager vorhanden und es wird direkt zur MainActivity gesprungen.
 */
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.setIcon(R.mipmap.ic_launcher_round)

        val cm = CredentialsManager

        //test-stuff

        SettingsManager.setNotificationSound(applicationContext, false)
        PreferenceManager.setDefaultValues(this, SettingsManager.getSettingsPath(), Context.MODE_PRIVATE, R.xml.settings_notifications, false)

        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        return

        //ende test-stuff

        if(HttpResponseCode.getResponseCode
                ("${cm.getHTTPUsername(this)}:" +
                        "${cm.getHTTPPassword(this)}") == HttpURLConnection.HTTP_OK) {
                    val intent = Intent(this, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
            cm.deleteHTTPCredentials(this)
        }

        setEnterListener()
        login.setOnClickListener { login() }
    }

    private fun login() {
        val name = username.text
        val pw = password.text
        val responseCode = HttpResponseCode.getResponseCode("$name:$pw")
        when (responseCode) {
            HttpURLConnection.HTTP_OK -> {
                //richtiges Passwort
                CredentialsManager.setHTTPCredentials(this, name.toString(), pw.toString())
                val intent = Intent(this, MainActivity::class.java)
                finish()
                startActivity(intent)
            }
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                //falsches Passwort
                val s = Snackbar.make(layout, getString(R.string.wrong_credentials), Snackbar.LENGTH_LONG)
                s.setAction("OK", View.OnClickListener { s.dismiss() })
                s.show()
            }
            else -> {
                val s = Snackbar.make(layout,
                        getString(R.string.service_not_available),
                        Snackbar.LENGTH_INDEFINITE)
                val textView = s.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
                textView.maxLines = 5
                s.setAction("OK", View.OnClickListener { s.dismiss() })
                s.show()
            }
        }
    }

    private fun setEnterListener() {
        password.setOnKeyListener { _, keyCode, event ->
            if(event.action == KeyEvent.ACTION_UP)
                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                    login()
                    true
                }
            false
        }
    }
}