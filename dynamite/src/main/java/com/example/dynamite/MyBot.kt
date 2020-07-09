package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move
import com.softwire.dynamite.game.Round
import kotlin.math.max
import kotlin.math.min

class MyBot : Bot {

    override fun makeMove(gamestate: Gamestate): Move {
        // Are you debugging?
        // Put a breakpoint in this method to see when we make a move


        val rounds = gamestate.rounds

        val enemyMoveProbability = mutableMapOf(
            Move.R to 0.0,
            Move.P to 0.0,
            Move.S to 0.0,
            Move.D to 0.0,
            Move.W to 0.0
        )

        checkHistory(rounds, enemyMoveProbability)

        val mostLikely = enemyMoveProbability.maxBy{ it.value }
        val likelyEnemyMove = mostLikely?.key ?: Move.R
        val moveScore = (mostLikely?.value ?: 0.0) / enemyMoveProbability.map{ it.value }.sum()

        val counterMove = beatMap[likelyEnemyMove] ?: error("")

        val lastRoundDraw = if (rounds.isEmpty()) false else (rounds.last().p1 == rounds.last().p2)
        val randomMoveLst = mutableListOf(Move.D, counterMove)
        randomMoveLst.shuffle()

        return if (lastRoundDraw && moveScore <= 0.5)
            move(randomMoveLst[0])
        else
            move(counterMove)
    }

    init {
        // Are you debugging?
        // Put a breakpoint on the line below to see when we start a new match
        println("Started new match")
    }

    private val historyLength = 10
    private var dynamiteLeft = 100

    private val beatMap = mapOf(
        Move.R to Move.P,
        Move.P to Move.S,
        Move.S to Move.R,
        Move.D to Move.W,
        Move.W to Move.R
    )

    private fun move(m: Move) : Move {
        if (m == Move.D) {
            // safeguard against using too many dynamite
            if (dynamiteLeft <= 0)
                return Move.R
            dynamiteLeft -= 1
        }
        return m
    }

    private fun scoreHistory(prevGame: List<Round>, currentGame: List<Round>) : Int {
        // reverse the lists because if one list is not full, we want to use the end of the other
        val prevGameRev = prevGame.asReversed()
        val currentGameRev = currentGame.asReversed()

        var total = 0

        val upperBound = min(prevGameRev.size, currentGameRev.size)

        // Give precedence to recent values
        for (i in 0 until upperBound) {
            if (prevGameRev[i].p1 == currentGameRev[i].p1) total += upperBound - i
//            if (prevGameRev[i].p2 == currentGameRev[i].p2) total += upperBound - i
            // If the players draw, this could also be a part of a pattern independent of what was thrown
            if (prevGameRev[i].p1 == prevGameRev[i].p2 && currentGameRev[i].p1 == currentGameRev[i].p2) total += upperBound - i
        }

        return total
    }

    private fun checkHistory(rounds: List<Round>, outMap: MutableMap<Move, Double>) {
        var idx = rounds.size
        if (idx <= 0) return
        val currentSession = rounds.subList(max(idx - historyLength, 0), idx)
        for (i in idx-1 downTo 1) {
            val enemyMove = rounds[i].p2
            val previousSession = rounds.subList(max(i - historyLength, 0), i)
            val score = scoreHistory(previousSession, currentSession)
            outMap[enemyMove] = (outMap[enemyMove] ?: 0.0) + score
        }
    }
}