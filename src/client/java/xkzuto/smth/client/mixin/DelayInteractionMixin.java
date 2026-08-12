package xkzuto.smth.client.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xkzuto.smth.delay.core.DelayTracker;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.item.ItemStack;

@Mixin(ClientPlayerInteractionManager.class)
public class DelayInteractionMixin {
    
    private ItemStack lastInteractBlockItem = ItemStack.EMPTY;
    private ItemStack lastInteractItemItem = ItemStack.EMPTY;

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void onInteractBlockHead(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        lastInteractBlockItem = player.getStackInHand(hand).copy();
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void onInteractBlockReturn(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue().isAccepted()) {
            DelayTracker.INSTANCE.onInteraction(lastInteractBlockItem, "Place Block");
        }
    }
    
    @Inject(method = "interactItem", at = @At("HEAD"))
    private void onInteractItemHead(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        lastInteractItemItem = player.getStackInHand(hand).copy();
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void onInteractItemReturn(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue().isAccepted()) {
            DelayTracker.INSTANCE.onInteraction(lastInteractItemItem, "Use Item");
        }
    }

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(PlayerEntity player, net.minecraft.entity.Entity target, CallbackInfo ci) {
        DelayTracker.INSTANCE.onInteraction(player.getMainHandStack().copy(), "Attack");
    }
}
