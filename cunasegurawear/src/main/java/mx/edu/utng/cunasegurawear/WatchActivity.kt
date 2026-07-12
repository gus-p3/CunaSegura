package mx.edu.utng.cunasegurawear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import mx.edu.utng.cunasegurawear.presentation.navigation.WatchNavHost
import mx.edu.utng.cunasegurawear.presentation.theme.WatchTheme
import mx.edu.utng.cunasegurawear.presentation.viewmodel.WatchViewModel
import mx.edu.utng.cunasegurawear.presentation.viewmodel.WatchViewModelFactory

class WatchActivity : ComponentActivity() {
    private val viewModel: WatchViewModel by viewModels {
        WatchViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { _ -> }
        
        val permissions = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        }
        
        requestPermissionLauncher.launch(permissions.toTypedArray())

        setContent {
            WatchTheme {
                WatchNavHost(viewModel = viewModel)
            }
        }
    }
}
