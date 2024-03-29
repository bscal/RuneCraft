package me.bscal.runecraft.gui.runeboard

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.bscal.runecraft.Rune
import me.bscal.runecraft.RuneCraft
import me.bscal.runecraft.gui.GuiItems
import me.bscal.runecraft.gui.runeboard.slots.BoardSlot
import me.bscal.runecraft.gui.runeboard.slots.EmptySlot
import me.bscal.runecraft.gui.runeboard.slots.GemSlot
import me.bscal.runecraft.gui.runeboard.slots.LineSlot
import me.bscal.runecraft.items.customitems.CustomId
import me.bscal.runecraft.items.customitems.CustomItems
import me.bscal.runecraft.items.runeitems.BreakLevel
import me.bscal.runecraft.items.runeitems.RuneCraftItems
import me.bscal.runecraft.items.runeitems.RuneTool
import me.bscal.runecraft.utils.addStat
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.extensions.bukkit.give
import net.axay.kspigot.items.setLore
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.logging.Level
import kotlin.random.Random

const val SMALL_RUNE_SIZE = 4 * 4
const val LARGE_RUNE_SIZE = 6 * 6

val RuneBoardCache = Object2ObjectOpenHashMap<UUID, RuneBoard>()

/**
 * GUI interface for managing a rune's board
 */
class RuneBoard(val Rune: Rune, val Size: Int)
{
	companion object
	{
		// Map keys
		//fun PackCoord(x: Int, y: Int) : Int = x or y shl 16
		//fun UnpackCoord(key: Int) : Array<Int> = arrayOf(key shr 16, key and 0xff)
		fun PackCoord(x: Int, y: Int): Int = (x + y * 6) //.coerceAtLeast(0).coerceAtMost(5) I leave out to find bugs
		fun UnpackCoord(key: Int): Array<Int> = arrayOf(key % 6, key / 6)
	}
	private var Gui: ChestGui? = null
	private var Generator: BoardGenerator? = null
	private lateinit var RunePanel: StaticPane
	private lateinit var StabilityIcon: GuiItem
	private lateinit var StatsIcon: GuiItem
	private lateinit var Player: Player
	private lateinit var RuneItemStack: ItemStack

	private val LineSlots: IntArrayList = IntArrayList()
	private val LineGems: IntArrayList = IntArrayList()

	fun Generate(player: Player): Boolean
	{
		if (Rune.IsGenerated) return false
		Rune.IsGenerated = true
		Generator = BoardRegistry.Registry[Rune.Type] ?: BoardRegistry.Default
		Generator?.Generate(player, this, Rune.Rarity)
		return true
	}

	fun Open(player: Player, runeItemStack: ItemStack)
	{
		Player = player
		RuneItemStack = runeItemStack
		if (Gui == null)
		{
			Generate(player)
			Gui = ChestGui(6, "${KColors.RED}RuneCraft")
			Gui?.setOnClose {
				RuneBoardCache.remove(player.uniqueId, this)
				if (!Rune.IsBuilt) Rune.Serialize(runeItemStack)
			}
			Gui?.setOnTopClick { it.isCancelled = true }

			CreateHeaderPanel()
			CreateSeparatorPanel()
			CreateRunePanel()

			RuneBoardCache[player.uniqueId] = this
		}
		Gui?.show(player)
	}

	fun CanBreak(slot: BoardSlot, tool: RuneTool): Boolean
	{
		return slot.BreakLevel != BreakLevel.UNBREAKABLE && tool.Level.Id >= slot.BreakLevel.Id
	}

	fun OnBreak(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool)
	{
		tool.Deincremeant(itemStack)
		SetEmpty(x, y)
		AddInstability(slot.GetInstabilityLost())
		Update()
	}

