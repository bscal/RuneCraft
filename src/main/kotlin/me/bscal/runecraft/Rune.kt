package me.bscal.runecraft

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.serialization.*
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.*
import kotlinx.serialization.protobuf.ProtoBuf
import me.bscal.runecraft.gui.runeboard.BoardSlots
import me.bscal.runecraft.gui.runeboard.LARGE_RUNE_SIZE
import me.bscal.runecraft.gui.runeboard.RuneBoard
import me.bscal.runecraft.gui.runeboard.slots.BoardSlot
import me.bscal.runecraft.stats.StatInstance
import me.bscal.runecraft.utils.getRuneData
import me.bscal.runecraft.utils.setRuneData
import net.axay.kspigot.items.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

@Serializable class Rune(val Type: RuneType)
{
	var Rarity: Int = 0
	var Instability: Int = 1
	var Color: Int = 0
	var Power: Float = 0f
	var IsGenerated: Boolean = false
	var IsBuilt: Boolean = false

	@Serializable(ObjectHashSetSerializer::class) var Stats: ObjectOpenHashSet<StatInstance> = ObjectOpenHashSet()
	@Serializable var BoardSlots = BoardSlots()

	@Transient var Board: RuneBoard = RuneBoard(this, LARGE_RUNE_SIZE)

	fun Open(player: Player, runeItemStack: ItemStack)
	{
		Board.Generate(player)
		Board.Open(player, runeItemStack)
	}

	/**
	 * Applies all stats from a built ruin to an itemstack
	 */
	fun AddRuneToItem(player: Player, itemStack: ItemStack): Boolean
	{
		if (!IsBuilt) return false
		Stats.forEach {
			it.GetStat().ApplyToItemStack(it, itemStack)
		}
		return true
	}

	/**
	 * Sets the Rune Item's rune data. Only for use for Rune itemstack and not to add a rune's stats to and itemstack.
	 * A shortcut for ItemStack.ItemMeta.setRuneData
	 */
	fun Serialize(itemStack: ItemStack)
	{
		itemStack.editMeta {
			it.setRuneData(this)
		}
	}

	companion object
	{
		fun Deserialize(itemStack: ItemStack): Rune?
		{
			if (itemStack.hasItemMeta())
			{
				val meta = itemStack.itemMeta
				val rune = meta.getRuneData()
				if (rune != null)
				{
					rune.Board = RuneBoard(rune, LARGE_RUNE_SIZE)
					return rune
				}
			}
			return null
		}
	}
}

// ************************************************
// Rune Serialization

@Serializable data class RuneType(val Name: String)
{
	companion object
	{
		val Overworld = RuneType("Overworld")
		val Default = Overworld
	}
}

object ObjectHashSetSerializer : KSerializer<ObjectOpenHashSet<StatInstance>>
{
	private val delegateSerializer = SetSerializer(StatInstance.serializer())
	override val descriptor = SerialDescriptor("ObjectOpenHashSet", delegateSerializer.descriptor)

	override fun serialize(encoder: Encoder, value: ObjectOpenHashSet<StatInstance>)
	{
		encoder.encodeSerializableValue(delegateSerializer, value)
	}

	override fun deserialize(decoder: Decoder): ObjectOpenHashSet<StatInstance>
	{
		return ObjectOpenHashSet(decoder.decodeSerializableValue(delegateSerializer))
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