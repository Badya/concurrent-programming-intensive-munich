package day1

import java.util.concurrent.atomic.AtomicReference

class MSQueue<E> : Queue<E> {
    private val head: AtomicReference<Node<E>>
    private val tail: AtomicReference<Node<E>>

    init {
        val dummy = Node<E>(null)
        head = AtomicReference(dummy)
        tail = AtomicReference(dummy)
    }

    override fun enqueue(element: E) {
        while (true) {
            val newNode = Node(element)
            val tailNode = tail.get()
            if (tailNode.next.compareAndSet(null, newNode)) {
                tail.compareAndSet(tailNode, tailNode.next.get())
                return
            } else {
                tail.compareAndSet(tailNode, tailNode.next.get())
            }
        }
    }

    override fun dequeue(): E? {
        while (true) {
            val curHead = head.get()
            val nextHead = curHead.next.get() ?: return null
            if (head.compareAndSet(curHead, nextHead)) {
                return nextHead.element
            }
        }
    }

    // FOR TEST PURPOSE, DO NOT CHANGE IT.
    override fun validate() {
        check(tail.get().next.get() == null) {
            "At the end of the execution, `tail.next` must be `null`"
        }
        check(head.get().element == null) {
            "At the end of the execution, the dummy node shouldn't store an element"
        }
    }

    private class Node<E>(
        var element: E?
    ) {
        val next = AtomicReference<Node<E>?>(null)
    }
}
