package com.backroomscamera.mixin;

import com.backroomscamera.camera.CameraRoll;
import com.backroomscamera.camera.ClientCameraState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin на GameRenderer — применяет Z-roll (крен) камеры через матрицу.
 *
 * Оригинальная логика (Fabric):
 *
 *   @Inject(method = "renderWorld", at = @At(value = "INVOKE",
 *     target = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(...)V"))
 *   private void renderWorld(float tickDelta, long limitTime, MatrixStack matrices, CI ci) {
 *       if (shouldRenderCameraEffect()) {
 *           CutsceneManager csm = getCutsceneManager();
 *           float deg;
 *           if (csm.isPlaying()) {
 *               deg = csm.cameraRotZ;
 *           } else {
 *               deg = CameraRoll.doCameraRoll(player, tickDelta);
 *           }
 *           matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(deg));
 *       }
 *   }
 *
 * В Forge: renderLevel(float, long, PoseStack) — то же самое.
 * Момент применения — перед tiltViewWhenHurt (боб камеры).
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    private Minecraft minecraft;

    /**
     * Применяем Z-вращение (roll) в матрицу рендера мира.
     * Точка инъекции: перед вызовом bobHurt (tiltViewWhenHurt) — как в оригинале.
     */
    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"
        )
    )
    private void onRenderLevel(float partialTick, long finishNanoTime,
                               PoseStack poseStack, CallbackInfo ci) {
        if (minecraft.player == null) return;

        Player player = minecraft.player;
        CameraRoll roll = ClientCameraState.getCameraRoll();

        float deg = roll.doCameraRoll(player, partialTick);

        if (Math.abs(deg) > 0.001f) {
            // matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(deg))
            // В Forge/JOML это эквивалентно:
            poseStack.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(deg)));
        }
    }
}
