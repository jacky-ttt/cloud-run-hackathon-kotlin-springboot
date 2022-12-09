package hello

import com.google.gson.Gson
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import kotlin.math.*

@SpringBootApplication
class KotlinApplication {

    fun findEnemyAt(stateMap: Map<String, PlayerState>, x: Int, y: Int): Boolean {
        for ((key, value) in stateMap) {
            if (value.x == x && value.y == y) {
                return true
            }

        }
        return false
    }

    fun hasFrontEnemy(): Boolean {
        val myLocationDirection = myPlayerState.direction
        val myLocationX = myPlayerState.x
        val myLocationY = myPlayerState.y
        val detectRange = 3

        when (myLocationDirection) {
            "N" -> {
                if (myLocationY == 0) {
                    return false
                }
                val maxY = max(0, myLocationY - detectRange)
                for (y in myLocationY - 1 downTo maxY) {
                    if (findEnemyAt(stateMap = stateMap, x = myLocationX, y = y)) {
                        return true
                    }
                }
            }

            "E" -> {
                if (myLocationX == arenaX - 1) {
                    return false
                }
                val minX = min(arenaX - 1, myLocationX + detectRange)
                for (x in (myLocationX + 1)..minX) {
                    if (findEnemyAt(stateMap = stateMap, x = x, y = myLocationY)) {
                        return true
                    }
                }
            }

            "S" -> {
                if (myLocationY == arenaY - 1) {
                    return false
                }
                val minY = min(arenaY - 1, myLocationY + detectRange)
                for (y in myLocationY + 1..minY) {
                    if (findEnemyAt(stateMap = stateMap, x = myLocationX, y = y)) {
                        return true
                    }
                }
            }

            "W" -> {
                if (myLocationX == 0) {
                    return false
                }
                val maxX = max(0, myLocationX - detectRange)
                for (x in (myLocationX - 1) downTo maxX) {
                    if (findEnemyAt(stateMap = stateMap, x = x, y = myLocationY)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun isFrontAvailable(): Boolean {
        val myLocationX = myPlayerState.x
        val myLocationY = myPlayerState.y
        var frontX = myLocationX
        var frontY = myLocationY
        when (myPlayerState.direction) {
            "N" -> {
                frontX = myLocationX
                frontY = myLocationY - 1
            }

            "E" -> {
                frontX = myLocationX + 1
                frontY = myLocationY
            }

            "S" -> {
                frontX = myLocationX
                frontY = myLocationY + 1
            }

            "W" -> {
                frontX = myLocationX - 1
                frontY = myLocationY
            }
        }
        if (frontX < 0 || frontX > arenaX - 1) {
            return false
        }
        if (frontY < 0 || frontY > arenaY - 1) {
            return false
        }
        return !findEnemyAt(stateMap = stateMap, x = frontX, y = frontY)
    }

//    fun isWorthTheEffort(myPlayerState: PlayerState, state: Map<String, PlayerState>): Boolean {
//        val highest = getHighestScorePlayerOrNull(state) ?: return false
//
//        val scoreDiff = myPlayerState.score - (highest.score ?: 0)
//        if (findShortestMoveToHighestScorePlayer() >= scoreDiff)
//            return false
//
//        return true
//    }

    fun getHighestScorePlayerOrNull(): PlayerState? {
        return stateMap.maxByOrNull { (k, v) ->
            v.score
        }?.value
    }

    fun getLowestScorePlayerOrNull(): PlayerState? {
        return stateMap.minByOrNull { (k, v) ->
            v.score
        }?.value
    }

//    fun findShortestMoveToHighestScorePlayer(state: Map<String, PlayerState>): Int {
//        val highest = getHighestScorePlayerOrNull(state) ?: return 999
//
//        val (buttX, buttY) = when (highest.direction) {
//            "N" -> {
//                Coordinate(highest.x, highest.y + 1)
//            }
//
//            "E" -> {
//                Coordinate(highest.x - 1, highest.y)
//            }
//
//            "S" -> {
//                Coordinate(highest.x, highest.y - 1)
//            }
//
//            "W" -> {
//                Coordinate(highest.x + 1, highest.y)
//            }
//
//            else -> {
//                Coordinate(-1, -1)
//            }
//        }
//
//
//
//        return 0
//    }

    data class Coordinate(val x: Int, val y: Int)

    fun getButtOfPlayer(player: PlayerState): Coordinate {
        // N->E->S->W
        // val delta = listOf(Pair(0, 1), Pair(-1, 0), Pair(0, -1), Pair(1, 0))
        return when (player.direction) {
            "N" -> Coordinate(x = player.x, y = player.y + 1)
            "E" -> Coordinate(x = player.x - 1, y = player.y)
            "S" -> Coordinate(x = player.x, y = player.y - 1)
            "W" -> Coordinate(x = player.x + 1, y = player.y)
            else -> Coordinate(x = -1, y = -1)
        }
    }

    fun getRightOfPlayer(player: PlayerState): Coordinate {
        return when (player.direction) {
            "N" -> Coordinate(x = player.x + 1, y = player.y)
            "E" -> Coordinate(x = player.x, y = player.y + 1)
            "S" -> Coordinate(x = player.x - 1, y = player.y)
            "W" -> Coordinate(x = player.x, y = player.y - 1)
            else -> Coordinate(x = -1, y = -1)
        }
    }

    fun getLeftOfPlayer(player: PlayerState): Coordinate {
        return when (player.direction) {
            "N" -> Coordinate(x = player.x - 1, y = player.y)
            "E" -> Coordinate(x = player.x, y = player.y - 1)
            "S" -> Coordinate(x = player.x + 1, y = player.y)
            "W" -> Coordinate(x = player.x, y = player.y + 1)
            else -> Coordinate(x = -1, y = -1)
        }
    }

    fun getFrontOfPlayer(player: PlayerState): Coordinate {
        return when (player.direction) {
            "N" -> Coordinate(x = player.x, y = player.y - 1)
            "E" -> Coordinate(x = player.x + 1, y = player.y)
            "S" -> Coordinate(x = player.x, y = player.y + 1)
            "W" -> Coordinate(x = player.x - 1, y = player.y)
            else -> Coordinate(x = -1, y = -1)
        }
    }

    fun isValidCoordinate(coordinate: Coordinate): Boolean {
        return (coordinate.x in 1 until arenaX && coordinate.y in 1 until arenaY)
    }

    fun getButtOrNextBestOfPlayer(player: PlayerState): Coordinate {
        // behind > right > left > front
        val butt = getButtOfPlayer(player)
        val (buttX, buttY) = butt
        val (buttPath, buttCost) = aStarSearch(
            start = GridPosition(myPlayerState.x, myPlayerState.y),
            finish = GridPosition(buttX, buttY),
            grid = SquareGrid(width = arenaX, height = arenaY, barriers = getBarrierFromStateMap())
        )

        val right = getRightOfPlayer(player)
        val (rightX, rightY) = right
        val (rightPath, rightCost) = aStarSearch(
            start = GridPosition(myPlayerState.x, myPlayerState.y),
            finish = GridPosition(rightX, rightY),
            grid = SquareGrid(width = arenaX, height = arenaY, barriers = getBarrierFromStateMap())
        )

        val left = getLeftOfPlayer(player)
        val (leftX, leftY) = left
        val (leftPath, leftCost) = aStarSearch(
            start = GridPosition(myPlayerState.x, myPlayerState.y),
            finish = GridPosition(leftX, leftY),
            grid = SquareGrid(width = arenaX, height = arenaY, barriers = getBarrierFromStateMap())
        )

        return if (buttCost != Int.MAX_VALUE && buttCost <= rightCost) {
            // choose butt
            Coordinate(buttX, buttY)
        } else if (rightCost != Int.MAX_VALUE && rightCost <= leftCost) {
            // choose right
            Coordinate(rightX, rightY)
        } else if (leftCost != Int.MAX_VALUE) {
            // choose left
            Coordinate(leftX, leftY)
        } else {
            // choose front
            val (frontX, frontY) = getFrontOfPlayer(player)
            Coordinate(frontX, frontY)
        }
    }

    fun getCommandPointingToHighestScorePlayer(): String? {
        val (buttX, buttY) = getButtOfPlayer(highest)

        val rotateCommand: String? = if (buttX < myPlayerState.x) {
            // go left, L + F
            when (myPlayerState.direction) {
                "N" -> "L"
                "E" -> "R"
                "S" -> "R"
                "W" -> null
                else -> null
            }
        } else if (buttX > myPlayerState.x) {
            // go right, R + F
            when (myPlayerState.direction) {
                "N" -> "R"
                "E" -> null
                "S" -> "L"
                "W" -> "R"
                else -> null
            }

        } else if (buttY < myPlayerState.y) {
            // go top, F

            when (myPlayerState.direction) {
                "N" -> null
                "E" -> "L"
                "S" -> "R"
                "W" -> "R"
                else -> null
            }

        } else if (buttY > myPlayerState.y) {
            // go bottom
            when (myPlayerState.direction) {
                "N" -> "R"
                "E" -> "R"
                "S" -> null
                "W" -> "L"
                else -> null
            }

        } else {
            // do nothing, boc x==y
            null
        }

        return rotateCommand
    }

    fun getBarrierFromStateMap(): List<Barrier> {
        val barriers = listOf(
            setOf(
                Pair(2, 4), Pair(2, 5), Pair(2, 6), Pair(3, 6), Pair(4, 6), Pair(5, 6), Pair(5, 5),
                Pair(5, 4), Pair(5, 3), Pair(5, 2), Pair(4, 2), Pair(3, 2)
            )
        )

        val pointSet = stateMap.filter { (k, v) ->
            k != mySelf
        }.map { (k, v) ->
            Pair(v.x, v.y)
        }.toSet()

        return listOf(pointSet)
    }

    fun getBarrierFromStateMapWithFireRange(): List<Barrier> {
        val tempSet = mutableSetOf<Pair<Int, Int>>()
        val detectRange = 3
        stateMap.filter { (k, v) ->
            k != mySelf
        }.forEach { (k, v) ->
            when (v.direction) {
                "N" -> {
                    for (y in v.y - 1 downTo v.y - detectRange) {
                        if (isValidCoordinate(Coordinate(v.x, y))) {
                            tempSet.add(Pair(v.x, y))
                        }
                    }
                }

                "E" -> {
                    for (x in (v.x + 1)..v.x + detectRange) {
                        if (isValidCoordinate(Coordinate(x, v.y))) {
                            tempSet.add(Pair(x, v.y))
                        }
                    }
                }

                "S" -> {
                    for (y in v.y + 1..v.y + detectRange) {
                        if (isValidCoordinate(Coordinate(v.x, y))) {
                            tempSet.add(Pair(v.x, y))
                        }
                    }
                }

                "W" -> {
                    for (x in (v.x - 1) downTo v.x - detectRange) {
                        if (isValidCoordinate(Coordinate(x, v.y))) {
                            tempSet.add(Pair(x, v.y))
                        }
                    }
                }
            }
            tempSet.add(Pair(v.x, v.y))
        }
        println("getBarrierFromStateMapWithFireRange: ${Gson().toJson(tempSet)}")
        return listOf(tempSet.toSet())
    }

    fun getRotateCommandPointingToTargetPlayer(position: GridPosition): String? {
        val (targetX, targetY) = position

        val rotateCommand: String? = if (targetX < myPlayerState.x) {
            // go left, L + F
            when (myPlayerState.direction) {
                "N" -> "L"
                "E" -> "R"
                "S" -> "R"
                "W" -> null
                else -> null
            }
        } else if (targetX > myPlayerState.x) {
            // go right, R + F
            when (myPlayerState.direction) {
                "N" -> "R"
                "E" -> null
                "S" -> "L"
                "W" -> "R"
                else -> null
            }

        } else if (targetY < myPlayerState.y) {
            // go top, F

            when (myPlayerState.direction) {
                "N" -> null
                "E" -> "L"
                "S" -> "R"
                "W" -> "R"
                else -> null
            }

        } else if (targetY > myPlayerState.y) {
            // go bottom
            when (myPlayerState.direction) {
                "N" -> "R"
                "E" -> "R"
                "S" -> null
                "W" -> "L"
                else -> null
            }

        } else {
            // do nothing, boc x==y
            null
        }

        return rotateCommand
    }

    fun getClosestPlayer(): GridPosition {
        var closest = stateMap.toList().filter { (k, v) ->
            k != mySelf
        }.minByOrNull { (k, v) ->
            (sqrt(
                ((v.x - myPlayerState.x) * (v.x - myPlayerState.x) +
                        (v.y - myPlayerState.y) * (v.y - myPlayerState.y)).toDouble()
            ))
        }?.second
        closest = closest ?: myPlayerState
        return Pair(closest.x, closest.y)
    }

    fun getClosestNonMovingPlayer(): GridPosition {
        val stayCoefficient = 1
        val scoreCoefficient = 1
        var closest = stayStateMap.toList().filter { (k, v) ->
            k != mySelf
        }.minByOrNull { (k, v) ->
            (sqrt(
                ((v.x - myPlayerState.x) * (v.x - myPlayerState.x) +
                        (v.y - myPlayerState.y) * (v.y - myPlayerState.y)).toDouble()
            )) - v.stay * stayCoefficient + v.score * scoreCoefficient
        }?.second
        closest = closest ?: dummyPlayerStateWithStay
        println("stayStateMap: ${Gson().toJson(stayStateMap)}")
        println("closest: ${closest.x}, ${closest.y}")
        return Pair(closest.x, closest.y)
    }

    fun getClosestAvailableSpace(): GridPosition {
        val barrier: Barrier = getBarrierFromStateMapWithFireRange().firstOrNull()
            ?: setOf(Pair(myPlayerState.x, myPlayerState.y))

        val availableSpace = mutableSetOf<Pair<Int, Int>>()
        for (x in 0 until arenaX) {
            for (y in 0 until arenaY) {
                if (!barrier.any { it.first == x && it.second == y }) {
                    availableSpace.add(Pair(x, y))
                }
            }
        }

        val closest = availableSpace.toList().minByOrNull { (x, y) ->
            (sqrt(
                ((x - myPlayerState.x) * (x - myPlayerState.x) +
                        (y - myPlayerState.y) * (y - myPlayerState.y)).toDouble()
            ))
        }

        return closest ?: Pair(myPlayerState.x, myPlayerState.y)
    }

    var myPlayerState: PlayerState = PlayerState(
        x = -1,
        y = -1,
        direction = "N",
        score = -1,
        wasHit = false
    )
    var highest: PlayerState = PlayerState(
        x = -1,
        y = -1,
        direction = "N",
        score = -1,
        wasHit = false
    )
    var lowest: PlayerState = PlayerState(
        x = -1,
        y = -1,
        direction = "N",
        score = -1,
        wasHit = false
    )
    var stateMap: Map<String, PlayerState> = mapOf("-1" to myPlayerState)
    var arenaX = 0
    var arenaY = 0
    var mySelf = ""
    val dummyPlayerStateWithStay = PlayerStateWithStay(
        x = -1,
        y = -1,
        direction = "N",
        score = -1,
        wasHit = false,
        stay = 0
    )

    var stayStateMap: Map<String, PlayerStateWithStay> = mapOf("-1" to dummyPlayerStateWithStay)

    fun updateStayStateMap(newStateMap: Map<String, PlayerState>): Map<String, PlayerStateWithStay> {
        val oldStateMap = stayStateMap.toMap()
        return newStateMap.mapValues { (k, v) ->
            var tempStay = 0

            oldStateMap[k]?.let {
                if (v.x == it.x && v.y == it.y) {
                    tempStay = it.stay + 1
                }
            }

            PlayerStateWithStay(
                x = v.x,
                y = v.y,
                direction = v.direction,
                score = v.score,
                wasHit = v.wasHit,
                stay = tempStay
            )
        }
    }

    fun runClosestNonMovingPlayerWithFireRange(): String? {
        val (closestX, closestY) = getClosestNonMovingPlayer()
        val (path, cost) = aStarSearch(
            start = GridPosition(myPlayerState.x, myPlayerState.y),
            finish = GridPosition(closestX, closestY),
            grid = SquareGrid(width = arenaX, height = arenaY, barriers = getBarrierFromStateMapWithFireRange())
        )
        println("Cost: $cost  Path: $path")


        if (path.isNotEmpty() && path.size >= 2 && cost in 1 until Int.MAX_VALUE) {
            val nextPosition = path[1]
            val rotateCommand = getRotateCommandPointingToTargetPlayer(nextPosition)

            return rotateCommand ?: "F"
        }
        return null
    }

    fun runClosestNonMovingPlayer(): String? {
        val (closestX, closestY) = getClosestNonMovingPlayer()
        val (path, cost) = aStarSearch(
            start = GridPosition(myPlayerState.x, myPlayerState.y),
            finish = GridPosition(closestX, closestY),
            grid = SquareGrid(width = arenaX, height = arenaY, barriers = getBarrierFromStateMap())
        )
        println("Cost: $cost  Path: $path")


        if (path.isNotEmpty() && path.size >= 2 && cost in 1 until Int.MAX_VALUE) {
            val nextPosition = path[1]
            val rotateCommand = getRotateCommandPointingToTargetPlayer(nextPosition)

            return rotateCommand ?: "F"
        }
        return null
    }

    @Bean
    fun routes() = router {
        GET {
            ServerResponse.ok().body(Mono.just("Let the battle begin!"))
        }

        POST("/**", accept(APPLICATION_JSON)) { request ->
            request.bodyToMono(ArenaUpdate::class.java).flatMap { arenaUpdate ->
                println("arenaUpdate: ${Gson().toJson(arenaUpdate)}")
//                println(arenaUpdate)

                // update variables---------------------------------------------------------------
                mySelf = arenaUpdate._links.self.href
                val arenaSize = arenaUpdate.arena.dims
                arenaX = arenaSize[0]
                arenaY = arenaSize[1]
                stayStateMap = updateStayStateMap(newStateMap = arenaUpdate.arena.state)
                stateMap = arenaUpdate.arena.state
                myPlayerState = stateMap[mySelf]
                    ?: return@flatMap ServerResponse.ok().body(Mono.just("T"))

                println("stayStateMap: ${Gson().toJson(stayStateMap)}")
                println("myPlayerState: ${Gson().toJson(myPlayerState)}")
//                println(Gson().toJson(myPlayerState))
//                println(myPlayerState)
                println("arena: $arenaX, $arenaY")

                highest = getHighestScorePlayerOrNull()
                    ?: return@flatMap ServerResponse.ok().body(Mono.just("T"))
                println("highest: ${highest.direction},${highest.x}, ${highest.y}")
                lowest = getLowestScorePlayerOrNull()
                    ?: return@flatMap ServerResponse.ok().body(Mono.just("T"))
                println("lowest: ${lowest.direction},${lowest.x}, ${lowest.y}")


                if (myPlayerState.wasHit) {
                    if (isFrontAvailable()) {
                        return@flatMap ServerResponse.ok().body(Mono.just("F"))
                    } else {
                        return@flatMap ServerResponse.ok()
                            .body(Mono.just(listOfNotNull("R", "L", if (hasFrontEnemy()) "T" else null).random()))
                    }

//                    val closest = getClosestPlayer()
//                    val rotateCommand = getRotateCommandPointingToTargetPlayer(closest)
//                    val defaultCommand = rotateCommand ?: "F"
//                    return@flatMap ServerResponse.ok()
//                        .body(Mono.just(listOfNotNull(defaultCommand, if (hasFrontEnemy()) "T" else null).random()))
                }

                if (hasFrontEnemy()) {
                    println("hasFrontEnemy true")
                    return@flatMap ServerResponse.ok().body(Mono.just("T"))
                }

                // find the least effort move, calculate coefficient=move cost + not moving value + score
                val command = runClosestNonMovingPlayerWithFireRange() ?: runClosestNonMovingPlayer()
                if (command != null) {
                    return@flatMap ServerResponse.ok().body(Mono.just(command))
                }

                // can be trapped, find the closest target
                val closest = getClosestPlayer()
                val rotateCommand = getRotateCommandPointingToTargetPlayer(closest)
                val defaultCommand = rotateCommand ?: "F"
                return@flatMap ServerResponse.ok()
                    .body(Mono.just(listOfNotNull(defaultCommand, if (hasFrontEnemy()) "T" else null).random()))

//                // find proper command---------------------------------------------------------------
//                val (buttOrBestX, buttOrBestY) = getButtOrNextBestOfPlayer(lowest)
//                println("butt: $buttOrBestX, $buttOrBestY")
//
//                val (path, cost) = aStarSearch(
//                    start = GridPosition(myPlayerState.x, myPlayerState.y),
//                    finish = GridPosition(buttOrBestX, buttOrBestY),
//                    grid = SquareGrid(width = arenaX, height = arenaY, barriers = getBarrierFromStateMap())
//                )
//
//                println("Cost: $cost  Path: $path")
//                // Cost: 14  Path: [(0, 0), (1, 0), (1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1), (6, 2), (6, 3), (6, 4), (6, 5), (6, 6), (7, 6), (7, 7)]
//
//                if (path.isNotEmpty() && path.size == 1 && cost == 0) {
//                    // reach target
//                    val rotateCommand = getRotateCommandPointingToTargetPlayer(Pair(highest.x, highest.y))
//
//                    val command = rotateCommand ?: "T"
//                    return@flatMap ServerResponse.ok().body(Mono.just(command))
//                } else if (path.isNotEmpty() && path.size >= 2 && cost in 1 until Int.MAX_VALUE) {
//                    val nextPosition = path[1]
//                    val rotateCommand = getRotateCommandPointingToTargetPlayer(nextPosition)
//
//                    val command = rotateCommand ?: "F"
//                    return@flatMap ServerResponse.ok().body(Mono.just(command))
//                } else {
//                    // cannot find path to highest, find the closest target
//                    val hasFrontEnemy = hasFrontEnemy()
//                    println("hasFrontEnemy $hasFrontEnemy")
//                    return@flatMap if (hasFrontEnemy) {
//                        ServerResponse.ok().body(Mono.just("T"))
//                    } else {
//                        // find the closest target
//                        val closest = getClosestPlayer()
//                        val rotateCommand = getRotateCommandPointingToTargetPlayer(closest)
//
//                        val command = rotateCommand ?: "F"
//                        return@flatMap ServerResponse.ok().body(Mono.just(command))
//                    }
//                }

//                if (myLocationWasHit) {
//                    // move to available space
//                    if (isFrontAvailable(
//                            stateMap = stateMap,
//                            myLocationDirection = myLocationDirection,
//                            myLocationX = myLocationX,
//                            myLocationY = myLocationY,
//                            arenaX = arenaX,
//                            arenaY = arenaY
//                        )
//                    ) {
//                        return@flatMap ServerResponse.ok().body(Mono.just("F"))
//                    } else {
//                        return@flatMap ServerResponse.ok().body(Mono.just("R"))
//                    }
//                }

//                var hasFrontEnemy = hasFrontEnemy(
//                    stateMap = stateMap,
//                    myPlayerState = myPlayerState,
//                    arenaX = arenaX,
//                    arenaY = arenaY
//                )
//                println("hasFrontEnemy $hasFrontEnemy")
//                return@flatMap if (hasFrontEnemy) {
//                    ServerResponse.ok().body(Mono.just("T"))
//                } else {
//                    ServerResponse.ok().body(Mono.just("R"))
////                    if (isFrontAvailable(
////                            stateMap = stateMap,
////                            myPlayerState = myPlayerState,
////                            arenaX = arenaX,
////                            arenaY = arenaY
////                        )
////                    ) {
////                        ServerResponse.ok().body(Mono.just("F"))
////                    } else {
////                        ServerResponse.ok().body(Mono.just("R"))
////                    }
//                }

//                // find proper command
//                val rotateCommand = getCommandPointingToHighestScorePlayer()
//
//                val command = rotateCommand ?: if (hasFrontEnemy()) "T" else "F"
//
//                return@flatMap ServerResponse.ok().body(Mono.just(command))
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<KotlinApplication>(*args)
}

data class ArenaUpdate(val _links: Links, val arena: Arena)
data class PlayerState(
    val x: Int,
    val y: Int,
    val direction: String,
    val score: Int,
    val wasHit: Boolean
)

data class PlayerStateWithStay(
    val x: Int,
    val y: Int,
    val direction: String,
    val score: Int,
    val wasHit: Boolean,
    val stay: Int
)

data class Links(val self: Self)
data class Self(val href: String)
data class Arena(val dims: List<Int>, val state: Map<String, PlayerState>)
