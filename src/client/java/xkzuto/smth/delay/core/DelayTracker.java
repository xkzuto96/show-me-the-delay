package xkzuto.smth.delay.core;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import java.util.LinkedList;
import java.util.List;

public class DelayTracker {
    public static final DelayTracker INSTANCE = new DelayTracker();

    private long lastSelectionTime = -1;
    private Item lastSelectedItemType = null;
    
    private final LinkedList<DelayRecord> recentDelays = new LinkedList<>();
    public static int MAX_HISTORY = 10;

    public void updateSelection(ItemStack stack) {
        if (stack.isEmpty()) {
            lastSelectedItemType = null;
            return;
        }

        Item currentItem = stack.getItem();
        if (this.lastSelectedItemType != currentItem) {
            this.lastSelectedItemType = currentItem;
            this.lastSelectionTime = System.currentTimeMillis();
        }
    }

    public void onInteraction(ItemStack usedItem, String interactionType) {
        if (lastSelectionTime != -1 && !usedItem.isEmpty()) {
            long delay = System.currentTimeMillis() - lastSelectionTime;
            recentDelays.addFirst(new DelayRecord(usedItem.copy(), delay, interactionType));
            if (recentDelays.size() > MAX_HISTORY) {
                recentDelays.removeLast();
            }
            lastSelectionTime = System.currentTimeMillis();
        }
    }

    public List<DelayRecord> getRecentDelays() {
        return recentDelays;
    }

    public static class DelayRecord {
        public ItemStack item;
        public long delayMs;
        public long timestamp;
        public String type;
        public float animY = -1;

        public DelayRecord(ItemStack item, long delayMs, String type) {
            this.item = item;
            this.delayMs = delayMs;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
