package twig.twigangelring;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;

public class Angelring implements ModInitializer {
  public static final String MOD_ID = "twigangelring";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static boolean canFly = false;

  public static final Identifier FLY_STATE_CHANGE_PACKET_ID = Identifier.of("twigangelring", "fly_state_change");
  public static final Identifier BOOST_PACKET_ID = Identifier.of("twigangelring", "ring_boost");

  @Override
  public void onInitialize() {
    LOGGER.info("Hello From Twig Angel Ring");
    AngelringItem.initialize();

    ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
      if (source.getType().msgId().toLowerCase().contains("fall") && entity instanceof ServerPlayerEntity player) {

        if (TrinketsApi.getTrinketComponent(player) != null) {
          TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).get();

          if (trinket.isEquipped(twig.twigangelring.AngelringItem.angelRing)) {
            return false;
          }
        }
      }
      return true;
    });

    ServerTickEvents.START_SERVER_TICK.register(event -> {
      for (ServerPlayerEntity player : event.getPlayerManager().getPlayerList()) {
        if (TrinketsApi.getTrinketComponent(player) != null
            && player.interactionManager.getGameMode() == GameMode.SURVIVAL) {
          TrinketComponent trinket = TrinketsApi.getTrinketComponent(player).get();
          Boolean hasTrinket = trinket.isEquipped(twig.twigangelring.AngelringItem.angelRing);

          player.getAbilities().allowFlying = hasTrinket && canFly;
          player.getAbilities().flying = hasTrinket && canFly;
          player.sendAbilitiesUpdate();
          twig.twigangelring.Angelring.canFly = hasTrinket && canFly;
        }
      }
    });

    ServerPlayNetworking.registerGlobalReceiver(
        FLY_STATE_CHANGE_PACKET_ID,
        (server, player, handler, buf, responseSender) -> {
          Boolean flystate = buf.readBoolean();
          canFly = flystate;
        });

    ServerPlayNetworking.registerGlobalReceiver(
        BOOST_PACKET_ID,
        (server, player, handler, buf, responseSender) -> {
          Vector3f boostVel = buf.readVector3f();
          player.setVelocity(new Vec3d((double) boostVel.x, (double) boostVel.y, (double) boostVel.z));
        });
  }
}