	fun OnBuild(event: InventoryClickEvent)
	{
		event.isCancelled = true
		Rune.IsBuilt = true

		RuneItemStack.amount = RuneItemStack.amount - 1
		val builtRuneItem = RuneCraftItems.CARVED_RUNE.NewStack(Rune)

		Player.give(builtRuneItem)
		Player.sound(Sound.BLOCK_ANVIL_HIT)
		Player.sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP) {
			this.volume = .75f
			this.pitch = .75f
		}
		Player.closeInventory()
	}

	fun Update()
	{
		Rune.Power = LineSlots.size.toFloat()
		UpdateStats()
		Gui?.update()
	}

	private fun UpdateStats()
	{
		val statIconMeta = StatsIcon.item.itemMeta
		val statIconLore = statIconMeta.lore()
		statIconLore?.clear()
		Rune.Stats.clear()
		for (slot in Rune.BoardSlots.Slots)
		{
			if (slot is GemSlot)
			{
				slot.Stats.forEach {
					Rune.Stats.add(it)
					statIconLore?.add(Component.text(it.GetStat().GetLoreString(it)))
				}
			}
		}
		statIconMeta.lore(statIconLore)
		StatsIcon.item.itemMeta = statIconMeta
	}

	fun FindLine(x: Int, y: Int)
	{
		LineSlots.clear()
		LineGems.clear()
		val visited = IntOpenHashSet()
		val stack = IntArrayList()
		stack.push(PackCoord(x, y))
		while (!stack.isEmpty)
		{
			val key = stack.popInt()
			val xy = UnpackCoord(key)
			val xx = xy[0]
			val yy = xy[1]
			if (visited.contains(key) || key < 0 || key >= Size) continue
			visited.add(key)

			var skip = true
			val slot = Rune.BoardSlots.Slots[key]
			if (slot is LineSlot)
			{
				LineSlots.add(key)
				skip = false
			}
			else if (slot is GemSlot)
			{
				LineGems.add(key)
				skip = true
			}

			if (skip) continue

			stack.push(PackCoord(xx, yy + 1))
			stack.push(PackCoord(xx + 1, yy))
			stack.push(PackCoord(xx - 1, yy))
			stack.push(PackCoord(xx, yy - 1))
		}
		RuneCraft.Log(Level.INFO, "Line: $LineSlots | $LineGems")
	}

	fun AddInstability(value: Int)
	{
		Rune.Instability += value
		if (Rune.Instability > 9 && GetExplodeChance() < Random.nextFloat()) Destroy()
		else StabilityIcon.item.amount = Rune.Instability

	}

	fun GetExplodeChance(): Float
	{
		return when (Rune.Instability)
		{
			in 0..9 -> 0f
			10 -> .10f
			11 -> .25f
			12 -> .60f
			else -> 1.0f
		}
	}

	fun Destroy()
	{
		RuneItemStack.amount = RuneItemStack.amount - 1
		Player.closeInventory()
		Gui = null
		Player.sound(Sound.ENTITY_GENERIC_EXPLODE) {
			category = SoundCategory.AMBIENT
			volume = .5f
			pitch = .5f
		}
		Player.sendMessage(Component.text("${KColors.RED}Your rune exploded because it's instability was too high."))
	}

	fun SetEmpty(x: Int, y: Int)
	{
		RunePanel.removeItem(x, y)
		Rune.BoardSlots.Slots[PackCoord(x, y)] = EmptySlot()
	}

	fun SetItem(x: Int, y: Int, slot: BoardSlot)
	{
		RunePanel.addItem(slot.GuiItemWrapper.GuiItem.copy(), x, y)
		Rune.BoardSlots.Slots[PackCoord(x, y)] = slot
	}

	fun GetGuiTitle(): String = Gui?.title ?: "NULL"

	private fun CreateRunePanel()
	{
		RunePanel = StaticPane(2, 0, 6, 6)
		RunePanel.setOnClick {
			val slotItem = it.currentItem
			val cursorItem = it.cursor ?: return@setOnClick

			if (slotItem != null || !RuneItems.RuneItemsSet.contains(CustomId.FromItemStack(cursorItem))) return@setOnClick
			it.isCancelled = true
			val x = 5.coerceAtMost(0.coerceAtLeast(it.slot % 9 - 2))
			val y = it.slot / 9
			RuneCraft.Log(Level.INFO, "$x $y")
			val runeItem: RuneItem = CustomItems.GetByItemStack(cursorItem) as RuneItem
			RunePanel.removeItem(x, y)
			Rune.BoardSlots.Slots[PackCoord(x, y)] = runeItem.BoardSlot
			RunePanel.addItem(runeItem.BoardSlot.GuiItemWrapper.GuiItem.copy(), x, y)
			cursorItem.amount = cursorItem.amount - 1
			Gui?.update()
		}
		for (i in 0 until Size)
		{
			val xy = UnpackCoord(i)
			if (Rune.BoardSlots.Slots[i] is EmptySlot) continue
			RunePanel.addItem(Rune.BoardSlots.Slots[i].GuiItemWrapper.GuiItem.copy(), xy[0], xy[1])
		}

		Gui?.addPane(RunePanel)
	}

	private fun CreateSeparatorPanel()
	{
		val leftSeparator = StaticPane(1, 0, 1, 6)
		leftSeparator.fillWith(GuiItems.SeparatorIcon) {
			it.isCancelled = true
		}
		Gui?.addPane(leftSeparator)

		val rightSeparator = StaticPane(8, 0, 1, 6)
		rightSeparator.fillWith(GuiItems.SeparatorIcon) {
			it.isCancelled = true
		}
		Gui?.addPane(rightSeparator)
	}

	private fun CreateHeaderPanel()
	{
		val header = StaticPane(0, 0, 1, 6)
		header.addItem(GuiItems.HelpItem, 0, 0)
		header.addItem(GuiItem(GuiItems.SeparatorIcon) { it.isCancelled = true }, 0, 1)

		StatsIcon = GuiItems.StatsItem.copy()
		header.addItem(StatsIcon, 0, 2)

		StabilityIcon = CreateStabilityItem()
		header.addItem(StabilityIcon, 0, 3)
		header.addItem(GuiItem(GuiItems.SeparatorIcon) { it.isCancelled = true }, 0, 4)
		header.addItem(CreateBuildItem(), 0, 5)
		Gui?.addPane(header)
	}

	private fun CreateStabilityItem(): GuiItem
	{
		val buildSlot = GuiItems.StabilityItem.copy()
		buildSlot.item.amount = Rune.Instability
		return buildSlot
	}

	private fun CreateBuildItem(): GuiItem
	{
		val buildSlot = GuiItems.BuildItem.copy()
		buildSlot.setAction(::OnBuild)
		return buildSlot
	}

}