package xkzuto.smth.delay.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import xkzuto.smth.delay.render.DelayHudRenderer;
import xkzuto.smth.delay.core.DelayConfig;
import xkzuto.smth.delay.core.DelayTracker;

public class DelayConfigScreen extends Screen {

    private boolean draggingPreview = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean lastFramePressed = false;
    
    private CustomSlider opacitySlider;
    private CustomSlider historySlider;
    private CustomSlider fadeSlider;
    private CustomSlider radiusSlider;

    public DelayConfigScreen() {
        super(Text.literal("Show Me The Delay"));
    }

    @Override
    protected void init() {
        super.init();
        
        int panelWidth = 240;
        int panelHeight = 180;
        int panelX = this.width / 2 - panelWidth / 2;
        int panelY = this.height / 2 - (panelHeight / 2);
        
        opacitySlider = new CustomSlider(panelX + 20, panelY + 40, panelWidth - 40, 16, "Opacity", DelayConfig.INSTANCE.backgroundAlpha / 255.0f);
        historySlider = new CustomSlider(panelX + 20, panelY + 75, panelWidth - 40, 16, "History Size", (DelayTracker.MAX_HISTORY - 1) / 19.0f);
        fadeSlider = new CustomSlider(panelX + 20, panelY + 110, panelWidth - 40, 16, "Fade Delay (s)", (DelayConfig.INSTANCE.fadeDelayMs - 100) / 9900.0f);
        radiusSlider = new CustomSlider(panelX + 20, panelY + 145, panelWidth - 40, 16, "Corner Radius", DelayConfig.INSTANCE.cornerRadius / 20.0f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = 240;
        int panelHeight = 180;
        int panelX = this.width / 2 - panelWidth / 2;
        int panelY = this.height / 2 - (panelHeight / 2);
        
        int panelBgColor = ((int)Math.max(0x44, DelayConfig.INSTANCE.backgroundAlpha) << 24) | 0x111111;
        DelayHudRenderer.drawRoundedRect(context, panelX, panelY, panelWidth, panelHeight, panelBgColor);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Configuration Panel"), this.width / 2, panelY + 15, 0xFFFFFFFF);
        
        boolean pressed = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
        
        opacitySlider.update(mouseX, mouseY, pressed);
        historySlider.update(mouseX, mouseY, pressed);
        fadeSlider.update(mouseX, mouseY, pressed);
        radiusSlider.update(mouseX, mouseY, pressed);
        
        opacitySlider.render(context, this);
        historySlider.render(context, this);
        fadeSlider.render(context, this);
        radiusSlider.render(context, this);
        
        DelayConfig.INSTANCE.backgroundAlpha = (int)(opacitySlider.progress * 255);
        
        int newHistory = 1 + (int)(historySlider.progress * 19);
        if (newHistory != DelayTracker.MAX_HISTORY) {
            DelayTracker.MAX_HISTORY = newHistory;
            while (DelayTracker.INSTANCE.getRecentDelays().size() > DelayTracker.MAX_HISTORY) {
                DelayTracker.INSTANCE.getRecentDelays().removeLast();
            }
        }
        
        DelayConfig.INSTANCE.fadeDelayMs = 100 + (int)(fadeSlider.progress * 9900);
        DelayConfig.INSTANCE.cornerRadius = (int)(radiusSlider.progress * 20);
        
        int totalHeight = DelayTracker.MAX_HISTORY * 22;
        int hudRepX = this.width + DelayConfig.INSTANCE.xOffset;
        int hudRepY = this.height / 2 + DelayConfig.INSTANCE.yOffset - totalHeight / 2;
        
        if (pressed && !lastFramePressed) {
            if (mouseX >= hudRepX && mouseX <= hudRepX + 110 && mouseY >= hudRepY && mouseY <= hudRepY + totalHeight) {
                draggingPreview = true;
                dragOffsetX = mouseX - hudRepX;
                dragOffsetY = mouseY - hudRepY;
            }
        }
        
        if (draggingPreview) {
            if (pressed) {
                DelayConfig.INSTANCE.xOffset = mouseX - dragOffsetX - this.width;
                DelayConfig.INSTANCE.yOffset = mouseY - dragOffsetY - this.height / 2 + totalHeight / 2;
                
                int minXOffset = -this.width;
                int maxXOffset = -110;
                int minYOffset = -this.height / 2 + totalHeight / 2;
                int maxYOffset = this.height / 2 - totalHeight / 2;
                
                DelayConfig.INSTANCE.xOffset = Math.max(minXOffset, Math.min(maxXOffset, DelayConfig.INSTANCE.xOffset));
                DelayConfig.INSTANCE.yOffset = Math.max(minYOffset, Math.min(maxYOffset, DelayConfig.INSTANCE.yOffset));
                
                hudRepX = this.width + DelayConfig.INSTANCE.xOffset;
                hudRepY = this.height / 2 + DelayConfig.INSTANCE.yOffset - totalHeight / 2;
            } else {
                draggingPreview = false;
            }
        }
        lastFramePressed = pressed;
        
        boolean hovered = mouseX >= hudRepX && mouseX <= hudRepX + 110 && mouseY >= hudRepY && mouseY <= hudRepY + totalHeight;
        
        context.fill(hudRepX - 1, hudRepY - 1, hudRepX + 111, hudRepY, 0xAAFFFFFF);
        context.fill(hudRepX - 1, hudRepY + totalHeight, hudRepX + 111, hudRepY + totalHeight + 1, 0xAAFFFFFF);
        context.fill(hudRepX - 1, hudRepY, hudRepX, hudRepY + totalHeight, 0xAAFFFFFF);
        context.fill(hudRepX + 110, hudRepY, hudRepX + 111, hudRepY + totalHeight, 0xAAFFFFFF);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        DelayConfig.save();
        super.close();
    }
    
    private static class CustomSlider {
        public int x, y, width, height;
        public String label;
        public float progress;
        public boolean dragging = false;
        
        public CustomSlider(int x, int y, int width, int height, String label, float progress) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
            this.progress = Math.max(0.0f, Math.min(1.0f, progress));
        }
        
        public void update(int mouseX, int mouseY, boolean pressed) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
            if (pressed) {
                if (hovered || dragging) {
                    dragging = true;
                    progress = (float)(mouseX - x) / width;
                    progress = Math.max(0.0f, Math.min(1.0f, progress));
                }
            } else {
                dragging = false;
            }
        }
        
        public void render(DrawContext context, Screen screen) {
            int knobX = x + (int)(progress * (width - 6));
            
            DelayHudRenderer.drawRoundedRect(context, x, y, width, height, 0x88000000);
            DelayHudRenderer.drawRoundedRect(context, knobX, y, 6, height, 0xFFFFFFFF);
            
            String val = "";
            if (label.contains("Opacity")) val = String.valueOf((int)(progress * 255));
            if (label.contains("History")) val = String.valueOf(1 + (int)(progress * 19));
            if (label.contains("Fade")) {
                float ms = 100 + (progress * 9900);
                val = String.format("%.2fs", ms / 1000.0f);
            }
            if (label.contains("Corner Radius")) val = String.valueOf((int)(progress * 20));
            
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(label + ": " + val), x + 5, y - 10, 0xFFFFFFFF, true);
        }
    }
}
