package me.bscal.runecraft.custom_items

import com.destroystokyo.paper.NamespacedTag
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.bscal.runecraft.RuneCraft
import net.axay.kspigot.items.*
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

const val CHISEL_LORE_USE = "Used in the art of rune crafting"

data class CustomItem(val ItemStack: ItemStack, val Cancel: Boolean, val Components: ObjectArrayList<Component> = ObjectArrayList())
{
}

fun CreateChisel(internalName: String, displayName: String, modelId: Int, uses: Int) : CustomItem
{
	val itemstack = itemStack(Material.WOODEN_HOE) {
		meta {
			name = displayName
			addLore {
				CHISEL_LORE_USE
				"Uses: ${uses}"
			}
			setCustomModelData(modelId)
			persistentDataContainer.set(NameKey, PersistentDataType.STRING, internalName)
			persistentDataContainer.set(UsesKey, PersistentDataType.INTEGER, uses)
		}
	}
	val item = CustomItem(itemstack, true)
	CustomItems.Items[internalName] = item
	return item
}

object CustomItems
{
	val Items = Object2ObjectOpenHashMap<String, CustomItem>()

	init
	{
		CreateChisel("iron_chisel", "Iron Chisel", 11000, 16)
		CreateChisel("gold_chisel", "Gold Chisel", 11001, 16)
		CreateChisel("diamond_chisel", "Diamond Chisel", 11002, 32)
	}
}

val NameKey = NamespacedKey(RuneCraft.INSTANCE, "custom_name")
val UsesKey = NamespacedKey(RuneCraft.INSTANCE, "uses")

class CustomItemsListener : Listener
{
	@EventHandler(priority = EventPriority.HIGH)
	fun OnPlayerInteract(event: PlayerInteractEvent)
	{
		val item = event.item
		if (item?.hasItemMeta() == true && item.type == Material.WOODEN_HOE)
		{
			val data = item.itemMeta.persistentDataContainer
			if (data.has(NameKey, PersistentDataType.STRING)) {
				val itemId = data.get(NameKey, PersistentDataType.STRING)
				val customItem = CustomItems.Items[itemId]
				if (customItem?.Cancel == true) event.isCancelled = true
			}
		}
	}
}