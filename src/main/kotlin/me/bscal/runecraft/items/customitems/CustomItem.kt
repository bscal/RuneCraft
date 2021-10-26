package me.bscal.runecraft.items.customitems

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.bscal.runecraft.RuneCraft
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer
import java.util.logging.Level

@JvmRecord data class CustomId(val Type: Material, val ModelId: Int)

abstract class CustomItem(val DefaultStack: ItemStack, val Cancel: Boolean)
{
	var InteractCallback: Consumer<PlayerInteractEvent>? = null
	var InitilizeCallback: CustomItemInitCallback? = null
	var RemoveCallback: CustomItemRemoveCallback? = null
	var AttackCallback: CustomItemAttackCallback? = null
	//val DamagedCallback: CustomItemDamagedCallback? = null
	//val MineCallback: Consumer<BlockBreakEvent>? = null

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

	fun GetByItemStack(itemStack: ItemStack): CustomItem?
	{
		if (!itemStack.hasItemMeta()) return null
		return GetById(CustomId(itemStack.type, itemStack.itemMeta.customModelData))

	}

}

class CustomItemInitContext private constructor(val Id: Int)
{
	companion object
	{
		val INVENTORY_CLICK = CustomItemInitContext(0)
		val ITEM_SWAP = CustomItemInitContext(1)
		val OFFHAND_SWAP = CustomItemInitContext(2)
	}
}

interface CustomItemAttackCallback
{
	fun Invoke(Player: Player, ItemStack: ItemStack, event: EntityDamageByEntityEvent)
}

interface CustomItemInitCallback
{
	fun Invoke(Player: Player, ItemStack: ItemStack, context: CustomItemInitContext)
}

interface CustomItemRemoveCallback
{
	fun Invoke(Player: Player, ItemStack: ItemStack, context: CustomItemInitContext)
}
