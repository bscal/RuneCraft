package me.bscal.runecraft.items.customitems

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.bscal.runecraft.RuneCraft
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer
import java.util.logging.Level

@JvmRecord data class CustomId(val Type: Material, val ModelId: Int)

abstract class CustomItem(val DefaultStack: ItemStack, val Cancel: Boolean, val InteractCallback: Consumer<PlayerInteractEvent>? = null)
{
	open fun NewStack(): ItemStack
	{
		return DefaultStack.clone()
	}
}

object CustomItems
{
	private val Items = Object2ObjectOpenHashMap<String, CustomItem>()
	private val ItemsById = Object2ObjectOpenHashMap<CustomId, CustomItem>()

	fun Register(internalName: String, item: CustomItem)
	{
		Items[internalName] = item
		ItemsById[CustomId(item.DefaultStack.type, item.DefaultStack.itemMeta.customModelData)] = item
		RuneCraft.LogDebug(Level.INFO, "Registering item: $internalName, $item")
	}

	fun Register(internalName: String, customId: CustomId, item: CustomItem)
	{
		Items[internalName] = item
		ItemsById[customId] = item
		RuneCraft.LogDebug(Level.INFO, "Registering item: $internalName, $customId")
	}

	fun GetByName(internalName: String): CustomItem?
	{
		return Items[internalName]
	}

	fun GetById(customId: CustomId): CustomItem?
	{
		return ItemsById[customId]
	}

	fun GetByItemstack(itemStack: ItemStack): CustomItem?
	{
		if (!itemStack.hasItemMeta()) return null
		return GetById(CustomId(itemStack.type, itemStack.itemMeta.customModelData))
	}

}