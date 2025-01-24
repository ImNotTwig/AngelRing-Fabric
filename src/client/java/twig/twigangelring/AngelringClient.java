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

public class AngelringClient implements ClientModInitializer {
  private static final double BOOST_COOLDOWN = 500;
  private static double lastBoostTime = 0;

  @Override
  public void onInitializeClient() {
    KeyBinding boostKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.twiganglering.boost",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "category.twiganglering.angelring"));


    ClientTickEvents.END_CLIENT_TICK.register(c -> {
      if (c.player != null) {
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
    });

  }
}
