package net.rk4z.s1.econgrowth.paper.events

import net.rk4z.beacon.Event
import net.rk4z.s1.econgrowth.paper.utils.ChangeInfo

class DatabaseChangeEvent(
    val changeInfo: ChangeInfo
) : Event()