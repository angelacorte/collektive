package it.unibo.collektive.alignment

import io.kotest.assertions.throwables.shouldThrowUnit
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.unibo.collektive.Collektive.Companion.aggregate
import it.unibo.collektive.IntId
import it.unibo.collektive.aggregate.ops.neighbouring
import it.unibo.collektive.aggregate.ops.share
import it.unibo.collektive.stack.Path

class TestAlignment : StringSpec({
    "The alignment should be performed also for the same aggregate operation called multiple times (issue #51)" {
        val result = aggregate(IntId(0)) {
            neighbouring(10) // path -> [neighbouring.1] = 10
            share(0) {
                requireNotNull(neighbouring(20).localValue) // path -> [share.1, neighbouring.2] = 20
            } // path -> [sharing.1] = Field(...)
            neighbouring(30) // path -> [neighbouring.3] = 30
            5
        }

        result.result shouldBe 5
        result.toSend.messages.keys shouldHaveSize 4 // 4 paths of alignment
        result.toSend.messages.keys shouldContainAll setOf(
            Path(listOf("neighbouring.1", "exchange.1")),
            Path(listOf("share.1", "sharing.1", "exchange.1", "neighbouring.2", "exchange.1")),
            Path(listOf("share.1", "sharing.1", "exchange.1")),
            Path(listOf("neighbouring.3", "exchange.1")),
        )
    }
    "Alignment must fail clearly when entries try to override each other" {
        val exception = shouldThrowUnit<IllegalStateException> {
            aggregate(IntId(0)) {
                kotlin.repeat(2) {
                    neighbouring(0)
                }
            }
        }
        exception.message shouldContain "Alignment was broken by multiple aligned calls"
    }
})
