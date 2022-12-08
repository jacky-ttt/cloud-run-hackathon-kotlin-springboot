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

    fun isFrontAvailable(
        stateMap: Map<String, PlayerState>,
        myPlayerState: PlayerState,
        arenaX: Int,
        arenaY: Int
    ): Boolean {
        var isFrontAvailable = false
        val myLocationDirection = myPlayerState.direction
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
            isFrontAvailable = false
            return isFrontAvailable
        }
        if (frontY < 0 || frontY > arenaY - 1) {
            isFrontAvailable = false
            return isFrontAvailable
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
        return when (player.direction) {
            "N" -> Coordinate(x = player.x, y = player.y + 1)
            "E" -> Coordinate(x = player.x - 1, y = player.y)
            "S" -> Coordinate(x = player.x, y = player.y - 1)
            "W" -> Coordinate(x = player.x + 1, y = player.y)
            else -> Coordinate(x = -1, y = -1)
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
    var stateMap: Map<String, PlayerState> = mapOf("-1" to myPlayerState)
    var arenaX = 0
    var arenaY = 0
    var mySelf = ""

    @Bean
    fun routes() = router {
        GET {
            ServerResponse.ok().body(Mono.just("Let the battle begin!"))
        }

        POST("/**", accept(APPLICATION_JSON)) { request ->
            request.bodyToMono(ArenaUpdate::class.java).flatMap { arenaUpdate ->
                println(Gson().toJson(arenaUpdate))
                println(arenaUpdate)

                // update variables
                mySelf = arenaUpdate._links.self.href
                val arenaSize = arenaUpdate.arena.dims
                arenaX = arenaSize[0]
                arenaY = arenaSize[1]
                stateMap = arenaUpdate.arena.state
                myPlayerState = stateMap[mySelf]
                    ?: return@flatMap ServerResponse.ok().body(Mono.just("T"))

                println(Gson().toJson(myPlayerState))
                println(myPlayerState)
                println(arenaX)
                println(arenaY)

                highest = getHighestScorePlayerOrNull()
                    ?: return@flatMap ServerResponse.ok().body(Mono.just("T"))


                val (path, cost) = aStarSearch(
                    start = GridPosition(myPlayerState.x, myPlayerState.y),
                    finish = GridPosition(highest.x, highest.y),
                    grid = SquareGrid(width = arenaX, height = arenaY, barriers = getBarrierFromStateMap())
                )

                println("Cost: $cost  Path: $path")

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

                // find proper command
                val rotateCommand = getCommandPointingToHighestScorePlayer()

                val command = rotateCommand ?: if (hasFrontEnemy()) "T" else "F"

                return@flatMap ServerResponse.ok().body(Mono.just(command))
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

data class Links(val self: Self)
data class Self(val href: String)
data class Arena(val dims: List<Int>, val state: Map<String, PlayerState>)
