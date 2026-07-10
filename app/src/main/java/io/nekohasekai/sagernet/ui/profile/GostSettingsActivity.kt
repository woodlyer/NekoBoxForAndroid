package io.nekohasekai.sagernet.ui.profile

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import io.nekohasekai.sagernet.Key
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.preference.EditTextPreferenceModifiers
import io.nekohasekai.sagernet.fmt.gost.GostBean

class GostSettingsActivity : ProfileSettingsActivity<GostBean>() {

    override fun createEntity() = GostBean()

    override fun GostBean.init() {
        DataStore.profileName = name
        DataStore.serverCustomArgs = customArgs
        DataStore.serverCustomConfigFileName = customConfigFileName
        DataStore.serverCustomConfigFileContent = customConfigFileContent
    }

    override fun GostBean.serialize() {
        name = DataStore.profileName
        customArgs = DataStore.serverCustomArgs
        customConfigFileName = DataStore.serverCustomConfigFileName
        customConfigFileContent = DataStore.serverCustomConfigFileContent
    }

    override fun PreferenceFragmentCompat.createPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.gost_preferences)

        val argsPref = findPreference<EditTextPreference>("serverCustomArgs")
        val namePref = findPreference<EditTextPreference>("serverCustomConfigFileName")
        val contentPref = findPreference<EditTextPreference>("serverCustomConfigFileContent")
        val previewPref = findPreference<androidx.preference.Preference>("gostArgsPreview")

        fun updatePreview() {
            val customArgs = argsPref?.text ?: ""
            val customFileName = namePref?.text ?: "kcp.json"
            val customContent = contentPref?.text ?: ""

            val argsList = mutableListOf<String>()
            argsList.add("-L")
            argsList.add("127.0.0.1:1080") // default placeholder port

            if (customArgs.isNotBlank()) {
                var resolvedArgs = customArgs
                if (customContent.isNotBlank()) {
                    resolvedArgs = resolvedArgs.replace(customFileName, "/data/user/0/moe.nb4a/files/$customFileName")
                }
                resolvedArgs.split("\\s+".toRegex()).forEach { if (it.isNotBlank()) argsList.add(it) }
            }
            previewPref?.summary = "gost " + argsList.joinToString(" ")
        }

        argsPref?.setOnPreferenceChangeListener { _, newValue ->
            argsPref.text = newValue as? String
            updatePreview()
            true
        }
        namePref?.setOnPreferenceChangeListener { _, newValue ->
            namePref.text = newValue as? String
            updatePreview()
            true
        }
        contentPref?.setOnPreferenceChangeListener { _, newValue ->
            contentPref.text = newValue as? String
            updatePreview()
            true
        }

        // Initial preview update
        updatePreview()
    }

    override fun finish() {
        if (DataStore.profileName == "喵要打开隐藏功能") {
            DataStore.isExpert = true
        } else if (DataStore.profileName == "喵要关闭隐藏功能") {
            DataStore.isExpert = false
        }
        super.finish()
    }

}