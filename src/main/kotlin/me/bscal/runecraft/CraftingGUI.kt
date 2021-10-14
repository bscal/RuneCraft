package me.bscal.runecraft

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.bscal.runecraft.stats.RuneStats
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.logging.Level
import kotlin.collections.ArrayList
import kotlin.random.Random

const val SMALL_RUNE_SIZE = 4 * 4
const val LARGE_RUNE_SIZE = 6 * 6

val RuneBoardCache = Object2ObjectOpenHashMap<UUID, RuneBoard>()

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

	lateinit var Slots: ObjectArrayList<BoardSlot>

	private lateinit var Gui: ChestGui
	private lateinit var RunePanel: StaticPane
	private lateinit var StabilityIcon: GuiItem
	private lateinit var StatsIcon: GuiItem
	private var Generator: BoardGenerator? = null

	private val Stats by lazy {
		RuneStats()
	}
	private val LineSlots: IntArrayList = IntArrayList()
	private val LineGems: IntArrayList = IntArrayList()

	constructor(rune: Rune, size: Int, slots: ObjectArrayList<BoardSlot>) : this(rune, size)
	{
		Slots = slots
	}

	fun Generate(player: Player): Boolean
	{
		if (this::Slots.isInitialized) return false
		Generator = BoardRegistry.Registry[Rune.Type] ?: BoardRegistry.Default
		Slots = ObjectArrayList<BoardSlot>(Size)
		Generator?.Generate(player, this, Rune.Rarity)
		return true
	}

	fun Open(player: Player)
	{
		if (this::Gui.isInitialized) Gui.show(player)

		Generate(player)
		Gui = ChestGui(6, "${KColors.RED}RuneCraft")
		Gui.setOnClose {
			RuneBoardCache.remove(player.uniqueId, this)
		}
		Gui.setOnTopClick {
			it.isCancelled = true
		}

		CreateHeaderPanel()
		CreateSeparatorPanel()
		CreateRunePanel()

		RuneBoardCache[player.uniqueId] = this
		Gui.show(player)
	}

	fun CanBreak(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean
	{
		return slot.BreakLevel != BreakLevel.UNBREAKABLE && tool.Level.Id >= slot.BreakLevel.Id
	}

	fun OnBreak(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
		tool.Deincremeant(itemStack)
		RemoveItem(x, y)
		AddItem(x, y, LineSlot(Material.WHITE_DYE))
		Gui.update()
		AddInstability(slot.GetInstabilityLost())
		FindLine(x, y)
		Update()
	}

	fun OnBuild(event: InventoryClickEvent)
	{
		event.isCancelled = true
		Rune.IsBuilt = true
	}

	fun Update()
	{
		Rune.Power = LineSlots.size.toFloat()

		GuiItems.StatsItem.item.let {
			it.amount = Rune.Power.toInt()
			it.meta {
				addLore {
					for (stat in Stats.StatMap)
						+stat.value.GetLoreString()
				}
			}
		}
	}

	fun Serialize()
	{

	}

	fun Deserialize()
	{

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
			val slot = Slots[key]
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
	{        // TODO
	}

	fun RemoveItem(x: Int, y: Int)
	{		//Slots.remove(x or (y shl 16))
		RunePanel.removeItem(x, y)
	}

	fun AddItem(x: Int, y: Int, slot: BoardSlot)
	{
		Slots[PackCoord(x, y)] = slot
		RunePanel.addItem(slot.Item, x, y)
	}

	fun GetGuiTitle(): String = Gui.title

	private fun CreateRunePanel()
	{
		RunePanel = StaticPane(2, 0, 6, 6)
		for (i in 0 until Slots.size)
		{
			val xy = UnpackCoord(i)
			RunePanel.addItem(Slots[i].Item.copy(), xy[0], xy[1])
		}
		Gui.addPane(RunePanel)
	}

	private fun CreateSeparatorPanel()
	{
		val leftSeparator = StaticPane(1, 0, 1, 6)
		leftSeparator.fillWith(GuiItems.SeparatorIcon) {
			it.isCancelled = true
		}
		Gui.addPane(leftSeparator)

		val rightSeparator = StaticPane(8, 0, 1, 6)
		rightSeparator.fillWith(GuiItems.SeparatorIcon) {
			it.isCancelled = true
		}
		Gui.addPane(rightSeparator)
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
		Gui.addPane(header)
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

object GuiItems
{
	private const val BLANK = ""

	val SeparatorIcon = itemStack(Material.BLACK_STAINED_GLASS_PANE) {
		meta {
			name = BLANK
		}
	}

	val HelpItem = GuiItem(itemStack(Material.BOOK) {
		meta {
			name = "${KColors.GREENYELLOW}Help"
			addLore {

			}
		}
	}) {
		it.isCancelled = true
	}

	val StatsItem = GuiItem(itemStack(Material.LECTERN) {
		meta {
			name = "${KColors.ALICEBLUE}Stats"
		}
	}) {
		it.isCancelled = true
	}

	val StabilityItem = GuiItem(itemStack(Material.TNT) {
		meta {
			name = "${KColors.DARKRED}Instability"
			addLore {
				+"${KColors.RED}Instability is number that shows how"
				+"${KColors.RED}close the rune is to being destroyed."
				+"${KColors.RED}Most moves will increase instability."
				+"${KColors.RED}Instability above 9 will have increased"
				+"${KColors.RED}odds of being destroyed."
				+" "
				+"${KColors.RED}0-9 = 0%"
				+"${KColors.RED}10  = 10%"
				+"${KColors.RED}11  = 25%"
				+"${KColors.RED}12  = 60%"
			}
		}
	}) {
		it.isCancelled = true
	}

	val BuildItem = GuiItem(itemStack(Material.ANVIL) {
		meta {
			name = "${KColors.DARKGREEN}Build"
		}
	})
}