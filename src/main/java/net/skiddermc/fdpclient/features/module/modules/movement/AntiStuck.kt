/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.movement

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.*
import net.skiddermc.fdpclient.features.module.*
import net.skiddermc.fdpclient.ui.client.hud.element.elements.*
import net.skiddermc.fdpclient.utils.timer.MSTimer
import net.skiddermc.fdpclient.value.IntegerValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook

@ModuleInfo(name = "AntiStuck", category = ModuleCategory.MOVEMENT)
class AntiStuck : Module() {
    private val flagsValue = IntegerValue("Flags", 5, 1, 10)

    private val timer = MSTimer()
    private val reduceTimer = MSTimer()
    private var flagsTime = 0
    private var stuck = false

    private fun reset() {
        stuck = false
        flagsTime = 0
        timer.reset()
        reduceTimer.reset()
    }

    override fun onEnable() {
        reset()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (stuck) {
            val freeze = FDPClient.moduleManager[Freeze::class.java]!!
            freeze.state = true

            if (timer.hasTimePassed(1500)) {
                stuck = false
                flagsTime = 0
                freeze.state = false
                timer.reset()
                reduceTimer.reset()
            }
        } else {
            if (flagsTime> flagsValue.get()) {
                timer.reset()
                reduceTimer.reset()
                flagsTime = 0
                stuck = true
                FDPClient.hud.addNotification(Notification(name, "Trying to unstuck you", NotifyType.INFO, 1500))
            }
            if (timer.hasTimePassed(1500) && reduceTimer.hasTimePassed(500) && flagsTime> 0) {
                flagsTime -= 1
                reduceTimer.reset()
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            flagsTime++
            reduceTimer.reset()
            if (!stuck) {
                timer.reset()
            }
        }
        if (stuck && packet is C03PacketPlayer) {
            event.cancelEvent()
        }
    }
}