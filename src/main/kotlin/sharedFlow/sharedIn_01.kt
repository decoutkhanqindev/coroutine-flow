package sharedFlow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.runBlocking

// cold flow
fun getUpstreamColdFlow(): Flow<Int> = flow {
  println("getUpstreamColdFlow started collecting")
  repeat(5) {
    println("Emitting $it")
    emit(it)
    delay(1000)
  }
}.onCompletion {
  println("getUpstreamColdFlow completed collecting")
}

fun main(): Unit = runBlocking {
  val upstreamColdFlow: Flow<Int> = getUpstreamColdFlow()
  val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

//  shareIn will convert a _cold_ [Flow] into a _hot_ [SharedFlow] that is started in the given coroutine [scope],
//  sharing emissions from a single running instance of the upstream flow with multiple downstream subscribers,
//  replaying a specified number of [replay] values to new subscribers.

  val sharedFlow: SharedFlow<Int> = upstreamColdFlow.shareIn(
    scope = scope, // the coroutine scope in which sharing is started.
    started = SharingStarted.Eagerly, // the strategy that controls when sharing is started and stopped.
    // Eagerly -> Sharing is started immediately and never stops
    // in case upstreamColdFlow will share immediately without collector [1] [2] collect
    replay = 2, // the number of values replayed to new subscribers (cannot be negative, defaults to zero).
  )

  println("before delay 6s")
  delay(6000)
  println("after delay 6s")

  println("started collecting SharedFlow")

  sharedFlow
    .onEach { println("[1] collect: $it") }
    .launchIn(scope)

  sharedFlow
    .onEach { println("[2] collect: $it") }
    .launchIn(scope)

  delay(10000)
  scope.cancel()
}