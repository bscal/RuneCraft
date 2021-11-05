package me.bscal.runecraft.gui.runeboard

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.bscal.runecraft.gui.runeboard.slots.BoardSlot
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

@Serializable(BoardSlotsSerializer::class) data class BoardSlots(var Slots: MutableList<BoardSlot> = ArrayList())

object BoardSlotsSerializer : KSerializer<List<BoardSlot>>
{
	private val delegateSerializer = ListSerializer(BoardSlotSerializer)
	override val descriptor = SerialDescriptor("ObjectOpenHashSet", delegateSerializer.descriptor)

	override fun serialize(encoder: Encoder, value: List<BoardSlot>)
	{
		encoder.encodeSerializableValue(delegateSerializer, value)
	}

	override fun deserialize(decoder: Decoder): List<BoardSlot>
	{
		return decoder.decodeSerializableValue(delegateSerializer)
	}

}

object BoardSlotSerializer : KSerializer<BoardSlot>
{
	override val descriptor = ByteArraySerializer().descriptor

	override fun serialize(encoder: Encoder, value: BoardSlot)
	{
		val byte = ByteArrayOutputStream()
		val obj = ObjectOutputStream(byte)
		obj.writeObject(value)
		return encoder.encodeSerializableValue(ByteArraySerializer(), byte.toByteArray())
	}

	override fun deserialize(decoder: Decoder): BoardSlot
	{
		val byte = ByteArrayInputStream(decoder.decodeSerializableValue(ByteArraySerializer()))
		val obj = ObjectInputStream(byte)
		return obj.readObject() as BoardSlot
	}
}