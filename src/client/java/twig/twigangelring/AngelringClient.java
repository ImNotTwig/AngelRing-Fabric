package twig.twigangelring;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
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
          double currentTime = System.currentTimeMillis();
          if (currentTime - lastBoostTime > BOOST_COOLDOWN) {
            if (c.player.isFallFlying()) {
              Vec3d lookDirection = c.player.getRotationVector();
              double boostAmount = 2;
              c.player.setVelocity(c.player.getVelocity().add(lookDirection.multiply(boostAmount)));
              lastBoostTime = currentTime;
            }
          }
        }
      }
    });
  }
}
