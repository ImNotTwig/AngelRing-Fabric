package twig.twigangelring.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import twig.twigangelring.DoubleJumpState;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;

@Mixin(ClientPlayerEntity.class)
public class AngelringClientMixin {

  private DoubleJumpState isDoubleJump = DoubleJumpState.INVALID;

  private static long lastJumpTime = System.currentTimeMillis();
  private static long currentJumpTime = 0;
  private static long jumpDiff = currentJumpTime - lastJumpTime;
  private static final long DOUBLE_JUMP_THRESHOLD = 750;
  private static final long HOLD_THRESHOLD = 75;

  @Inject(at = @At("HEAD"), method = "tick")
  private void onTick(CallbackInfo info) {
    MinecraftClient client = MinecraftClient.getInstance();
    ClientPlayerEntity player = client.player;

    KeyBinding jumpKey = client.options.jumpKey;

    if (TrinketsApi.getTrinketComponent(player) != null) {
      TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).get();

      if (player.isOnGround()) {
        twig.twigangelring.Angelring.canFly = false;
      }

      if (trinket.isEquipped(twig.twigangelring.AngelringItem.angelRing)) {

        if (jumpKey.isPressed()) {
          currentJumpTime = System.currentTimeMillis();

          jumpDiff = currentJumpTime - lastJumpTime;
          lastJumpTime = currentJumpTime;

          if (jumpDiff > HOLD_THRESHOLD && jumpDiff < DOUBLE_JUMP_THRESHOLD) {
            if (player.isOnGround()) {
              isDoubleJump = DoubleJumpState.ONCE;
            } else if (!player.getAbilities().flying) {
              isDoubleJump = DoubleJumpState.TWICE;
            } else {
              if (isDoubleJump == DoubleJumpState.ONCE) {
                isDoubleJump = DoubleJumpState.TWICE;
              } else if (isDoubleJump == DoubleJumpState.INVALID) {
                isDoubleJump = DoubleJumpState.ONCE;
              }
            }
          }

          if (isDoubleJump == DoubleJumpState.TWICE) {
            twig.twigangelring.Angelring.canFly = !twig.twigangelring.Angelring.canFly;
            isDoubleJump = DoubleJumpState.INVALID;
          }
        } else {
          currentJumpTime = 0;
        }
        if (jumpDiff >= DOUBLE_JUMP_THRESHOLD) {
          isDoubleJump = DoubleJumpState.INVALID;
        }

      } else {
        twig.twigangelring.Angelring.canFly = false;
      }
    }
  }
}
