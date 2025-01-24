package twig.twigangelring;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class AngelringClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    KeyBinding boostKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.twiganglering.boost",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "category.twiganglering.angelring"));

    ClientTickEvents.END_CLIENT_TICK.register(c -> {
      if (c.player.isFallFlying()) {
        if (boostKey.isPressed()) {
          c.player.setVelocity(c.player.getVelocity().add(0, 0.5, 0));
        }
      }
    });
  }
}
