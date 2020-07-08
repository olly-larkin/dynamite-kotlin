package com.example.dynamite

import com.softwire.dynamite.runner.*

object BotRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        val results: Results = DynamiteRunner.playGames{ MyBot() }
    }
}