package com.backroomscamera.camera;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Хранит состояние камеры на клиентской стороне.
 * Аналог полей getCameraShake() / getCameraRoll() из SPBRevampedClient (Fabric).
 */
@OnlyIn(Dist.CLIENT)
public class ClientCameraState {

    private static final CameraShake cameraShake = new CameraShake();
    private static final CameraRoll  cameraRoll  = new CameraRoll();

    public static CameraShake getCameraShake() {
        return cameraShake;
    }

    public static CameraRoll getCameraRoll() {
        return cameraRoll;
    }
}
