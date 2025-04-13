package net.ririfa.econgrowth.core.events

import net.ririfa.econgrowth.core.utils.ChangeInfo
import net.rk4z.beacon.Event

class DatabaseChangeEvent(
    val changeInfo: ChangeInfo
) : Event()