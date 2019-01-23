package de.codecrops.vertretungsplangymwen.gui.customFragments


import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import de.codecrops.vertretungsplangymwen.R
import de.codecrops.vertretungsplangymwen.settings.SettingsSPAdapter

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsNotificationFragment : PreferenceFragmentCompat() {

    lateinit var master : SwitchPreference

    override fun onCreatePreferences(savedStateInstance: Bundle?, rootKey: String?) {
        //Verbinden mit SPAdapter
        preferenceManager.preferenceDataStore = SettingsSPAdapter(context!!)
        //Setzen des Inhalts
        setPreferencesFromResource(R.xml.settings_notifications, rootKey)
        //finden und speichern der Preferenzen
        //master = findPreference(resources.getString(R.string.shared_preferences_settings_background_refresh_master))
        //master = findPreference("BackgroundRefreshMaster")
        //Registrieren des onPreferenceChangeListeners
        /*
        master.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
            preferenceScreen.removePreference(master)


            true
        }*/
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        //master = findPreference("BackgroundRefreshMaster") as SwitchPreference
        //master.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, any -> true }
        super.onActivityCreated(savedInstanceState)
    }


}
