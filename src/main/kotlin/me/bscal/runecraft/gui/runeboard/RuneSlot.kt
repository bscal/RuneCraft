package me.bscal.runecraft.gui.runeboard

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import me.bscal.runecraft.items.customitems.CustomItems
import me.bscal.runecraft.items.runeitems.BreakLevel
import me.bscal.runecraft.items.runeitems.RuneTool
import me.bscal.runecraft.stats.StatInstance
import me.bscal.runecraft.stats.StatRegistry
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.addLore
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
import java.io.IOException
import java.io.ObjectInputStream
import java.io.Serializable

interface IBoardSlot : Serializable
{
	fun GetGuiItem(): GuiItem

	fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)

	fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)

	fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean

	fun GetInstabilityLost(): Int
}

abstract class BoardSlot(val MaterialType: Material, val InstabilityLost: Int, val BreakLevel: BreakLevel) : IBoardSlot
{
	@Transient var Item: GuiItem; protected set

	init
	{
		Item = GuiItem(ItemStack(MaterialType))
		Item.setAction(::OnClick)
	}

	@Throws(IOException::class, ClassNotFoundException::class)
	private fun readObject(objIn: ObjectInputStream)
	{
		objIn.defaultReadObject()
		Item = GuiItem(ItemStack(MaterialType))
		Item.setAction(::OnClick)
	}

	private fun OnClick(it: InventoryClickEvent)
	{
		it.result = Event.Result.DENY
		if (it.clickedInventory != null && it.clickedInventory?.type == InventoryType.CHEST && it.isLeftClick)
		{
			val x = 5.coerceAtMost(0.coerceAtLeast(it.slot % 9 - 2))
			val y = it.slot / 9
			val key = RuneBoard.PackCoord(x, y)
			val board = RuneBoardCache[it.whoClicked.uniqueId]
			if (board != null && board.GetGuiTitle().equals(it.view.title))
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

	override fun GetGuiItem(): GuiItem = Item

	override fun GetInstabilityLost(): Int = InstabilityLost
}

class LineSlot(materialType: Material) : BoardSlot(materialType, 0, BreakLevel.UNBREAKABLE)
{
	init
	{
		Item.item.editMeta {
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

class DefaultSlot(materialType: Material, stabilityLost: Int, breakLevel: BreakLevel) : BoardSlot(materialType, stabilityLost, breakLevel)
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}-1 Stability"))
		lore.add(Component.text("${KColors.RED}-1 Durability"))
		Item.item.lore(lore)
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

class BedrockSlot : BoardSlot(Material.BEDROCK, 0, BreakLevel.UNBREAKABLE)
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}Unbreakable"))
		Item.item.lore(lore)
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

class DirtSlot(isGrass: Boolean) : BoardSlot(if (isGrass) Material.GRASS_BLOCK else Material.DIRT, 0, BreakLevel.ANY)
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}-1 Stability"))
		Item.item.lore(lore)
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true

}

abstract class GemSlot(name: String, material: Material) : BoardSlot(material, 0, BreakLevel.UNBREAKABLE)
{
	val Stats = ArrayList<StatInstance>()

	init
	{
		Item.item.meta {
			this.name = name
			this.addLore {
				Stats.forEach { +it.GetStat()?.GetLoreString(it)!! } // TODO better way? thinking removing stat if deserialized
			}
		}
	}
}

class DiamondSlot : GemSlot("${KColors.LIGHTSKYBLUE}Diamond Gem", Material.DIAMOND_BLOCK)
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

class EmeraldSlot : GemSlot("${KColors.MEDIUMSPRINGGREEN}Emerald Gem", Material.EMERALD_BLOCK)
{
	init
	{
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

class RedstoneSlot : GemSlot("${KColors.DARKRED}Ruby Gem", Material.REDSTONE_BLOCK)
{
	init
	{
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

class LapisSlot : GemSlot("${KColors.DARKBLUE}Sapphire Gem", Material.LAPIS_BLOCK)
{
	init
	{
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

class AmethystSlot : GemSlot("${KColors.DARKPURPLE}Amethyst Gem", Material.AMETHYST_BLOCK)
{
	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}
