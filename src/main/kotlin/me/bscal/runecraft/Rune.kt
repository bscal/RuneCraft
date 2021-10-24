package me.bscal.runecraft

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import me.bscal.runecraft.gui.BoardSlot
import me.bscal.runecraft.gui.LARGE_RUNE_SIZE
import me.bscal.runecraft.gui.RuneBoard
import net.axay.kspigot.items.*
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import java.util.logging.Level

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
					board.toList() as ArrayList<BoardSlot> /* = java.util.ArrayList<me.bscal.runecraft.gui.BoardSlot> */)                //val slotBuffer = meta.persistentDataContainer.get(BoardSlotKey, PersistentDataType.BYTE_ARRAY)
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

	@Transient val Board: RuneBoard = RuneBoard(this, LARGE_RUNE_SIZE)

	fun Open(player: Player, runeItemStack: ItemStack)
	{
		//TODO
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