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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import xkzuto.smth.delay.core.DelayTracker;
import xkzuto.smth.delay.core.DelayConfig;
import xkzuto.smth.delay.render.DelayHudRenderer;
import xkzuto.smth.delay.gui.DelayConfigScreen;

public class DelayClientInit implements ClientModInitializer {

    private static KeyBinding configKeyBinding;

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

        configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.show-me-the-delay.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyBinding.Category.create(Identifier.of("show-me-the-delay", "category"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKeyBinding.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new DelayConfigScreen());
                }
            }
            
            if (client.player != null) {
                ItemStack mainHand = client.player.getMainHandStack();
                DelayTracker.INSTANCE.updateSelection(mainHand);
            }
        });

        HudRenderCallback.EVENT.register(new DelayHudRenderer());
    }
}
