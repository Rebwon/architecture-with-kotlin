package com.rebwon.example

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AllocatedTest {

    val tomorrow: LocalDateTime = LocalDateTime.now().plusDays(1)
    val later = tomorrow.plusDays(1)

    @Test
    fun prefers_current_stock_batches_to_shipments() {
        val in_stock_batch = Batch("in-stock-batch", "RETRO-CLOCK",
            100, eta = null)
        val shipment_batch = Batch("shipment-batch", "RETRO-CLOCK",
            100, eta = tomorrow)

        val line = OrderLine("oref", "RETRO-CLOCK", 10)

        allocate(line, listOf(in_stock_batch, shipment_batch))

        assertThat(in_stock_batch.available_quantity()).isEqualTo(90)
        assertThat(shipment_batch.available_quantity()).isEqualTo(100)
    }

    @Test
    fun prefers_earlier_batches() {
        val earliest = Batch("speedy-batch", "MINIMALIST-SPOON",
        100, eta = LocalDateTime.now())
        val medium = Batch("normal-batch", "MINIMALIST-SPOON",
        100, eta = tomorrow)
        val latest = Batch("slow-batch", "MINIMALIST-SPOON",
        100, eta = later)
        val line = OrderLine("order1", "MINIMALIST-SPOON", 10)

        allocate(line, listOf(earliest, medium, latest))

        assertThat(earliest.available_quantity()).isEqualTo(90)
        assertThat(medium.available_quantity()).isEqualTo(100)
        assertThat(latest.available_quantity()).isEqualTo(100)
    }

    @Test
    fun returns_allocated_batch_ref() {
        val in_stock_batch = Batch("in-stock-batch", "RETRO-CLOCK",
            100, eta = null)
        val shipment_batch = Batch("shipment-batch", "RETRO-CLOCK",
            100, eta = tomorrow)
        val line = OrderLine("oref", "RETRO-CLOCK", 10)

        val allocation = allocate(line, listOf(in_stock_batch, shipment_batch))

        assertThat(allocation).isEqualTo(in_stock_batch.ref)
    }

    @Test
    fun raises_out_of_stock_exception_if_cannot_allocate() {
        val batch = Batch("batch1", "SMALL-FORK", 10, eta = LocalDateTime.now())
        allocate(OrderLine("order1", "SMALL-FORK", 10), listOf(batch))

        assertThatExceptionOfType(OutOfStock::class.java)
            .isThrownBy {
                allocate(OrderLine("order2", "SMALL-FORK", 1), listOf(batch))
            }
    }

    class OutOfStock : Exception()

    fun allocate(line: OrderLine, batches: List<Batch>): String {
        val batch = batches
            .sortedBy { b -> b in batches }
            .find { b -> b.canAllocate(line) } ?: throw OutOfStock()
        batch.allocate(line)
        return batch.ref
    }
}