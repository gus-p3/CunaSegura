package mx.edu.utng.cunasegura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.edu.utng.cunasegura.presentation.navigation.NavGraph
import mx.edu.utng.cunasegura.ui.theme.CunaSeguraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CunaSeguraTheme {
                NavGraph()
            }
        }
    }
}