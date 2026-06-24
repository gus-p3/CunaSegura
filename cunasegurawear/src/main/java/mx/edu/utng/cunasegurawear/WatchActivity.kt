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
        
        requestPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            WatchTheme {
                WatchNavHost(viewModel = viewModel)
            }
        }
    }
}
