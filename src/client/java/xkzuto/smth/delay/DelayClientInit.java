package xkzuto.smth.delay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import xkzuto.smth.delay.core.DelayTracker;
import xkzuto.smth.delay.core.DelayConfig;
import xkzuto.smth.delay.render.DelayHudRenderer;
import xkzuto.smth.delay.gui.DelayConfigScreen;

public class DelayClientInit implements ClientModInitializer {

    private static boolean wasOPressed = false;

    @Override
    public void onInitializeClient() {
        DelayConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("delayconfig")
                .executes(context -> {
                    MinecraftClient.getInstance().send(() -> {
                        MinecraftClient.getInstance().setScreen(new DelayConfigScreen());
                    });
                    return 1;
                }));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() != null) {
                boolean isOPressed = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_O) == GLFW.GLFW_PRESS;
                if (isOPressed && !wasOPressed) {
                    if (client.currentScreen == null) {
                        client.setScreen(new DelayConfigScreen());
                    }
                }
                wasOPressed = isOPressed;
            }
            
            if (client.player != null) {
                ItemStack mainHand = client.player.getMainHandStack();
                DelayTracker.INSTANCE.updateSelection(mainHand);
            }
        });

        HudRenderCallback.EVENT.register(new DelayHudRenderer());
    }
}
