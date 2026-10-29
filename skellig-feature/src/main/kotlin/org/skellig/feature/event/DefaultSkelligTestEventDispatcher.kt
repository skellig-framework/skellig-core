package org.skellig.feature.event

import kotlin.reflect.KClass

class DefaultSkelligTestEventDispatcher : SkelligTestEventDispatcher {

    protected val handlers = mutableMapOf<KClass<*>, MutableList<Any>>()

    @Synchronized
    override fun <Event : SkelligEvent> removeHandlerFor(event: KClass<Event>) {
        handlers.remove(event)
    }

    @Synchronized
    override fun <Event : SkelligEvent> dispatch(event: Event) {
        handlers[event::class]?.let {
            it.forEach { (it as (Event) -> Unit)(event) }
        }
    }

    @Synchronized
    override fun <Event : SkelligEvent> register(event: KClass<Event>, handler: (Event) -> Unit) {
        handlers.computeIfAbsent(event) { mutableListOf() }.add(handler)
    }
}