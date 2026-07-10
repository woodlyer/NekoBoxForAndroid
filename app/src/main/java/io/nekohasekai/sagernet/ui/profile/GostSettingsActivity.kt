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
        DataStore.serverAddress = serverAddress
        DataStore.serverPort = serverPort
        DataStore.serverUsername = username
        DataStore.serverPassword = password
        DataStore.serverProtocol = protocol
        DataStore.serverSNI = sni
        DataStore.serverCertificates = certificates
        DataStore.serverHeaders = extraHeaders
        DataStore.serverInsecureConcurrency = insecureConcurrency
        DataStore.serverCustomArgs = customArgs
        DataStore.profileCacheStore.putBoolean("sUoT", sUoT)
    }

    override fun GostBean.serialize() {
        name = DataStore.profileName
        serverAddress = DataStore.serverAddress
        serverPort = DataStore.serverPort
        username = DataStore.serverUsername
        password = DataStore.serverPassword
        protocol = DataStore.serverProtocol
        sni = DataStore.serverSNI
        certificates = DataStore.serverCertificates
        extraHeaders = DataStore.serverHeaders.replace("\r\n", "\n")
        insecureConcurrency = DataStore.serverInsecureConcurrency
        customArgs = DataStore.serverCustomArgs
        sUoT = DataStore.profileCacheStore.getBoolean("sUoT")
    }

    override fun PreferenceFragmentCompat.createPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.gost_preferences)
        findPreference<EditTextPreference>(Key.SERVER_PORT)!!.apply {
            setOnBindEditTextListener(EditTextPreferenceModifiers.Port)
        }
        findPreference<EditTextPreference>(Key.SERVER_PASSWORD)!!.apply {
            summaryProvider = PasswordSummaryProvider
        }
        findPreference<EditTextPreference>(Key.SERVER_INSECURE_CONCURRENCY)!!.apply {
            setOnBindEditTextListener(EditTextPreferenceModifiers.Number)
        }
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