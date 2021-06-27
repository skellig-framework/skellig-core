package org.skellig.teststep.processor.performance

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.stream.Collectors
import kotlin.system.measureNanoTime

@ExperimentalCoroutinesApi
fun main(): Unit = runBlocking {
    var totalTime = 0L
    withContext(Default) {
        repeat(5) {
            launch {
                println(LocalDateTime.now())
                val time = measureNanoTime {
                    findPrimes(5000)
                }
                val timeMs = NANOSECONDS.toMillis(time)
                println("Took %,d ms".format(timeMs))
                totalTime += timeMs
            }
        }
    }
    println(totalTime)
}

@ExperimentalCoroutinesApi
private suspend fun findPrimes(n: Int) {
    coroutineScope {
        var ch = generate()
        repeat(n) {
            val prime = ch.receive()

            if (prime % 2 == 0 && prime != 2) {
                println("Even prime found!")
            }
            val chNext = filter(ch, prime)
            ch = chNext
        }
        coroutineContext.cancelChildren()
    }
}

@ExperimentalCoroutinesApi
fun CoroutineScope.generate(): ReceiveChannel<Int> =
    produce {
        var i = 2
        while (true) {
            send(i++)
        }
    }

@ExperimentalCoroutinesApi
fun CoroutineScope.filter(channelIn: ReceiveChannel<Int>, prime: Int): ReceiveChannel<Int> =
    produce {
        for (i in channelIn) {
            if (i % prime != 0) {
                send(i)
            }
        }
    }