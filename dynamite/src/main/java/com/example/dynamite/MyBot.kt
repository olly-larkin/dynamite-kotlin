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

        val enemyMoveProbability1 = mutableMapOf(
            Move.R to 0.0,
            Move.P to 0.0,
            Move.S to 0.0,
            Move.D to 0.0,
            Move.W to 0.0
        )

        val enemyMoveProbability2 = mutableMapOf(
            Move.R to 0.0,
            Move.P to 0.0,
            Move.S to 0.0,
            Move.D to 0.0,
            Move.W to 0.0
        )

        checkHistory(1, rounds, enemyMoveProbability1)
        checkHistory(2, rounds, enemyMoveProbability2)

        val mostLikely1 = enemyMoveProbability1.maxBy{ it.value }
        val likelyEnemyMove1 = mostLikely1?.key ?: Move.R
        val moveScore1 = (mostLikely1?.value ?: 0.0)

        val mostLikely2 = enemyMoveProbability2.maxBy{ it.value }
        val likelyEnemyMove2 = mostLikely2?.key ?: Move.R
        val moveScore2 = (mostLikely2?.value ?: 0.0)

        val likelyEnemyMove: Move
        val moveScore: Double
        if (moveScore1 > moveScore2) {
            likelyEnemyMove = likelyEnemyMove1
            moveScore = moveScore1 / enemyMoveProbability1.map{ it.value }.sum()
        } else {
            likelyEnemyMove = likelyEnemyMove2
            moveScore = moveScore2 / enemyMoveProbability2.map{ it.value }.sum()
        }

        val counterMove = beatMap[likelyEnemyMove] ?: error("")

        val lastRoundDraw = if (rounds.isEmpty()) false else (rounds.last().p1 == rounds.last().p2)
        val randomMoveLst = mutableListOf(Move.D, counterMove)
        randomMoveLst.shuffle()

        return if (lastRoundDraw && moveScore < 0.5)
            move(randomMoveLst[0])
        else
            move(counterMove)
    }

    init {
        // Are you debugging?
        // Put a breakpoint on the line below to see when we start a new match
        println("Started new match")
    }

    private val historyLength = 2
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

    private fun scoreHistory(player: Int, prevGame: List<Round>, currentGame: List<Round>) : Int {
        // reverse the lists because if one list is not full, we want to use the end of the other
        val prevGameRev = prevGame.asReversed()
        val currentGameRev = currentGame.asReversed()

        var total = 0

        val upperBound = min(prevGameRev.size, currentGameRev.size)

        // Give precedence to recent values
        for (i in 0 until upperBound) {
            if (player == 1 && prevGameRev[i].p1 == currentGameRev[i].p1) total += historyLength - i
            if (player == 2 && prevGameRev[i].p2 == currentGameRev[i].p2) total += historyLength - i
            // If the players draw, this could also be a part of a pattern independent of what was thrown
            if (prevGameRev[i].p1 == prevGameRev[i].p2 && currentGameRev[i].p1 == currentGameRev[i].p2) total += historyLength - i
        }

        return total
    }

    private fun checkHistory(player: Int, rounds: List<Round>, outMap: MutableMap<Move, Double>) {
        val idx = rounds.size
        if (idx <= 0) return
        val currentSession = rounds.subList(max(idx - historyLength, 0), idx)
        for (i in idx-1 downTo 1) {
            val enemyMove = rounds[i].p2
            val previousSession = rounds.subList(max(i - historyLength, 0), i)
            val score = scoreHistory(player, previousSession, currentSession) * i
            outMap[enemyMove] = (outMap[enemyMove] ?: 0.0) + score
        }
    }
}