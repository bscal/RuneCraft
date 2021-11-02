package me.bscal.runecraft.gui.runeboard

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import me.bscal.runecraft.items.customitems.CustomItems
import me.bscal.runecraft.items.runeitems.BreakLevel
import me.bscal.runecraft.items.runeitems.RuneTool
import me.bscal.runecraft.stats.StatInstance
import me.bscal.runecraft.stats.StatRegistry
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.addLore
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.Serializable

interface IBoardSlot : Serializable
{
	fun GetGuiItem(): GuiItem

	fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)

	fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)

	fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean

	fun GetInstabilityLost(): Int
}

open class GuiItemWrapper() : Externalizable
{
	@Transient lateinit var Item: GuiItem; protected set

	constructor(itemStack: ItemStack) : this()
	{
		Item = GuiItem(itemStack)
	}

	override fun writeExternal(out: ObjectOutput?)
	{
		if (Item.item.type.isAir) return
		out?.write(Item.item.serializeAsBytes())
	}

	override fun readExternal(input: ObjectInput?)
	{
		try
		{
			val bytes = ByteArray(input?.available() ?: 255)
			input?.read(bytes)
			Item = if (bytes.isEmpty())
			{
				GuiItem(ItemStack(Material.AIR))
			}
			else
			{
				val itemStack = ItemStack.deserializeBytes(bytes)
				GuiItem(itemStack) {
					it.result = Event.Result.DENY
					if (it.clickedInventory != null && it.clickedInventory?.type == InventoryType.CHEST && it.isLeftClick)
					{
						val x = 5.coerceAtMost(0.coerceAtLeast(it.slot % 9 - 2))
						val y = it.slot / 9
						val key = RuneBoard.PackCoord(x, y)
						val board = RuneBoardCache[it.whoClicked.uniqueId]
						if (board != null && board.GetGuiTitle() == it.view.title)
						{
							val tool: ItemStack = it.cursor ?: ItemStack(Material.AIR)
							val customItem = CustomItems.GetByItemStack(tool)
							if (customItem is RuneTool)
							{
								val slot = board.Slots[key]
								if (board.CanBreak(x, y, slot, tool, customItem, it))
								{
									slot.OnBreak(x, y, tool, customItem, it)
									board.OnBreak(x, y, slot, tool, customItem, it)
								}
							}
						}
					}
				}
			}
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

}

abstract class BoardSlot(itemStack: ItemStack, val InstabilityLost: Int, val BreakLevel: BreakLevel) : IBoardSlot
{
	companion object
	{
		@JvmStatic val serialVersionID = 1L
	}

	var GuiItem: GuiItemWrapper = GuiItemWrapper(itemStack);

	init
	{
		GuiItem.Item.setAction {
			it.result = Event.Result.DENY
			if (it.clickedInventory != null && it.clickedInventory?.type == InventoryType.CHEST && it.isLeftClick)
			{
				val x = 5.coerceAtMost(0.coerceAtLeast(it.slot % 9 - 2))
				val y = it.slot / 9
				val key = RuneBoard.PackCoord(x, y)
				val board = RuneBoardCache[it.whoClicked.uniqueId]
				if (board != null && board.GetGuiTitle() == it.view.title)
				{
					val tool: ItemStack = it.cursor ?: ItemStack(Material.AIR)
					val customItem = CustomItems.GetByItemStack(tool)
					if (customItem is RuneTool)
					{
						val slot = board.Slots[key]
						if (board.CanBreak(x, y, slot, tool, customItem, it))
						{
							slot.OnBreak(x, y, tool, customItem, it)
							board.OnBreak(x, y, slot, tool, customItem, it)
						}
					}
				}
			}
		}
	}

	override fun GetGuiItem(): GuiItem = GuiItem.Item

	override fun GetInstabilityLost(): Int = InstabilityLost
}

class EmptySlot() : BoardSlot(ItemStack(Material.AIR), 0, BreakLevel.UNBREAKABLE)
{ //
	//	@Throws(IOException::class, ClassNotFoundException::class)
	//	private fun readObject(objIn: ObjectInputStream)
	//	{
	//		objIn.defaultReadObject()
	//		GuiItem = GuiItemWrapper(ItemStack(Material.AIR))
	//	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean =
		false
}

class LineSlot(itemStack: ItemStack) : BoardSlot(itemStack, 0, BreakLevel.UNBREAKABLE)
{
	init
	{
		GuiItem.Item.item.editMeta {
			it.name = "${KColors.LIGHTGRAY}Rune Line"
		}
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean =
		false

}

class DefaultSlot(itemStack: ItemStack, stabilityLost: Int, breakLevel: BreakLevel) : BoardSlot(itemStack, stabilityLost, breakLevel)
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}-1 Stability"))
		lore.add(Component.text("${KColors.RED}-1 Durability"))
		GuiItem.Item.item.lore(lore)
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

class BedrockSlot : BoardSlot(ItemStack(Material.BEDROCK), 0, BreakLevel.UNBREAKABLE)
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}Unbreakable"))
		GuiItem.Item.item.lore(lore)
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean =
		false
}

