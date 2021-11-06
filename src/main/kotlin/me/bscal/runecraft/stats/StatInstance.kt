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
import me.bscal.runecraft.RuneCraft
import net.axay.kspigot.data.NBTData
import org.bukkit.NamespacedKey
import org.bukkit.attribute.AttributeModifier
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

/**
 * Stores data about a stat. StatInstance's are not directly linked to any Stat but use an ID to point to a Stat ID
 */
@Serializable(StatInstanceSerializer::class) data class StatInstance(var Id: NamespacedKey, var Value: Double,
	var Operation: AttributeModifier.Operation, var AdditionalData: NBTData) : Externalizable
{
	constructor() : this(NamespacedKey(RuneCraft.INSTANCE, "NULL"), 0.0, AttributeModifier.Operation.ADD_NUMBER, NBTData())

	fun GetStat(): BaseStat = StatRegistry.Registry[Id]!!

	override fun writeExternal(out: ObjectOutput?)
	{
		out?.writeUTF(Id.asString())
		out?.writeDouble(Value)
		out?.writeObject(Operation)
		out?.writeUTF(AdditionalData.serialize())
	}

	override fun readExternal(input: ObjectInput?)
	{
		Id = NamespacedKey.fromString(input?.readUTF()!!)!!
		Value = input.readDouble()
		Operation = input.readObject() as AttributeModifier.Operation
		AdditionalData = NBTData.deserialize(input.readUTF())
	}
}

// ************************************************
// StatInstance Serialization

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
			encodeStringElement(descriptor, 3, value.AdditionalData.serialize())
		}
	}
}