package twig.twigangelring;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;

public class Angelring implements ModInitializer {
  public static final String MOD_ID = "twigangelring";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static boolean canFly = false;

  @Override
  public void onInitialize() {
    LOGGER.info("Hello From Twig Angel Ring");
    AngelringItem.initialize();

    ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
      if (source.getType().msgId().toLowerCase().contains("fall") && entity instanceof ServerPlayerEntity player) {

        if (TrinketsApi.getTrinketComponent(player) != null) {
          TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).get();

          if (trinket.isEquipped(twig.twigangelring.AngelringItem.angelRing))
            return false;
        }
      }
      return true;
    });
    ServerTickEvents.START_SERVER_TICK.register(event -> {
      for (ServerPlayerEntity player : event.getPlayerManager().getPlayerList()) {
        if (TrinketsApi.getTrinketComponent(player) != null) {
          TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).get();
          if (trinket.isEquipped(twig.twigangelring.AngelringItem.angelRing)) {
            if (canFly) {
              player.getAbilities().allowFlying = true;
              player.getAbilities().flying = true;
            } else {
              player.getAbilities().allowFlying = false;
              player.getAbilities().flying = false;
            }
            player.sendAbilitiesUpdate();
          }
        }
      }
    });
  }
}
