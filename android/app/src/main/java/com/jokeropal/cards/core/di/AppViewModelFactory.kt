package com.jokeropal.cards.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jokeropal.cards.presentation.game.GameViewModel
import com.jokeropal.cards.presentation.gameover.GameOverViewModel
import com.jokeropal.cards.presentation.menu.MenuViewModel
import com.jokeropal.cards.presentation.splash.SplashViewModel

class AppViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext: Context = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        ServiceLocator.init(appContext)
        return when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) ->
                SplashViewModel() as T

            modelClass.isAssignableFrom(MenuViewModel::class.java) ->
                MenuViewModel(
                    ServiceLocator.progress(appContext),
                    ServiceLocator.chapters(appContext)
                ) as T

            modelClass.isAssignableFrom(GameViewModel::class.java) ->
                GameViewModel(
                    ServiceLocator.chapters(appContext),
                    ServiceLocator.progress(appContext),
                    ServiceLocator.settings(appContext),
                    ServiceLocator.generateBoard(),
                    ServiceLocator.resolveSwap(),
                    ServiceLocator.applyJoker(),
                    ServiceLocator.evaluateChapter(),
                    ServiceLocator.saveChapterResult(appContext)
                ) as T

            modelClass.isAssignableFrom(GameOverViewModel::class.java) ->
                GameOverViewModel(ServiceLocator.progress(appContext)) as T

            else -> throw IllegalArgumentException(modelClass.name)
        }
    }
}
