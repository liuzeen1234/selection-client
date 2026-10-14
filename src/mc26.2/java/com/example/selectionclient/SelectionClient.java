package com.example.selectionclient;
import com.example.selectionclient.selection.SelectionScreen;
import com.example.selectionclient.selection.SelectionRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public final class SelectionClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("selection-client");
    @Override public void onInitializeClient() {
        I18n.load();
        SelectionRenderer.register();
        KeyMapping open = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.selection-client.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("selection-client", "selection"))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (open.consumeClick()) {
                if (client.level != null && client.player != null && client.gui.screen() == null)
                    client.gui.setScreen(new SelectionScreen(null));
            }
        });
    }
}
