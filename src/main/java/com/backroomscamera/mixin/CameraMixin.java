package com.backroomscamera.mixin;

import com.backroomscamera.camera.CameraShake;
import com.backroomscamera.camera.ClientCameraState;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin на Camera.setup() — аналог CameraMixin из оригинального Fabric-мода.
 *
 * Оригинальная логика:
 *   @Inject(method = "update", at = @At("TAIL"))
 *   private void cameraShake(Area area, Entity focusedEntity, boolean thirdPerson,
 *                             boolean inverseView, float tickDelta, CallbackInfo ci)
 *   {
 *       if (SPBRevampedClient.shouldRenderCameraEffect()) {
 *           SPBRevampedClient.getCameraShake().tick(this);  // ← тик тряски
 *       }
 *   }
 *
 * В Forge: Camera.setup() принимает (BlockGetter, Entity, boolean, boolean, float).
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private float xRot;  // pitch

    @Shadow
    private float yRot;  // yaw

    /**
     * Вызывается в конце Camera.setup() — тикаем CameraShake и применяем
     * pitch + yaw офсеты от тряски.
     *
     * Оригинал применяет офсеты именно здесь, в camera.update(), потому что
     * Camera уже вычислила финальный pitch/yaw. Roll применяется отдельно
     * в GameRendererMixin через матрицу.
     */
    @Inject(
        method = "setup",
        at = @At("TAIL")
    )
    private void onCameraSetup(BlockGetter level, Entity entity,
                               boolean detached, boolean thirdPersonReverse,
                               float partialTick, CallbackInfo ci) {
        CameraShake shake = ClientCameraState.getCameraShake();

        // Тикаем тряску
        shake.tick((Camera)(Object) this);

        // Применяем pitch и yaw офсеты прямо в поля камеры
        // (точно как в оригинале — method_19330 = xRot (pitch), method_19329 = yRot (yaw))
        xRot += shake.pitchOffset;
        yRot += shake.yawOffset;
    }
}
