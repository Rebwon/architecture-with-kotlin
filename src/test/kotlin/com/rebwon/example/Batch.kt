package com.rebwon.example

import java.time.LocalDateTime

class Batch(val ref: String, val sku: String, quantity: Int, val eta: LocalDateTime?) {

    var purchased_quantity = quantity
    var allocations: MutableSet<OrderLine> = mutableSetOf()

    fun allocate(line: OrderLine) {
        if (this.canAllocate(line))
            this.allocations.add(line)
    }

    fun deallocate(line: OrderLine) {
        if (line in this.allocations)
            this.allocations.remove(line)
    }

    private fun allocated_quantity(): Int {
        return Integer.sum(0, this.allocations.sumOf { line -> line.quantity })
    }

    fun available_quantity(): Int {
        return this.purchased_quantity -
                this.allocated_quantity()
    }

    fun canAllocate(line: OrderLine): Boolean {
        return this.sku == line.sku &&
                this.available_quantity() >= line.quantity
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Batch

        if (ref != other.ref) return false

        return true
    }

    override fun hashCode(): Int {
        return ref.hashCode()
    }

}