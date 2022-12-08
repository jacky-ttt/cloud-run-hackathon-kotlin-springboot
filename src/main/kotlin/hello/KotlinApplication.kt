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
            // println(key)
            // println(value)
            if (value.x == x && value.y == y) {
                return true
            }

        }
        return false
    }

    fun hasFrontEnemy(
        stateMap: Map<String, PlayerState>,
        myLocationDirection: String,
        myLocationX: Int,
        myLocationY: Int,
        arenaX: Int,
        arenaY: Int
    ): Boolean {
        var hasFrontEnemy = false
        when (myLocationDirection) {
            "N" -> {
                if (myLocationY == 0) {
                    return false
                }
                val minY = min(0, myLocationY - 1)
                for (y in myLocationY - 1 downTo minY) {
                    if (findEnemyAt(stateMap = stateMap, x = myLocationX, y = y)) {
                        return true
                    }
                }
            }

            "E" -> {
                if (myLocationX == arenaX - 1) {
                    return false
                }
                val maxX = max(arenaX, myLocationX + 1)
                for (x in (myLocationX + 1) until maxX) {
                    if (findEnemyAt(stateMap = stateMap, x = x, y = myLocationY)) {
                        return true
                    }
                }
            }

            "S" -> {
                if (myLocationY == arenaY - 1) {
                    return false
                }
                val maxY = max(arenaX, myLocationY + 1)
                for (y in myLocationY + 1 until maxY) {
                    if (findEnemyAt(stateMap = stateMap, x = myLocationX, y = y)) {
                        return true
                    }
                }
            }

            "W" -> {
                if (myLocationX == 0) {
                    return false
                }
                val minX = min(0, myLocationX - 1)
                for (x in (myLocationX - 1) downTo minX) {
                    if (findEnemyAt(stateMap = stateMap, x = x, y = myLocationY)) {
                        return true
                    }
                }
            }
        }
        return hasFrontEnemy
    }

    fun isFrontAvailable(
        stateMap: Map<String, PlayerState>,
        myLocationDirection: String,
        myLocationX: Int,
        myLocationY: Int,
        arenaX: Int,
        arenaY: Int
    ): Boolean {
        var isFrontAvailable = false
        var frontX = myLocationX
        var frontY = myLocationY
        when (myLocationDirection) {
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

    @Bean
    fun routes() = router {
        GET {
            ServerResponse.ok().body(Mono.just("Let the battle begin!"))
        }

        POST("/**", accept(APPLICATION_JSON)) { request ->
            request.bodyToMono(ArenaUpdate::class.java).flatMap { arenaUpdate ->
                println(Gson().toJson(arenaUpdate))
                println(arenaUpdate)

                val mySelf = arenaUpdate._links.self.href
                val arenaSize = arenaUpdate.arena.dims
                val arenaX = arenaSize[0]
                val arenaY = arenaSize[0]
                val stateMap = arenaUpdate.arena.state
                val myLocation =
                    stateMap[mySelf] ?: return@flatMap ServerResponse.ok().body(Mono.just("T"))

                println(Gson().toJson(myLocation))
                println(myLocation)

                val myLocationX = myLocation.x
                val myLocationY = myLocation.y
                val myLocationDirection = myLocation.direction
                val myLocationWasHit = myLocation.wasHit
                val myLocationScore = myLocation.score
//                {
//                    "x": 4,
//                    "direction": "N",
//                    "score": -427,
//                    "wasHit": true,
//                    "y": 11
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

                var hasFrontEnemy = hasFrontEnemy(
                    stateMap = stateMap,
                    myLocationDirection = myLocationDirection,
                    myLocationX = myLocationX,
                    myLocationY = myLocationY,
                    arenaX = arenaX,
                    arenaY = arenaY
                )
                return@flatMap if (hasFrontEnemy) {
                    ServerResponse.ok().body(Mono.just("T"))
                } else {
                    ServerResponse.ok().body(Mono.just("R"))
//                    if (isFrontAvailable(
//                            stateMap = stateMap,
//                            myLocationDirection = myLocationDirection,
//                            myLocationX = myLocationX,
//                            myLocationY = myLocationY,
//                            arenaX = arenaX,
//                            arenaY = arenaY
//                        )
//                    ) {
//                        ServerResponse.ok().body(Mono.just("F"))
//                    } else {
//                        ServerResponse.ok().body(Mono.just("R"))
//                    }
                }
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
