package day1

import java.util.concurrent.atomic.AtomicReference

class TreiberStack<E> : Stack<E> {
    // Initially, the stack is empty.
    private val top = AtomicReference<Node<E>?>(null)

    override tailrec fun push(element: E) {
        // TODO: Make me linearizable!
        // TODO: Update `top` via Compare-and-Set,
        // TODO: restarting the operation on CAS failure.
        val curTop = top.get()
        val newTop = Node(element, curTop)
        if (!top.compareAndSet(curTop, newTop)) {
            push(element)
        }
    }

    override tailrec fun pop(): E? {
        // TODO: Make me linearizable!
        // TODO: Update `top` via Compare-and-Set,
        // TODO: restarting the operation on CAS failure.
        val curTop = top.get() ?: return null
        return if (top.compareAndSet(curTop, curTop.next)) {
            curTop.element
        } else {
            pop()
        }
    }

    private class Node<E>(
        val element: E,
        val next: Node<E>?
    )
}