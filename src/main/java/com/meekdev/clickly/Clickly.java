package com.meekdev.clickly;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clickly implements ModInitializer {
    public static final String MOD_ID = "clickly";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = "1.0.0";

    @Override
    public void onInitialize() {
        LOGGER.info("Clickly mod initialized");
    }
}