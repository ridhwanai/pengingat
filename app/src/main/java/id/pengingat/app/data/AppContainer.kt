package id.pengingat.app.data

import android.content.Context

/**
 * Wadah dependensi manual (tanpa framework DI).
 * Dimiliki oleh PengingatApp; semua komponen memakai instance yang sama.
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    val db: AppDatabase = AppDatabase.create(appContext)

    val settings: SettingsRepo = SettingsRepo(appContext)

    val quotes: QuoteRepository = QuoteRepository(appContext)

    val progress: ProgressRepository = ProgressRepository(db)
}
