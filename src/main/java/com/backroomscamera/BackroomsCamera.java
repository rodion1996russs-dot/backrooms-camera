package com.backroomscamera;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("backroomscamera")
public class BackroomsCamera {

    public static final String MOD_ID = "backroomscamera";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public BackroomsCamera() {
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(this::clientSetup);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        LOGGER.info("Backrooms Camera initialized");
    }
}
