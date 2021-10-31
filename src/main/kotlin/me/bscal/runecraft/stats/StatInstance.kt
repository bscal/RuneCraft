package me.bscal.runecraft.stats

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.protobuf.ProtoBuf
import net.axay.kspigot.data.NBTData
import org.bukkit.NamespacedKey
import org.bukkit.attribute.AttributeModifier
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

@Serializable(StatInstanceSerializer::class) data class StatInstance(val Id: NamespacedKey, var Value: Double,
	val Operation: AttributeModifier.Operation, var additionalData: NBTData)
{
	fun GetStat(): BaseStat = StatRegistry.Registry[Id]!!
}

class StatInstanceListTagType : PersistentDataType<ByteArray, List<StatInstance>>
{
	override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

	override fun getComplexType(): Class<List<StatInstance>> = List::class.java as Class<List<StatInstance>>

	override fun toPrimitive(complex: List<StatInstance>, context: PersistentDataAdapterContext): ByteArray
	{
		return ProtoBuf.encodeToByteArray(StatInstanceListSerializer, complex)
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): List<StatInstance>
	{
		return ProtoBuf.decodeFromByteArray(StatInstanceListSerializer, primitive)
	}
}

object StatInstanceListSerializer : KSerializer<List<StatInstance>>
{
	private val delegateSerializer = ListSerializer(StatInstanceSerializer)
	override val descriptor = SerialDescriptor("ObjectOpenHashSet", delegateSerializer.descriptor)

	override fun deserialize(decoder: Decoder): List<StatInstance>
	{
		return decoder.decodeSerializableValue(delegateSerializer)
	}

	override fun serialize(encoder: Encoder, value: List<StatInstance>)
	{
		encoder.encodeSerializableValue(delegateSerializer, value)
	}
}

object StatInstanceSerializer : KSerializer<StatInstance>
{
	override val descriptor: SerialDescriptor
		get() = buildClassSerialDescriptor("StatInstance") {
			element<String>("namespacedKey")
			element<Double>("value")
			element<String>("operation")
			element<String>("additionalData")
		}

	override fun deserialize(decoder: Decoder): StatInstance
	{
		decoder.decodeStructure(descriptor) {
			lateinit var namespacedKey: String
			var value: Double = 0.0
			lateinit var operation: String
			lateinit var additionalData: String

			loop@ while (true)
			{
				when (val i = decodeElementIndex(descriptor))
				{
					CompositeDecoder.DECODE_DONE -> break@loop
					0 -> namespacedKey = decodeStringElement(descriptor, i)
					1 -> value = decodeDoubleElement(descriptor, i)
					2 -> operation = decodeStringElement(descriptor, i)
					3 -> additionalData = decodeStringElement(descriptor, i)
					else -> throw SerializationException("Unknown index $i")
				}
			}
			val id = NamespacedKey.fromString(namespacedKey)
			val attrOperation = AttributeModifier.Operation.valueOf(operation)
			val data = NBTData.deserialize(additionalData)
			return StatInstance(id!!, value, attrOperation, data)
		}
	}

	override fun serialize(encoder: Encoder, value: StatInstance)
	{
		encoder.encodeStructure(descriptor) {
			encodeStringElement(descriptor, 0, value.Id.asString())
			encodeDoubleElement(descriptor, 1, value.Value)
			encodeStringElement(descriptor, 2, value.Operation.name)
			encodeStringElement(descriptor, 3, value.additionalData.serialize())
		}
	}
}