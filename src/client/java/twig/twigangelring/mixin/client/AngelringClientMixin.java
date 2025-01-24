package twig.twigangelring.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;

@Mixin(ClientPlayerEntity.class)
public class AngelringClientMixin {
  private int tickTimer = 0;
  private boolean isDoubleJump = false;
  private boolean jumpKeyIsDown = false;

  @Inject(at = @At("HEAD"), method = "tick")
  private void onTick(CallbackInfo info) {
    MinecraftClient client = MinecraftClient.getInstance();
    ClientPlayerEntity player = client.player;

    KeyBinding jumpKey = client.options.jumpKey;

    if (TrinketsApi.getTrinketComponent(player) != null) {
      TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).get();

      if (jumpKey.isPressed() && !player.isOnGround() &&
          !player.getAbilities().creativeMode) {
        jumpKeyIsDown = true;
        tickTimer += 1;
        if (tickTimer >= 20) {
          tickTimer = 0;
          isDoubleJump = false;
        }

        if (trinket.isEquipped(twig.twigangelring.AngelringItem.angelRing)) {
          if (isDoubleJump) {
            if (jumpKeyIsDown) {
              twig.twigangelring.Angelring.canFly = true;
            } else {
              twig.twigangelring.Angelring.canFly = false;
            }
          } else if (!isDoubleJump) {
            isDoubleJump = true;
          }
        } else {
          twig.twigangelring.Angelring.canFly = false;
        }
      }

      if (!jumpKey.isPressed() && tickTimer >= 20) {
        jumpKeyIsDown = false;
      }
    }
  }
}
