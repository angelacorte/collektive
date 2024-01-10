package it.unibo.collektive.alignment

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import it.unibo.collektive.Collektive.Companion.aggregate
import it.unibo.collektive.IntId
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.operators.neighboring
import it.unibo.collektive.field.Field
import it.unibo.collektive.field.combine
import it.unibo.collektive.field.min
import it.unibo.collektive.field.minus
import it.unibo.collektive.field.plus
import it.unibo.collektive.network.NetworkImplTest
import it.unibo.collektive.network.NetworkManager
import it.unibo.collektive.path.Path

class BranchAlignment : StringSpec({
    val id0 = IntId(0)

    "The branch alignment should occur also for nested functions" {
        val result = aggregate(id0) {
            val condition = true
            fun test() {
                neighboring("test")
            }

            fun test2() {
                test()
            }
            if (condition) {
                test2()
            }
        }
        result.toSend.messages.keys shouldHaveSize 1 // 1 path of alignment
        result.toSend.messages.keys shouldContain Path(
            listOf(true, "test2.1", "test.1", "neighbouring.1", "exchange.1"),
        )
    }
    "The branch alignment should not occur in non aggregate context" {
        val result = aggregate(id0) {
            val condition = true
            fun test(): String = "hello"
            fun test2() {
                test()
            }
            if (condition) {
                test2()
            }
        }
        result.toSend.messages.keys shouldHaveSize 0 // 0 path of alignment
    }
    "A field should be projected when used in a body of a branch condition (issue #171)" {
        val nm = NetworkManager()
        (0..2)
            .map { IntId(it) }
            .map { NetworkImplTest(nm, it) to it }
            .map { (net, id) ->
                aggregate(id, net) {
                    val outerField = neighboring(0)
                    outerField.neighborsCount shouldBe id.id
                    if (id.id % 2 == 0) {
                        neighboring(1).neighborsCount shouldBe id.id / 2
                        neighboring(1) + outerField
                    } else {
                        neighboring(1).neighborsCount shouldBe (id.id - 1) / 2
                        neighboring(1) - outerField
                        outerField.min()
                    }
                }
            }
    }
    fun exchangeWithThreeDevices(body: Aggregate.(Field<Int>) -> Field<Int>) {
        val nm = NetworkManager()
        (0..2)
            .map { IntId(it) }
            .map { NetworkImplTest(nm, it) to it }
            .map { (net, id) ->
                aggregate(id, net) {
                    exchange(0) { body(it) }
                }
            }
    }
    "A field should be projected also when the field is referenced as lambda parameter (issue #171)" {
        exchangeWithThreeDevices {
            if ((localId as IntId).id % 2 == 0) {
                neighboring(1) + it
            } else {
                neighboring(1) - it
            }
        }
    }
    fun manuallyAlignedExchangeWithThreeDevices(pivot: (Int) -> Any?) = exchangeWithThreeDevices { field ->
        alignedOn(pivot((localId as IntId).id)) {
            neighboring(1) + field
        }
    }
    "A field should be projected whenever there is an alignment operation, not just on branches (issue #171)" {
        manuallyAlignedExchangeWithThreeDevices { it % 2 == 0 }
    }
    "A field should be projected whenever there is an alignment regardless of the type," +
        " not just booleans (issue #171)" {
            manuallyAlignedExchangeWithThreeDevices { it % 2 }
        }
    "A field should be projected when it is a non-direct receiver (issue #171)" {
        exchangeWithThreeDevices {
            with(it) {
                with((localId as IntId).id % 2 == 0) {
                    if (this) {
                        combine(neighboring(1)) { a, b -> a + b }
                    } else {
                        combine(neighboring(1)) { a, b -> a - b }
                    }
                }
            }
        }
    }
})
