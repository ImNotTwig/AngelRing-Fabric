package twig.twigangelring;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;

public class AngelringItem extends TrinketItem {
  public AngelringItem(Settings settings) {
    super(settings);
  }

  public static Item register(Item item, String id) {
    // Create the identifier for the item.
    Identifier itemID = new Identifier(Angelring.MOD_ID, id);

    // Register the item.
    Item registeredItem = Registry.register(Registries.ITEM, itemID, item);

    // Return the registered item!
    return registeredItem;
  }

  public static final Item angelRing = AngelringItem.register(new Item(new FabricItemSettings()), "angelring");

  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(angelRing));
  }
}
