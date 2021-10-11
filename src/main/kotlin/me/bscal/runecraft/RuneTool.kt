package me.bscal.runecraft

import me.bscal.runecraft.RunecraftCustomItems.CreateChisel
import me.bscal.runecraft.RunecraftCustomItems.UsesKey
import me.bscal.runecraft.custom_items.CustomItem
import me.bscal.runecraft.custom_items.CustomItems
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.*
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class RuneTool(val InternalName: String, val Level: Int, val Stack: ItemStack)
{
	companion object
	{
		val IRON_CHISEL = RuneTool("iron_chisel", 1, CreateChisel("iron_chisel", "Iron Chisel", 11000, 16))
		val GOLD_CHISEL = RuneTool("gold_chisel", 2, CreateChisel("gold_chisel", "Gold Chisel", 11001, 16))
		val DIAMOND_CHISEL = RuneTool("diamond_chisel", 3, CreateChisel("diamond_chisel", "Diamond Chisel", 11002, 32))
	}

	init
	{
		if (!Stack.hasItemMeta()) assert(false) { "RuneTool has no ItemMeta" }
		if (!Stack.itemMeta.hasCustomModelData()) assert(false) { "RuneTool has no customModelData" }
		if (!Stack.itemMeta.persistentDataContainer.has(UsesKey, PersistentDataType.INTEGER)) assert(
			false) { "RuneTool has no UsesKey persistent data!" }
		Register()
	}

	@Suppress("DEPRECATION")
	fun ItemsEquals(otherStack: ItemStack): Boolean
	{
		val comparisonType = if (Stack.type.isLegacy) Bukkit.getUnsafe().fromLegacy(Stack.data, true)
		else Stack.type // This may be called from legacy item stacks, try to get the right material

		return comparisonType == otherStack.type && otherStack.hasItemMeta() && Bukkit.getItemFactory()
			.equals(this.Stack.itemMeta, otherStack.itemMeta)
	}

	fun AllowUse(itemStack: ItemStack, uses: Int = 1): Boolean
	{
		return (itemStack.itemMeta.persistentDataContainer.get(UsesKey, PersistentDataType.INTEGER) ?: 0) - uses > 0
	}

	fun Deincremeant(itemStack: ItemStack, uses: Int = 1) : Int
	{
		val data = itemStack.itemMeta.persistentDataContainer
		val newUses = (data.get(UsesKey, PersistentDataType.INTEGER) ?: 0) - uses
		if (newUses < 1) itemStack.amount = 0
		else data.set(UsesKey, PersistentDataType.INTEGER, newUses)
		return newUses
	}

	fun UpdateLore(itemStack: ItemStack, uses: Int)
	{
		if (uses < 1) return
		// For each in case another plugin or something add lore
		itemStack.lore()?.forEach {
			it as TextComponent
			val str = it.content()
			if (str.contains("Uses:"))
			{
				val split = str.split(':')
				it.content("${split[0]} $uses")
				return
			}
		}
	}

	private fun Register()
	{
		CustomItems.Items[InternalName] = CustomItem(Stack, true)
	}
}

object RunecraftCustomItems
{
	const val CHISEL_LORE_USE = "Used in the art of rune crafting"

	val NameKey = NamespacedKey(RuneCraft.INSTANCE, "custom_name")
	val UsesKey = NamespacedKey(RuneCraft.INSTANCE, "uses")

	fun CreateChisel(internalName: String, displayName: String, modelId: Int, uses: Int): ItemStack
	{
		val itemstack = itemStack(Material.WOODEN_HOE) {
			meta {
				name = "${KColors.WHITE}$displayName"
				addLore {
					+CHISEL_LORE_USE
					+"Uses: $uses"
				}
				customModel = modelId
				persistentDataContainer.set(NameKey, PersistentDataType.STRING, internalName)
				persistentDataContainer.set(UsesKey, PersistentDataType.INTEGER, uses)
			}
		}
		return itemstack
	}
}