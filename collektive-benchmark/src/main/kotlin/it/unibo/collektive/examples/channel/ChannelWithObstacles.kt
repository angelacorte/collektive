package it.unibo.collektive.examples.channel

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.DistanceSensor
import it.unibo.collektive.alchemist.device.sensors.LocalSensing
import it.unibo.collektive.examples.gradient.gradient

/**
 * Compute the channel between the source and the target with obstacles.
 */
context(LocalSensing, DistanceSensor)
fun Aggregate<Int>.channelWithObstacles(): Any =
    if (sense("obstacle")) {
        false
    } else {
        channel(sense("source"), sense("target"), channelWidth = 0.3)
    }

/**
 * Compute the channel between the [source] and the [target] with a specific [channelWidth].
 */
context(DistanceSensor)
fun Aggregate<Int>.channel(source: Boolean, destination: Boolean, channelWidth: Double): Boolean {
    require(channelWidth.isFinite() && channelWidth > 0)
    val toSource = gradient(source)
    val toDestination = gradient(destination)
    val sourceToDestination = broadcast(from = source, payload = toDestination)
    val channel = toSource + toDestination - sourceToDestination
    return if (channel.isFinite()) channel <= channelWidth else false
}
