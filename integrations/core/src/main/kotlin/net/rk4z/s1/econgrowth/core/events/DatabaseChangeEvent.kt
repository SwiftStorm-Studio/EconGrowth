package net.rk4z.s1.econgrowth.core.events

import net.rk4z.beacon.Event
import net.rk4z.s1.econgrowth.core.utils.ChangeInfo

class DatabaseChangeEvent(
    val changeInfo: ChangeInfo
) : Event()