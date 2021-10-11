package me.bscal.runecraft.custom_items

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.bscal.runecraft.RunecraftCustomItems.NameKey
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

data class CustomItem(val ItemStack: ItemStack, val Cancel: Boolean, val Components: ObjectArrayList<Component> = ObjectArrayList())
{}

object CustomItems
{
	val Items = Object2ObjectOpenHashMap<String, CustomItem>()
}

class CustomItemsListener : Listener
{
	@EventHandler(priority = EventPriority.HIGH)
	fun OnPlayerInteract(event: PlayerInteractEvent)
	{
		val item = event.item
		if (item?.hasItemMeta() == true && item.type == Material.WOODEN_HOE)
		{
			val data = item.itemMeta.persistentDataContainer
			if (data.has(NameKey, PersistentDataType.STRING))
			{
				val itemId = data.get(NameKey, PersistentDataType.STRING)
				val customItem = CustomItems.Items[itemId]
				if (customItem?.Cancel == true) event.isCancelled = true
			}
		}
	}
}