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

object BoardSlotsSerializer : KSerializer<BoardSlots>
{
	private val delegateSerializer = ListSerializer(BoardSlotSerializer)
	override val descriptor = SerialDescriptor("ObjectOpenHashSet", delegateSerializer.descriptor)

	override fun serialize(encoder: Encoder, value: BoardSlots)
	{
		encoder.encodeSerializableValue(delegateSerializer, value.Slots)
	}

	override fun deserialize(decoder: Decoder): BoardSlots
	{
		return BoardSlots(decoder.decodeSerializableValue(delegateSerializer) as MutableList<BoardSlot>)
	}
}

object BoardSlotSerializer : KSerializer<BoardSlot>
{
	private val delegateSerializer = ByteArraySerializer()
	override val descriptor = SerialDescriptor("ByteArray", delegateSerializer.descriptor)

	override fun serialize(encoder: Encoder, value: BoardSlot)
	{
		val byte = ByteArrayOutputStream()
		val obj = ObjectOutputStream(byte)
		obj.writeObject(value)
		return encoder.encodeSerializableValue(delegateSerializer, byte.toByteArray())
	}

	override fun deserialize(decoder: Decoder): BoardSlot
	{
		val byte = ByteArrayInputStream(decoder.decodeSerializableValue(delegateSerializer))
		val obj = ObjectInputStream(byte)
		return obj.readObject() as BoardSlot
	}
}