class DirtSlot(isGrass: Boolean) : BoardSlot(ItemStack(if (isGrass) Material.GRASS_BLOCK else Material.DIRT), 0, BreakLevel.ANY)
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}-1 Stability"))
		GuiItem.Item.item.lore(lore)
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true

}

abstract class GemSlot(itemStack: ItemStack) : BoardSlot(itemStack, 0, BreakLevel.UNBREAKABLE)
{
	val Stats = ArrayList<StatInstance>()

	init
	{
		GuiItem.Item.item.meta {
			this.addLore {
				Stats.forEach { +it.GetStat().GetLoreString(it) } // TODO better way? thinking removing stat if deserialized
			}
		}
	}

	constructor() : this(ItemStack(Material.AIR))
}

class DiamondSlot : GemSlot(itemStack(Material.DIAMOND_BLOCK) {
	meta {
		name = "${KColors.LIGHTSKYBLUE}Diamond Gem"
	}
})
{
	init
	{
		Stats.add(StatRegistry.VANILLA_STAT.NewStatInstance(Attribute.GENERIC_ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADD_NUMBER))
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

class EmeraldSlot : GemSlot(itemStack(Material.EMERALD_BLOCK) {
	meta {
		name = "${KColors.MEDIUMSPRINGGREEN}Emerald Gem"
	}
})
{
	init
	{
		Stats.add(StatRegistry.VANILLA_STAT.NewStatInstance(Attribute.GENERIC_MAX_HEALTH, 1.0, AttributeModifier.Operation.ADD_NUMBER))
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

class RedstoneSlot : GemSlot(itemStack(Material.REDSTONE_BLOCK) {
	meta {
		name = "${KColors.DARKRED}Ruby Gem"
	}
})
{
	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

class LapisSlot : GemSlot(itemStack(Material.LAPIS_BLOCK) {
	meta {
		name = "${KColors.DARKBLUE}Sapphire Gem"
	}
})
{
	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

class AmethystSlot : GemSlot(itemStack(Material.AMETHYST_BLOCK) {
	meta {
		name = "${KColors.DARKPURPLE}Amethyst Gem"
	}
})
{
	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

abstract class RuneSlot(itemStack: ItemStack) : GemSlot(itemStack)

class HealthRuneSlot(itemStack: ItemStack) : RuneSlot(itemStack)
{
	init
	{
		Stats.add(StatRegistry.VANILLA_STAT.NewStatInstance(Attribute.GENERIC_MAX_HEALTH, 1.0, AttributeModifier.Operation.ADD_NUMBER))
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean
	{
		return true
	}

}

class DamageRuneSlot(itemStack: ItemStack) : RuneSlot(itemStack)
{
	init
	{
		Stats.add(StatRegistry.VANILLA_STAT.NewStatInstance(Attribute.GENERIC_ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADD_NUMBER))
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean
	{
		return true
	}

}