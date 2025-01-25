package twig.twigangelring;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;

public class AngelringClient implements ClientModInitializer {
  private static final double BOOST_COOLDOWN = 500;
  private static double lastBoostTime = 0;

  private DoubleJumpState isDoubleJump = DoubleJumpState.INVALID;

  private Boolean canFly = false;

  private long lastJumpTime = System.currentTimeMillis();
  private long currentJumpTime = 0;
  private long jumpDiff = currentJumpTime - lastJumpTime;
  private static final long DOUBLE_JUMP_THRESHOLD = 500;
  private static final long HOLD_THRESHOLD = 75;

  @Override
  public void onInitializeClient() {
    KeyBinding boostKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.twiganglering.boost",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "category.twiganglering.angelring"));

    ClientTickEvents.END_CLIENT_TICK.register(c -> {
      if (c.player != null) {

        if (TrinketsApi.getTrinketComponent(c.player) != null) {
          TrinketComponent trinket = TrinketsApi.getTrinketComponent(c.player).get();
          if (trinket.isEquipped(twig.twigangelring.AngelringItem.angelRing)) {
            if (boostKey.isPressed()) {
              PacketByteBuf buf = PacketByteBufs.create();
              double currentTime = System.currentTimeMillis();
              if (currentTime - lastBoostTime > BOOST_COOLDOWN) {
                if (c.player.isFallFlying()) {
                  Vec3d lookDirection = c.player.getRotationVector();
                  double boostAmount = 2;
                  Vec3d newVel = c.player.getVelocity().add(lookDirection.multiply(boostAmount));
                  c.player.setVelocity(c.player.getVelocity().add(lookDirection.multiply(boostAmount)));
                  buf.writeVector3f(new Vector3f((float) newVel.x, (float) newVel.y, (float) newVel.z));
                  ClientPlayNetworking.send(Angelring.BOOST_PACKET_ID, buf);
                  lastBoostTime = currentTime;
                }
              }
            }
          }
        }
      }
    });

    ClientTickEvents.END_CLIENT_TICK.register(c -> {
      if (c.player != null) {

        KeyBinding jumpKey = c.options.jumpKey;

        if (TrinketsApi.getTrinketComponent(c.player) != null) {
          TrinketComponent trinket = TrinketsApi.getTrinketComponent(c.player).get();

          if (c.player.isOnGround()) {
            canFly = false;
          }

          if (trinket.isEquipped(twig.twigangelring.AngelringItem.angelRing)) {

            if (jumpKey.isPressed()) {
              currentJumpTime = System.currentTimeMillis();

              jumpDiff = currentJumpTime - lastJumpTime;
              lastJumpTime = currentJumpTime;

              // dont process double jumps if we're holding down the key
              if (jumpDiff > HOLD_THRESHOLD) {
                if (jumpDiff < DOUBLE_JUMP_THRESHOLD) {
                  if (c.player.isOnGround()) {
                    isDoubleJump = DoubleJumpState.ONCE;
                  } else if (!c.player.getAbilities().flying) {
                    isDoubleJump = DoubleJumpState.TWICE;
                  } else {
                    if (isDoubleJump == DoubleJumpState.ONCE) {
                      isDoubleJump = DoubleJumpState.TWICE;
                    } else if (isDoubleJump == DoubleJumpState.INVALID) {
                      isDoubleJump = DoubleJumpState.TWICE;
                    }
                  }
                }

                if (isDoubleJump == DoubleJumpState.TWICE) {
                  canFly = !canFly;
                  isDoubleJump = DoubleJumpState.INVALID;
                  lastJumpTime = 0;
                  currentJumpTime = 0;
                }
              } else {
                lastJumpTime = 0;
                currentJumpTime = 0;
              }
            } else {
              currentJumpTime = 0;
            }
            if (jumpDiff >= DOUBLE_JUMP_THRESHOLD) {
              isDoubleJump = DoubleJumpState.INVALID;
            }

          } else {
            canFly = false;
          }

          PacketByteBuf buf = PacketByteBufs.create();
          buf.writeBoolean(canFly);

          ClientPlayNetworking.send(Angelring.FLY_STATE_CHANGE_PACKET_ID, buf);
        }
      }
    });

  }
}
