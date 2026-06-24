package mx.edu.utng.cunasegurawear.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import mx.edu.utng.cunasegurawear.data.ble.BleWatchClient
import mx.edu.utng.cunasegurawear.data.datasource.WatchPreferencesDataSource
import mx.edu.utng.cunasegurawear.data.db.AppDatabase
import mx.edu.utng.cunasegurawear.data.location.WatchLocationTracker
import mx.edu.utng.cunasegurawear.data.repository.AlertRepositoryImpl
import mx.edu.utng.cunasegurawear.data.repository.ConfigRepositoryImpl
import mx.edu.utng.cunasegurawear.domain.usecase.CancelAlertUseCase
import mx.edu.utng.cunasegurawear.domain.usecase.GetSosActionsUseCase
import mx.edu.utng.cunasegurawear.domain.usecase.TriggerSosUseCase

@Suppress("UNCHECKED_CAST")
class WatchViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val bleClient    = BleWatchClient(context)
        val prefsDs      = WatchPreferencesDataSource(context)
        val alertRepo    = AlertRepositoryImpl(bleClient)
        val configRepo   = ConfigRepositoryImpl(prefsDs)
        
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val db = AppDatabase.getDatabase(context, scope)
        val dao = db.touchConfigDao()
        
        val locationTracker = WatchLocationTracker(context)

        return WatchViewModel(
            TriggerSosUseCase(alertRepo),
            CancelAlertUseCase(alertRepo),
            GetSosActionsUseCase(configRepo),
            dao,
            locationTracker,
            context
        ) as T
    }
}
