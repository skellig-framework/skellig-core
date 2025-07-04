package org.skellig.feature.event

import kotlin.reflect.KClass

//TODO: use in Skellig Extensions
interface SkelligTestEventDispatcher {
    fun <Event : SkelligEvent> dispatch(event: Event)

    fun <Event: SkelligEvent> register(event: KClass<Event>, handler: (Event) -> Unit)

    fun <Event : SkelligEvent> removeHandlerFor(event: KClass<Event>)
}