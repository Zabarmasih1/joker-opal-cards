package com.jokeropal.cards.core.navigation

import androidx.fragment.app.FragmentManager
import com.jokeropal.cards.R
import com.jokeropal.cards.domain.model.ChapterResult
import com.jokeropal.cards.presentation.game.GameFragment
import com.jokeropal.cards.presentation.gameover.GameOverFragment
import com.jokeropal.cards.presentation.menu.MenuFragment
import com.jokeropal.cards.presentation.splash.SplashFragment

object Navigator {

    const val TAG_GAME = "chapter_game"
    const val TAG_RESULT = "chapter_result"

    fun showSplash(manager: FragmentManager) {
        manager.beginTransaction()
            .replace(R.id.fragment_container, SplashFragment())
            .commitAllowingStateLoss()
    }

    fun toMenu(manager: FragmentManager) {
        manager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in_scale,
                R.anim.fade_out_scale,
                R.anim.fade_in_scale,
                R.anim.fade_out_scale
            )
            .replace(R.id.fragment_container, MenuFragment())
            .commitAllowingStateLoss()
    }

    fun toGame(manager: FragmentManager) {
        manager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, GameFragment())
            .addToBackStack(TAG_GAME)
            .commitAllowingStateLoss()
    }

    fun toResult(manager: FragmentManager, result: ChapterResult) {
        manager.popBackStack(TAG_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        manager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in_up,
                R.anim.fade_out_down,
                R.anim.fade_in_up,
                R.anim.fade_out_down
            )
            .replace(R.id.fragment_container, GameOverFragment.newInstance(result))
            .addToBackStack(TAG_RESULT)
            .commitAllowingStateLoss()
    }

    fun replayChapter(manager: FragmentManager) {
        manager.popBackStack(TAG_RESULT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        toGame(manager)
    }

    fun backToMenu(manager: FragmentManager) {
        manager.popBackStack(TAG_RESULT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun back(manager: FragmentManager) {
        if (manager.backStackEntryCount > 0) {
            manager.popBackStack()
        }
    }
}
