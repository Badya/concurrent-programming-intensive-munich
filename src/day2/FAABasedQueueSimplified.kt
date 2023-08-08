package day2

import day1.*
import java.util.concurrent.atomic.*
import kotlin.math.*

class FAABasedQueueSimplified<E> : Queue<E> {
    private val infiniteArray = AtomicReferenceArray<Any?>(1024) // conceptually infinite array
    private val enqIdx = AtomicLong(0)
    private val deqIdx = AtomicLong(0)

    override tailrec fun enqueue(element: E) {
        // TODO: Increment the counter atomically via Fetch-and-Add.
        // TODO: Use `getAndIncrement()` function for that.
        val i = enqIdx.getAndIncrement()

        // TODO: Atomically install the element into the cell
        // TODO: if the cell is not poisoned.
        if (infiniteArray.compareAndSet(i.toInt(), null, element)) {
            return
        } else {
            enqueue(element)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override tailrec fun dequeue(): E? {
        // Is this queue empty?
        // order matters!
        // this fails
        // if (enqIdx.get() <= deqIdx.get()) return null
        // this works
        // if (deqIdx.get() >= enqIdx.get()) return null
        if (!shouldTryToDequeue()) return null
        // TODO: Increment the counter atomically via Fetch-and-Add.
        // TODO: Use `getAndIncrement()` function for that.
        val i = deqIdx.getAndIncrement()

        // TODO: Try to retrieve an element if the cell contains an
        // TODO: element, poisoning the cell if it is empty.
        return if (infiniteArray.compareAndSet(i.toInt(), null, POISONED)) {
            dequeue()
        } else {
            val res = infiniteArray.get(i.toInt()) as E
            infiniteArray.set(i.toInt(), null)
            return res
        }
    }

    override fun validate() {
        for (i in 0 until min(deqIdx.get().toInt(), enqIdx.get().toInt())) {
            check(infiniteArray[i] == null || infiniteArray[i] == POISONED) {
                "`infiniteArray[$i]` must be `null` or `POISONED` with `deqIdx = ${deqIdx.get()}` at the end of the execution"
            }
        }
        for (i in max(deqIdx.get().toInt(), enqIdx.get().toInt()) until infiniteArray.length()) {
            check(infiniteArray[i] == null || infiniteArray[i] == POISONED) {
                "`infiniteArray[$i]` must be `null` or `POISONED` with `enqIdx = ${enqIdx.get()}` at the end of the execution"
            }
        }
    }

    // snapshot way of checking for emptiness
    // has no required order of reads
    private fun shouldTryToDequeue(): Boolean {
        while (true) {
            val curEnqIdx = enqIdx.get()
            val curDeqIdx = deqIdx.get()
            if (curEnqIdx != enqIdx.get()) continue

            // can be replaced with:
            // val curDeqIdx = deqIdx.get()
            // val curEnqIdx = enqIdx.get()
            // if (curDeqIdx != deqIdx.get()) continue

            return curDeqIdx < curEnqIdx
        }
    }
}

// TODO: poison cells with this value.
private val POISONED = Any()
