package me.bscal.runecraft

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import me.bscal.runecraft.custom_items.CustomItem
import me.bscal.runecraft.custom_items.CustomItems
import me.bscal.runecraft.stats.Stat
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.*
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import java.util.logging.Level

class RuneBoardTagType : PersistentDataType<ByteArray, Array<BoardSlot>>
{
	override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

	override fun getComplexType(): Class<Array<BoardSlot>> = Array<BoardSlot>::class.java

	override fun toPrimitive(complex: Array<BoardSlot>, context: PersistentDataAdapterContext): ByteArray
	{    //	return ProtoBuf.encodeToByteArray(complex)
		val byte = ByteArrayOutputStream()
		val obj = ObjectOutputStream(byte)
		obj.writeObject(complex)
		return byte.toByteArray()
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Array<BoardSlot>
	{        //return ProtoBuf.decodeFromByteArray(primitive)
		val byte = ByteArrayInputStream(primitive)
		val obj = ObjectInputStream(byte)
		return obj.readObject() as Array<BoardSlot>
	}
}

class RuneItemTagType : PersistentDataType<ByteArray, Rune>
{
	override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

	override fun getComplexType(): Class<Rune> = Rune::class.java

	override fun toPrimitive(complex: Rune, context: PersistentDataAdapterContext): ByteArray
	{
		return ProtoBuf.encodeToByteArray(complex)
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Rune
	{
		return ProtoBuf.decodeFromByteArray(primitive)
	}
}

@Serializable class Rune(val Type: RuneType)
{
	companion object
	{
		val RuneKey = NamespacedKey(RuneCraft.INSTANCE, "rune_data")
		val BoardSlotKey = NamespacedKey(RuneCraft.INSTANCE, "board_slot_data")

		fun Deserialize(itemStack: ItemStack): Rune?
		{
			if (itemStack.hasItemMeta())
			{
				val meta = itemStack.itemMeta
				val rune = meta.persistentDataContainer.get(RuneKey, RuneItemTagType())
				val board = meta.persistentDataContainer.get(BoardSlotKey, RuneBoardTagType())
				if (board != null && board.isNotEmpty()) rune?.SetSlots(
					board.toList() as ArrayList<BoardSlot> /* = java.util.ArrayList<me.bscal.runecraft.BoardSlot> */)                //val slotBuffer = meta.persistentDataContainer.get(BoardSlotKey, PersistentDataType.BYTE_ARRAY)
				//if (slotBuffer != null) {
				//rune?.SetSlots(ProtoBuf.decodeFromByteArray(slotBuffer))
				//}
				return rune
			}
			return null
		}
	}

	var Rarity: Int = 0
	var Instability: Int = 0
	var Color: Int = 0
	var Power: Float = 0f
	var IsGenerated: Boolean = false
	var IsBuilt: Boolean = false

	@Transient
	val Board: RuneBoard = RuneBoard(this, LARGE_RUNE_SIZE)

	fun Open(player: Player, runeItemStack: ItemStack)
	{        // TODO
		Board.Generate(player)
		IsGenerated = true
		Board.Open(player, runeItemStack)
	}

	fun AddRuneToItem(player: Player, itemStack: ItemStack): Boolean
	{
		Board.Stats.StatsSet.forEach {
			it.ApplyToItemStack(itemStack)
		}
		return true
	}

	fun Serialize(itemStack: ItemStack)
	{
		val im = itemStack.itemMeta
		RuneCraft.LogDebug(Level.INFO, "$this")
		im.persistentDataContainer.set(RuneKey, RuneItemTagType(), this)
		im.persistentDataContainer.set(BoardSlotKey, RuneBoardTagType(), Board.Slots.toTypedArray())
		itemStack.itemMeta = im
	}

	fun SetSlots(slots: ArrayList<BoardSlot>)
	{
		Board.Slots = slots
	}

}

@Serializable @JvmRecord data class RuneType(val Name: String)
{
	companion object
	{
		val Overworld = RuneType("Overworld")
		val Default = Overworld
	}
}

// TODO should this be relocated with RuneTools?
val UncarvedRune = UncarvedRuneItem()
val CarvedRune = CarvedRuneItem()

fun RegisterRuneCustomItems()
{
	CustomItems.Register("rc_uncarved_rune", UncarvedRune)
	CustomItems.Register("rc_carved_rune", CarvedRune)
}

class UncarvedRuneItem() : CustomItem(itemStack(Material.END_STONE_BRICKS) {
	meta {
		name = "${KColors.ROSYBROWN}Uncarved Rune"
		customModel = 11000
		addLore {
			+"${KColors.LIGHTSLATEGRAY}A rune which has not been engraved."
			+"${KColors.LIGHTSLATEGRAY}To begin engraving right click in hand."
		}
	}
}, true, InteractCallback = {
	val rune = Rune.Deserialize(it.item!!)
	rune?.Open(it.player, it.item!!)
})
{
	fun NewStack(rune: Rune): ItemStack
	{
		val itemStack = super.NewStack()
		rune.Serialize(itemStack)
		return itemStack
	}
}

class CarvedRuneItem() : CustomItem(itemStack(Material.END_STONE_BRICKS) {
	meta {
		name = "${KColors.ROSYBROWN}Engraved Rune"
		customModel = 11001
		addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1)
		flag(ItemFlag.HIDE_ENCHANTS)
		addLore {
			+"${KColors.LIGHTSLATEGRAY}A rune that is engraved with power."
			+"${KColors.LIGHTSLATEGRAY}These runes can be added to armor/tools."
			+" "

		}
	}
}, true)
{
	fun NewStack(stats: ObjectArrayList<Stat>): ItemStack
	{
		val itemStack = super.NewStack()
		for (stat in stats)
		{
			stat.ApplyToItemStack(itemStack)
			itemStack.editMeta {
				it.addLore {
					+"${KColors.LIGHTSLATEGRAY}${stat.GetLoreString()}"
				}
			}
		}
		return itemStack
	}
}