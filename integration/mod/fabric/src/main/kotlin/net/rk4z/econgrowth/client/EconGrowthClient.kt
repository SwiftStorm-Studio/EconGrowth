package net.rk4z.econgrowth.client

import net.rk4z.beacon.IEventHandler
import net.rk4z.beacon.handler
import net.rk4z.econgrowth.events.ClientLoadEvent
import net.rk4z.econgrowth.events.ClientStartEvent
import net.rk4z.econgrowth.events.ClientStopEvent

class EconGrowthClient : IEventHandler {
    val onLoad = handler<ClientLoadEvent> {

    }

    val onStart = handler<ClientStartEvent> {

    }

    val onStop = handler<ClientStopEvent> {

    }
}