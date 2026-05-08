package org.example.project.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.example.project.android.ScoreBuilder
import org.example.project.db.GameRepo
import org.example.project.db.GameRepoImpl
import org.example.project.db.gameDataStore
import org.example.project.utils.DeviceSignalsProvider
import org.example.project.utils.ResolveStartFlowUseCase
import org.example.project.utils.SavedScoreRouter
import org.example.project.utils.ScoreParamsCollector
import org.example.project.viewmodel.StartViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val gameModule = module {

    single<GameRepo> {
        GameRepoImpl(
            dataStore = get()
        )
    }

    single {
        ScoreBuilder()
    }

    single {
        SavedScoreRouter()
    }

    single<GameRepo> {
        GameRepoImpl(get())
    }

    single {
        DeviceSignalsProvider(
            context = androidContext()
        )
    }

    single {
        ScoreParamsCollector(
            signalsProvider = get()
        )
    }

    factory {
        ResolveStartFlowUseCase(
            gameRepo = get(),
            paramsCollector = get(),
            linkBuilder = get(),
            savedScoreRouter = get()
        )
    }

    viewModel {
        StartViewModel(
            resolveStartFlowUseCase = get()
        )
    }
}

val dataStoreModule = module {
    single<DataStore<Preferences>> {
        androidContext().gameDataStore
    }
}