package com.backroomscamera.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Крен (roll/Z-вращение) камеры — портирован из SP-Backrooms Revamped.
 *
 * Два компонента (из оригинала):
 *  1. spinRoll   — крен от горизонтального поворота мышью (lookRollMultiplier)
 *  2. strafeRoll — крен от стрейфа (боковое движение) (strafeRollMultiplier)
 *
 * Оба плавно затухают через Lerp.
 * Итоговый roll суммируется с rollOffset из CameraShake.
 */
public class CameraRoll {

    private float prevYaw    = 0f;
    private float rotAmount  = 0f;  // накопленное вращение
    private float spinRoll   = 0f;  // roll от поворота мышью
    private float strafeRoll = 0f;  // roll от стрейфа

    // Множители (в оригинале берутся из конфига, задаём дефолты)
    private static final float LOOK_ROLL_MULTIPLIER   = 0.08f;
    private static final float STRAFE_ROLL_MULTIPLIER = 10.0f;

    /**
     * Вычисляет итоговый Z-roll в градусах.
     * Вызывается из GameRendererMixin при рендере кадра.
     *
     * @param player    текущий игрок
     * @param tickDelta частичный тик (партиал тик)
     * @return итоговый угол крена в градусах
     */
    public float doCameraRoll(Player player, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();

        // --- текущий yaw с интерполяцией ---
        float currentYaw = lerp(player.yRotO, player.getYRot(), tickDelta);

        // --- дельта yaw за этот кадр ---
        float deltaYaw = currentYaw - prevYaw;

        // Нормализуем в [-180, 180]
        while (deltaYaw >  180f) deltaYaw -= 360f;
        while (deltaYaw < -180f) deltaYaw += 360f;

        prevYaw = currentYaw;

        // --- lastFrameDuration: время прошлого кадра в секундах ---
        float lastFrameDuration = mc.getDeltaFrameTime();

        // --- spinRoll: поворот мышью → крен ---
        // Оригинал: rotAmount += deltaYaw * lookRollMultiplier
        //           spinRoll = Lerp(spinRoll, rotAmount, 0.2, lastFrameDuration)
        //           rotAmount = Lerp(rotAmount, 0, 0.1, lastFrameDuration)
        rotAmount += deltaYaw * LOOK_ROLL_MULTIPLIER;
        spinRoll   = lerp(spinRoll,  rotAmount, 0.20f, lastFrameDuration);
        rotAmount  = lerp(rotAmount, 0f,        0.10f, lastFrameDuration);

        // --- strafeRoll: стрейф → крен ---
        // Получаем 2D относительную скорость (боковая компонента относительно взгляда)
        float strafeComponent = get2DRelativeRotation(player, tickDelta);
        strafeRoll = lerp(strafeRoll, strafeComponent * STRAFE_ROLL_MULTIPLIER, 0.15f, lastFrameDuration);

        // --- суммируем с shake roll ---
        CameraShake shake = ClientCameraState.getCameraShake();
        float shakeRoll = (shake != null) ? shake.getCameraZRot() : 0f;

        return spinRoll + strafeRoll + shakeRoll;
    }

    /**
     * Вычисляет боковую (strafe) компоненту движения игрока в его системе координат.
     * Оригинал: MathStuff.get2DRelativeRotation(vec2d, degrees) → Vec2f.x (боковая ось)
     */
    private float get2DRelativeRotation(Player player, float tickDelta) {
        Vec3 vel = player.getDeltaMovement();
        if (vel.lengthSqr() < 0.0001) return 0f;

        float yawRad = (float) Math.toRadians(
            lerp(player.yRotO, player.getYRot(), tickDelta)
        );

        // Проецируем горизонтальную скорость на правый вектор игрока
        float rightX = (float)  Math.cos(yawRad);
        float rightZ = (float)  Math.sin(yawRad);

        return (float)(vel.x * rightX + vel.z * rightZ);
    }

    // Экспоненциальный lerp (MathStuff.Lerp из оригинала)
    private static float lerp(float src, float dst, float smooth, float delta) {
        return src + (dst - src) * (1f - (float) Math.pow(smooth, delta));
    }
}
