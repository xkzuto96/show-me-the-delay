package xkzuto.smth.delay.render;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import xkzuto.smth.delay.core.DelayTracker;

import java.util.List;

import xkzuto.smth.delay.core.DelayConfig;
import xkzuto.smth.delay.gui.DelayConfigScreen;
import java.util.List;

public class DelayHudRenderer implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;

        List<DelayTracker.DelayRecord> records = DelayTracker.INSTANCE.getRecentDelays();
        if (records.isEmpty()) return;

        int screenWidth = drawContext.getScaledWindowWidth();
        int screenHeight = drawContext.getScaledWindowHeight();
        
        int x = screenWidth + DelayConfig.INSTANCE.xOffset;
        int targetY = screenHeight / 2 + DelayConfig.INSTANCE.yOffset - (DelayTracker.MAX_HISTORY * 22) / 2;
        
        for (DelayTracker.DelayRecord record : records) {
            long age = System.currentTimeMillis() - record.timestamp;
            long fadeStart = DelayConfig.INSTANCE.fadeDelayMs;
            long fadeEnd = fadeStart + 1000;
            if (age > fadeEnd) continue;

            if (record.animY == -1) {
                record.animY = targetY - 22;
            }
            record.animY += (targetY - record.animY) * 0.2f;
            int renderY = (int) record.animY;

            float alpha = 1.0f;
            if (age > fadeStart) {
                alpha = 1.0f - ((age - fadeStart) / 1000.0f);
            }

            int alphaInt = (int)(DelayConfig.INSTANCE.backgroundAlpha * alpha);
            int bgColor = (alphaInt << 24) | DelayConfig.INSTANCE.backgroundColor;
            
            drawRoundedRect(drawContext, x, renderY, 110, 20, bgColor);
            drawContext.drawItem(record.item, x + 2, renderY + 2);
            
            String text = record.delayMs + " ms";
            int textColor = ((int)(255 * alpha) << 24) | 0xFFFFFF;
            drawContext.drawText(client.textRenderer, text, x + 24, renderY + 6, textColor, true);

            targetY += 22;
        }
    }

    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        int rawRadius = xkzuto.smth.delay.core.DelayConfig.INSTANCE.cornerRadius;
        int maxRadius = Math.min(width / 2, height / 2);
        int radius = Math.min(rawRadius, maxRadius);
        
        if (radius <= 0) {
            context.fill(x, y, x + width, y + height, color);
            return;
        }

        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        for (int i = 0; i < radius; i++) {
            double dy = radius - i - 0.5;
            double dx = Math.sqrt(radius * radius - dy * dy);
            int solidPixels = (int) Math.floor(dx);
            float fractionalAlpha = (float) (dx - solidPixels);

            if (solidPixels > 0) {
                context.fill(x + radius - solidPixels, y + i, x + radius, y + i + 1, color);
                context.fill(x + width - radius, y + i, x + width - radius + solidPixels, y + i + 1, color);
                context.fill(x + radius - solidPixels, y + height - 1 - i, x + radius, y + height - i, color);
                context.fill(x + width - radius, y + height - 1 - i, x + width - radius + solidPixels, y + height - i, color);
            }

            if (fractionalAlpha > 0.05f) {
                int pixelA = (int) (a * fractionalAlpha);
                int pixelColor = (pixelA << 24) | (r << 16) | (g << 8) | b;
                
                context.fill(x + radius - solidPixels - 1, y + i, x + radius - solidPixels, y + i + 1, pixelColor);
                context.fill(x + width - radius + solidPixels, y + i, x + width - radius + solidPixels + 1, y + i + 1, pixelColor);
                context.fill(x + radius - solidPixels - 1, y + height - 1 - i, x + radius - solidPixels, y + height - i, pixelColor);
                context.fill(x + width - radius + solidPixels, y + height - 1 - i, x + width - radius + solidPixels + 1, y + height - i, pixelColor);
            }
        }
    }
}
