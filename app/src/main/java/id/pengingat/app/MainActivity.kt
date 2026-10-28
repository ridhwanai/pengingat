package id.pengingat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import id.pengingat.app.ui.PengingatRoot
import id.pengingat.app.ui.theme.PengingatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ekstra dari widget / notifikasi: "screen" = checkin | home
        val startScreen = intent?.getStringExtra(EXTRA_SCREEN)
        setContent {
            PengingatTheme {
                PengingatRoot(startScreen = startScreen)
            }
        }
    }

    companion object {
        const val EXTRA_SCREEN = "extra_screen"
        const val SCREEN_CHECKIN = "checkin"
        const val SCREEN_HOME = "home"
    }
}
