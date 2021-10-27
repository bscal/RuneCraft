package me.bscal.runecraft.items.customitems

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.bscal.runecraft.stats.PotionEffectTypeTag
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import java.util.*

class PlayerItemSlot(val Slot: EquipmentSlot?)
{
	companion object
	{
		val HEAD = PlayerItemSlot(EquipmentSlot.HEAD)
		val CHEST = PlayerItemSlot(EquipmentSlot.CHEST)
		val LEGS = PlayerItemSlot(EquipmentSlot.LEGS)
		val FEET = PlayerItemSlot(EquipmentSlot.FEET)
		val HAND = PlayerItemSlot(EquipmentSlot.HAND)
		val OFF_HAND = PlayerItemSlot(EquipmentSlot.OFF_HAND)
	}
}

class StatPlayer
{
	val ItemPerSlot = Object2ObjectOpenHashMap<PlayerItemSlot, ItemStack>(6)
	val ItemSet = ObjectOpenHashSet<ItemStack>(6)

	fun AddItem(slot: PlayerItemSlot, itemStack: ItemStack)
	{
		ItemPerSlot[slot] = itemStack
		ItemSet.add(itemStack)
	}
}

class CustomItemListener : Listener
{
	private val m_Runnables = Object2ObjectOpenHashMap<UUID, KSpigotRunnable>()

	@EventHandler(priority = EventPriority.HIGH)
	fun OnJoin(event: PlayerJoinEvent)
	{
		val player: Player = event.player
		if (m_Runnables.containsKey(player.uniqueId)) m_Runnables.remove(player.uniqueId)?.cancel()
		m_Runnables[player.uniqueId] = task(period = 100) {
			if (!player.isOnline)
			{
				m_Runnables.remove(player.uniqueId)
				it.cancel()
				return@task
			}

			if (!player.isDead)
			{
				for (slot in EquipmentSlot.values())
				{
					val itemStack = player.inventory.getItem(slot)
					if (itemStack != null)
					{
						UpdatePotionStats(player, itemStack)
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	fun OnQuit(event: PlayerQuitEvent)
	{
		m_Runnables.remove(event.player.uniqueId)?.cancel()
	}

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
		}        // Todo player receiving damage
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
		if (newItemStack == null || !newItemStack.hasItemMeta()) return
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

	private fun UpdatePotionStats(player: Player, itemStack: ItemStack)
	{
		itemStack.editMeta {
			for (key in it.persistentDataContainer.keys)
			{
				val type = PotionEffectType.getByName(key.key)
				if (type != null)
				{
					val effect = it.persistentDataContainer.get(key, PotionEffectTypeTag())
					if (effect != null)
					{
						val currentEffect = player.getPotionEffect(effect.type)
						if (currentEffect != null && effect.amplifier >= currentEffect.amplifier)
						{
							player.removePotionEffect(effect.type)
							player.addPotionEffect(effect)
						}
					}
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