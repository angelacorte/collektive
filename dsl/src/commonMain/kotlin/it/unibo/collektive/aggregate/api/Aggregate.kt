package it.unibo.collektive.aggregate.api

import it.unibo.collektive.ID
import it.unibo.collektive.aggregate.api.YieldingContext.YieldingResult
import it.unibo.collektive.field.Field

/**
 * TODO.
 */
interface Aggregate {
    /**
     * The local [ID] of the device.
     */
    val localId: ID

    /**
     * This function computes the local value of e_i, substituting variable n with the nvalue w of
     * messages received from neighbours, using the local value of e_i ([initial]) as a default.
     * The exchange returns the neighbouring or local value v_r from the evaluation of e_r applied to the [body].
     * e_s evaluates to a nvalue w_s consisting of local values to be sent to neighbour devices δ′,
     * which will use their corresponding w_s(δ') as soon as they wake up and perform their next execution round.
     *
     * Often, expressions e_r and e_s coincide, so this function provides a shorthand for exchange(e_i, (n) => (e, e)).
     *
     * ## Example
     * ```
     * exchange(0) { f ->
     *  f.mapField { _, v -> if (v % 2 == 0) v + 1 else v * 2 }
     * }
     * ```
     * The result of the exchange function is a field with as messages a map with key the id of devices across the
     * network and the result of the computation passed as relative local values.
     */
    fun <X> exchange(initial: X, body: (Field<X>) -> Field<X>): Field<X>

    /**
     * TODO.
     */
    fun <Init, Ret> exchanging(
        initial: Init,
        body: YieldingContext<Field<Init>, Field<Ret>>.(Field<Init>) -> YieldingResult<Field<Init>, Field<Ret>>,
    ): Field<Ret>

    /**
     * Iteratively updates the value computing the [transform] expression at each device using the last
     * computed value or the [initial].
     */
    fun <Initial> repeat(initial: Initial, transform: (Initial) -> Initial): Initial

    /**
     * Iteratively updates the value computing the [transform] expression from a [YieldingContext]
     * at each device using the last computed value or the [initial].
     */
    fun <Initial, Return> repeating(
        initial: Initial,
        transform: YieldingContext<Initial, Return>.(Initial) -> YieldingResult<Initial, Return>,
    ): Return

    /**
     * Alignment function that pushes in the stack the pivot, executes the body and pop the last
     * element of the stack after it is called.
     * Returns the body's return element.
     */
    fun <R> alignedOn(pivot: Any?, body: () -> R): R
}