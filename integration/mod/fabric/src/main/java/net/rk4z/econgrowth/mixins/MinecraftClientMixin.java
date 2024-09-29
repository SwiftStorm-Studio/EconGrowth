package net.rk4z.econgrowth.mixins;

import kotlin.Unit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.rk4z.beacon.EventBus;
import net.rk4z.beacon.EventProcessingType;
import net.rk4z.econgrowth.events.ClientLoadEvent;
import net.rk4z.econgrowth.events.ClientStartEvent;
import net.rk4z.econgrowth.events.ClientStopEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CountDownLatch;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Unique
    private static final CountDownLatch loadCompletedLatch = new CountDownLatch(1);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackManager;scanPacks()V"))
    public void onLoad(RunArgs args, CallbackInfo ci) {
        EventBus.postWithCallback(
                ClientLoadEvent.get(),
                processedEvent -> {
                    loadCompletedLatch.countDown();
                    return Unit.INSTANCE;
                },
                0L,
                EventProcessingType.HandlerAsync.INSTANCE,
                false
        );
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;onResolutionChanged()V"))
    public void onStart(RunArgs args, CallbackInfo ci) {
        try {
            loadCompletedLatch.await();
            EventBus.postSync(ClientStartEvent.get(), false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void onStop(CallbackInfo ci) {
        EventBus.postSync(ClientStopEvent.get(), false);
    }
}
