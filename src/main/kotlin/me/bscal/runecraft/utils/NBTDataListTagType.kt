package me.bscal.runecraft.utils

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.protobuf.ProtoBuf
import net.axay.kspigot.data.NBTData
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

class NBTDataListTagType : PersistentDataType<ByteArray, List<NBTData>>
{
	override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

	override fun getComplexType(): Class<List<NBTData>> = List::class.java as Class<List<NBTData>>

	override fun toPrimitive(complex: List<NBTData>, context: PersistentDataAdapterContext): ByteArray
	{
		return ProtoBuf.encodeToByteArray(NBTDataListSerializer, complex)
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): List<NBTData>
	{
		return ProtoBuf.decodeFromByteArray(NBTDataListSerializer, primitive)
	}
}

object NBTDataListSerializer : KSerializer<List<NBTData>>
{
	private val delegateSerializer = ListSerializer(NBTDataSerializer)
	override val descriptor = SerialDescriptor("ObjectOpenHashSet", delegateSerializer.descriptor)

	override fun deserialize(decoder: Decoder): List<NBTData>
	{
		return decoder.decodeSerializableValue(delegateSerializer)
	}

	override fun serialize(encoder: Encoder, value: List<NBTData>)
	{
		encoder.encodeSerializableValue(delegateSerializer, value)
	}
}

object NBTDataSerializer : KSerializer<NBTData>
{
	override val descriptor: SerialDescriptor
		get() = PrimitiveSerialDescriptor("NBTData", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): NBTData
	{
		return NBTData.deserialize(decoder.decodeString())
	}

	override fun serialize(encoder: Encoder, value: NBTData)
	{
		encoder.encodeString(value.serialize())
	}

}