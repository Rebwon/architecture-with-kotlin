package com.rebwon.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class BatchModelTests {

    fun make_batch_and_line(sku: String, batch_qty: Int, line_qty: Int): Tuple {
        return Tuple(
            Batch("batch-001", sku, batch_qty, eta = LocalDateTime.now()),
            OrderLine("order-123", sku, line_qty)
        )
    }

    @Test
    fun allocating_to_a_batch_reduces_the_available_quantity() {
        val batch = Batch("batch-001", "SMALL-TABLE", 20, LocalDateTime.now())
        val line = OrderLine("order-ref", "SMALL-TABLE", 2)

        batch.allocate(line)

        assertThat(batch.available_quantity()).isEqualTo(18)
    }

    @Test
    fun can_allocate_if_available_greater_than_required() {
        val (large_batch, small_line) = make_batch_and_line("ELEGANT-LAMP", 20, 2)
        assertThat(large_batch.canAllocate(small_line)).isTrue()
    }

    @Test
    fun cannot_allocate_if_available_smaller_than_required() {
        val (small_batch, large_line) = make_batch_and_line("ELEGANT-LAMP", 2, 20)
        assertThat(small_batch.canAllocate(large_line)).isFalse()
    }

    @Test
    fun can_allocate_if_available_equal_to_required() {
        val (batch, line) = make_batch_and_line("ELEGANT-LAMP", 2, 2)
        assertThat(batch.canAllocate(line)).isTrue()
    }

    @Test
    fun cannot_allocate_if_skus_do_not_match() {
        val batch = Batch("batch-001", "UNCOMFORTABLE-CHAIR", 100, eta = null)
        val different_sku_line = OrderLine("order-123", "EXPENSIVE-TOASTER", 10)
        assertThat(batch.canAllocate(different_sku_line)).isFalse()
    }

    @Test
    fun can_only_deallocate_allocated_lines() {
        val (batch, unallocated_line) = make_batch_and_line("DECORATIVE-TRINKET", 20, 2)
        batch.deallocate(unallocated_line)
        assertThat(batch.available_quantity()).isEqualTo(20)
    }

    @Test
    fun allocation_is_idempotent() {
        val (batch, line) = make_batch_and_line("ANGULAR-DESK", 20, 2)
        batch.allocate(line)
        batch.allocate(line)
        assertThat(batch.available_quantity()).isEqualTo(18)
    }

}
