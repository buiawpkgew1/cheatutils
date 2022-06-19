package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraTunnelConfig;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.helpers.MixinGameRendererHelper;
import com.zergatul.cheatutils.helpers.MixinMouseHandlerHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow protected abstract Vec3 calculateViewVector(float p_20172_, float p_20173_);

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;getTeamColor()I", cancellable = true)
    private void onGetTeamColor(CallbackInfoReturnable<Integer> info) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }
        var entity = (Entity) (Object) this;
        var list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            for (EntityTracerConfig config: list) {
                if (config.enabled && config.clazz.isInstance(entity) && config.glow) {
                    info.setReturnValue(config.glowColor.getRGB());
                    info.cancel();
                    return;
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;turn(DD)V", cancellable = true)
    private void onTurn(double yRot, double xRot, CallbackInfo info) {
        if (!MixinMouseHandlerHelper.insideTurnPlayer) {
            return;
        }
        if (!FreeCamController.instance.isActive()) {
            return;
        }
        var entity = (Entity) (Object) this;
        if (entity instanceof LocalPlayer) {
            FreeCamController.instance.onMouseTurn(yRot, xRot);
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
    private void onGetEyePosition(float p_20300_, CallbackInfoReturnable<Vec3> info) {
        if (!MixinGameRendererHelper.insidePick) {
            return;
        }
        FreeCamController freecam = FreeCamController.instance;
        if (!freecam.isActive()) {
            return;
        }
        var entity = (Entity) (Object) this;
        if (entity instanceof LocalPlayer) {
            info.setReturnValue(new Vec3(freecam.getX(), freecam.getY(), freecam.getZ()));
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
    private void onGetViewVector(float p_20253_, CallbackInfoReturnable<Vec3> info) {
        if (!MixinGameRendererHelper.insidePick) {
            return;
        }
        FreeCamController freecam = FreeCamController.instance;
        if (!freecam.isActive()) {
            return;
        }
        var entity = (Entity) (Object) this;
        if (entity instanceof LocalPlayer) {
            info.setReturnValue(this.calculateViewVector(freecam.getXRot(), freecam.getYRot()));
            info.cancel();
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
    private void onCollide(Vec3 vec31, CallbackInfoReturnable<Vec3> info) {
        ElytraTunnelConfig config = ConfigStore.instance.getConfig().elytraTunnelConfig;
        if (config.enabled) {
            Entity entity = (Entity) (Object) this;
            if (entity instanceof LocalPlayer) {
                LocalPlayer player = (LocalPlayer) entity;
                if (player.isFallFlying()) {
                    //ModMain.LOGGER.info("collide: {} -> {}", vec31, info.getReturnValue());
                    Vec3 result = info.getReturnValue();
                    AABB aabb = player.getBoundingBox().expandTowards(result);
                    if (aabb.maxY > config.limit) {
                        info.setReturnValue(new Vec3(result.x, result.y - (aabb.maxY - config.limit), result.z));
                        //ModMain.LOGGER.info("collide override: {} -> {}", vec31, info.getReturnValue());
                        //ModMain.LOGGER.info("collide override: y={}, {} -> {}, maxy={}", player.getY(), vec31, info.getReturnValue(), aabb.maxY);
                    }
                }
            }
        }
    }
}
