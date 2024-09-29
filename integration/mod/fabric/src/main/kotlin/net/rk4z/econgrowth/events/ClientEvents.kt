package net.rk4z.econgrowth.events

import net.rk4z.beacon.CancellableEvent

class ClientLoadEvent : CancellableEvent() {
    companion object {
        private val INSTANCE = ClientLoadEvent()
        @JvmStatic
        fun get(): ClientLoadEvent {
            return INSTANCE
        }
    }
}

class ClientStartEvent : CancellableEvent() {
    companion object {
        private val INSTANCE = ClientStartEvent()
        @JvmStatic
        fun get(): ClientStartEvent {
            return INSTANCE
        }
    }
}

class ClientStopEvent : CancellableEvent() {
    companion object {
        private val INSTANCE = ClientStopEvent()
        @JvmStatic
        fun get(): ClientStopEvent {
            return INSTANCE
        }
    }
}