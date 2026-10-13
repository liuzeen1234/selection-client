package com.example.selectionclient;
import com.example.selectionclient.selection.SelectionScreen;
import com.example.selectionclient.selection.SelectionRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public final class SelectionClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("selection-client");
    @Override public void onInitializeClient() {
        I18n.load();
        SelectionRenderer.register();
        KeyBinding open = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.selection-client.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.selection-client"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (open.wasPressed()) {
                if (client.world != null && client.player != null && client.currentScreen == null)
                    client.setScreen(new SelectionScreen(null));
            }
        });
    }
}
