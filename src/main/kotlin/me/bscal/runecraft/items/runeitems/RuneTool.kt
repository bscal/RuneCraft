package me.bscal.runecraft.items.runeitems

import me.bscal.runecraft.RuneCraft
import me.bscal.runecraft.items.customitems.CustomId
import me.bscal.runecraft.items.customitems.CustomItem
import me.bscal.runecraft.items.customitems.CustomItems
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class RuneTool(val InternalName: String, val Level: BreakLevel, defaultStack: ItemStack) : CustomItem(defaultStack, true)
{
	init
	{
		if (!DefaultStack.hasItemMeta()) assert(false) { "RuneTool has no ItemMeta" }
		if (!DefaultStack.itemMeta.hasCustomModelData()) assert(false) { "RuneTool has no customModelData" }
		if (!DefaultStack.itemMeta.persistentDataContainer.has(RuneCraftItems.UsesKey, PersistentDataType.INTEGER)) assert(
			false) { "RuneTool has no UsesKey persistent data!" }
	}

	fun Register()
	{
		CustomItems.Register(InternalName, CustomId(DefaultStack.type, DefaultStack.itemMeta.customModelData), this)
	}

	@Suppress("DEPRECATION")
	fun ItemsEquals(otherStack: ItemStack): Boolean
	{
		val comparisonType = if (DefaultStack.type.isLegacy) Bukkit.getUnsafe().fromLegacy(DefaultStack.data, true)
		else DefaultStack.type // This may be called from legacy item stacks, try to get the right material

		return comparisonType == otherStack.type && otherStack.hasItemMeta() && Bukkit.getItemFactory()
			.equals(this.DefaultStack.itemMeta, otherStack.itemMeta)
	}

	fun AllowUse(itemStack: ItemStack, uses: Int = 1): Boolean
	{
		return true //(itemStack.itemMeta.persistentDataContainer.get(UsesKey, PersistentDataType.INTEGER) ?: 0) - uses > 0
	}

	fun Deincremeant(itemStack: ItemStack, uses: Int = 1): Int
	{
		val meta = itemStack.itemMeta
		val newUses = (meta.persistentDataContainer.get(RuneCraftItems.UsesKey, PersistentDataType.INTEGER) ?: 0) - uses
		if (newUses < 1) itemStack.amount = 0
		else
		{
			meta.persistentDataContainer.set(RuneCraftItems.UsesKey, PersistentDataType.INTEGER, newUses)
			RuneCraft.LogDebug(java.util.logging.Level.INFO, "Updating uses to $newUses")
		}
		itemStack.itemMeta = meta
		UpdateLore(itemStack, newUses)
		return newUses
	}

	fun UpdateLore(itemStack: ItemStack, uses: Int)
	{
		if (uses < 1) return  // For each in case another plugin or something add lore
		val meta = itemStack.itemMeta
		val lore = meta.lore
		itemStack.lore()
		if (lore != null)
		{
			for (i in 0 until lore.size)
			{
				if (lore[i].startsWith("Uses:")) lore[i] = "Uses: $uses"
			}
		}
		meta.lore = lore
		itemStack.itemMeta = meta
	}
}

enum class BreakLevel(val Id: Int)
{
	UNBREAKABLE(0), ANY(1), LEVEL_1(2), LEVEL_2(4), LEVEL_3(8);

	fun Match(level: BreakLevel, vararg allowed: BreakLevel): Boolean
	{
		var mask = 0
		allowed.forEach {
			mask += it.Id
		}
		return level.Id and mask > 0
	}
}