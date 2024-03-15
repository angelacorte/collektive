package it.unibo.collektive.examples.channel

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.share
import it.unibo.collektive.alchemist.device.sensors.DistanceSensor
import it.unibo.collektive.field.Field.Companion.fold

/**
 * Computes the [gradientCast] from the [from] with the [payload] that is the distance from the [from] to the target.
 */
context(DistanceSensor)
fun Aggregate<Int>.broadcast(from: Boolean, payload: Double): Double = gradientCast(from, payload) { it }

/**
 * Compute the gradient of the aggregate from the [source] to the [target].
 * The [accumulate] function is used to accumulate the value of the aggregate.
 */
context(DistanceSensor)
fun Aggregate<Int>.gradientCast(source: Boolean, initial: Double, accumulate: (Double) -> Double): Double =
    share(Double.POSITIVE_INFINITY to initial) { field ->
        val dist = distances()
        when {
            source -> 0.0 to initial
            else -> {
                val resultField = dist.alignedMap(field) { distField, (currentDist, value) ->
                    distField + currentDist to accumulate(value)
                }
                resultField.fold(Double.POSITIVE_INFINITY to Double.POSITIVE_INFINITY) { acc, value ->
                    if (value.first < acc.first) value else acc
                }
            }
        }
    }.second
