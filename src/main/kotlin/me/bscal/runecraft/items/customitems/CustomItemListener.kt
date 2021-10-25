package me.bscal.runecraft.items.customitems

import me.bscal.runecraft.stats.PotionEffectTypeTag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

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
	fun OnEntityDamage(event: EntityDamageByEntityEvent)
	{
		if (event.isCancelled) return
		if (event.damager is Player)
		{
			val player = event.damager as Player
			val item = player.itemInUse
			if (item != null && !item.type.isAir)
			{
				val customItem = CustomItems.GetByItemStack(item)
				customItem?.AttackCallback?.Invoke(player, item, event)
			}
		}		// Todo player receiving damage
	}

	@EventHandler(priority = EventPriority.HIGH)
	fun OnPlayerEquipItem(event: InventoryClickEvent)
	{
		if (event.isCancelled) return

		if (event.slotType == InventoryType.SlotType.ARMOR)
		{
			val player = event.view.player as Player
			val oldItemStack = event.inventory.getItem(event.slot)
			val newItemStack = event.currentItem
			HandleCustomItems(player, oldItemStack, newItemStack, CustomItemInitContext.INVENTORY_CLICK)
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	fun OnPlayerItemHeld(event: PlayerItemHeldEvent)
	{
		if (event.isCancelled) return

		val player = event.player
		val oldItemStack = player.inventory.getItem(event.previousSlot)
		val newItemStack = player.inventory.getItem(event.newSlot)
		HandleCustomItems(player, oldItemStack, newItemStack, CustomItemInitContext.ITEM_SWAP)
	}

	@EventHandler(priority = EventPriority.HIGH)
	fun OnPlayerSwapHands(event: PlayerSwapHandItemsEvent)
	{
		if (event.isCancelled) return

		val offhand = event.offHandItem
		if (offhand != null && !offhand.type.isAir && offhand.hasItemMeta())
		{
			val offhandCustomItem = CustomItems.GetByItemStack(offhand)
			offhandCustomItem?.InitilizeCallback?.Invoke(event.player, offhand, CustomItemInitContext.OFFHAND_SWAP)
		}
	}

	private fun InitPotionEffectStats(player: Player, itemStack: ItemStack)
	{
		// TODO passives
		itemStack.editMeta {
			for (key in it.persistentDataContainer.keys)
			{
				val type = PotionEffectType.getByName(key.key)
				if (type != null)
				{
					val effect = it.persistentDataContainer.get(key, PotionEffectTypeTag())
					if (effect != null) player.addPotionEffect(effect)
				}
			}
		}
	}

	private fun HandleInitItem(player: Player, itemStack: ItemStack?, context: CustomItemInitContext)
	{
		if (itemStack != null && !itemStack.type.isAir && itemStack.hasItemMeta())
		{
			val offhandCustomItem = CustomItems.GetByItemStack(itemStack)
			offhandCustomItem?.InitilizeCallback?.Invoke(player, itemStack, context)
		}
	}

	private fun HandleRemoveItem(player: Player, itemStack: ItemStack?, context: CustomItemInitContext)
	{
		if (itemStack != null && !itemStack.type.isAir && itemStack.hasItemMeta())
		{
			val offhandCustomItem = CustomItems.GetByItemStack(itemStack)
			offhandCustomItem?.RemoveCallback?.Invoke(player, itemStack, context)
		}
	}

	private fun HandleCustomItems(player: Player, oldItem: ItemStack?, newItem: ItemStack?, context: CustomItemInitContext)
	{
		if (oldItem != null && !oldItem.type.isAir && oldItem.hasItemMeta())
		{
			val oldCustomItem = CustomItems.GetByItemStack(oldItem)
			oldCustomItem?.RemoveCallback?.Invoke(player, oldItem, context)
		}

		if (newItem != null && !newItem.type.isAir && newItem.hasItemMeta())
		{
			val newCustomItem = CustomItems.GetByItemStack(newItem)
			newCustomItem?.InitilizeCallback?.Invoke(player, newItem, context)
		}
	}
}