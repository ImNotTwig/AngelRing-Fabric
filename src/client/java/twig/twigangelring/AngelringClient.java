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

  // TODO: change the thresholds to be based on ticks instead of milliseconds

  private static final int DOUBLE_JUMP_TICK_MIN = 1;
  private static final int DOUBLE_JUMP_TICK_MAX = 10;

  private long currentTick = 0;
  private long lastJumpTick = 0;
  private long currentJumpTick = 0;
  private long tickDiff = 0;

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
      if (c.player == null) {
        currentTick = 0;
        currentJumpTick = 0;
        lastJumpTick = 0;
        tickDiff = 0;
      } else if (c.player != null) {

        currentTick++;

        KeyBinding jumpKey = c.options.jumpKey;

        if (TrinketsApi.getTrinketComponent(c.player) != null) {
          TrinketComponent trinket = TrinketsApi.getTrinketComponent(c.player).get();

          if (c.player.isOnGround()) {
            canFly = false;
          }

          if (trinket.isEquipped(twig.twigangelring.AngelringItem.angelRing)) {

            if (jumpKey.isPressed()) {
              currentJumpTick = currentTick;

              tickDiff = currentJumpTick - lastJumpTick;
              lastJumpTick = currentJumpTick;

              // dont process double jumps if we're holding down the key
              if (tickDiff > DOUBLE_JUMP_TICK_MIN) {
                if (tickDiff < DOUBLE_JUMP_TICK_MAX) {
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
                  lastJumpTick = 0;
                  currentJumpTick = 0;
                }
              } else {
                lastJumpTick = 0;
                currentJumpTick = 0;
              }
            } else {
              currentJumpTick = 0;
            }
            if (tickDiff >= DOUBLE_JUMP_TICK_MAX) {
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
