package me.bscal.runecraft.items.customitems

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class CustomItemListener : Listener
{
	@EventHandler(priority = EventPriority.HIGH)
	fun OnPlayerInteract(event: PlayerInteractEvent)
	{
		if (event.isCancelled) return

		val item = event.item
		if (item?.hasItemMeta() == true)
		{
			val id = CustomId(item.type, item.itemMeta.customModelData)
			val customItem = CustomItems.GetById(id)
			if (customItem != null)
			{
				if (customItem.Cancel) event.isCancelled = true
				customItem.InteractCallback?.accept(event)
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	fun OnPlayerEquipItem(event: PlayerArmorChangeEvent)
	{
	}
}