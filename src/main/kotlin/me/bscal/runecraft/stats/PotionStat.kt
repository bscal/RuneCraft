package me.bscal.runecraft.stats

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.protobuf.ProtoBuf
import me.bscal.runecraft.RuneCraft
import net.axay.kspigot.data.NBTData
import net.axay.kspigot.data.NBTDataType
import org.bukkit.NamespacedKey
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@OptIn(ExperimentalSerializationApi::class) @Serializer(forClass = PotionEffect::class) object PotionEffectSerializer :
	KSerializer<PotionEffect>
{
	override val descriptor: SerialDescriptor
		get() = buildClassSerialDescriptor("PotionEffect") {
			element<Int>("amplifier")
			element<Int>("duration")
			element<String>("typeName")
			element<Boolean>("ambient")
			element<Boolean>("particles")
			element<Boolean?>("icon")
		}

	override fun deserialize(decoder: Decoder): PotionEffect
	{
		decoder.decodeStructure(descriptor) {
			var amplifier: Int = 1
			var duration: Int = 3000
			var typeName: String? = null
			var ambient: Boolean = false
			var particles: Boolean = true
			var icon: Boolean = false

			loop@ while (true)
			{
				when (val i = decodeElementIndex(descriptor))
				{
					CompositeDecoder.DECODE_DONE -> break@loop
					0 -> amplifier = decodeIntElement(descriptor, i)
					1 -> duration = decodeIntElement(descriptor, i)
					2 -> typeName = decodeStringElement(descriptor, i)
					3 -> ambient = decodeBooleanElement(descriptor, i)
					4 -> particles = decodeBooleanElement(descriptor, i)
					5 -> icon = decodeBooleanElement(descriptor, i)
					else -> throw SerializationException("Unknown index $i")
				}
			}

			val type = PotionEffectType.getByName(requireNotNull(typeName))
			return PotionEffect(requireNotNull(type), duration, amplifier, ambient, particles, icon)
		}
	}

	override fun serialize(encoder: Encoder, value: PotionEffect)
	{
		encoder.encodeStructure(descriptor) {
			encodeIntElement(descriptor, 0, value.amplifier)
			encodeIntElement(descriptor, 1, value.duration)
			encodeStringElement(descriptor, 2, value.type.name)
			encodeBooleanElement(descriptor, 3, value.isAmbient)
			encodeBooleanElement(descriptor, 4, value.hasParticles())
			encodeBooleanElement(descriptor, 5, value.hasIcon())
		}
	}

}

class PotionEffectTypeTag : PersistentDataType<ByteArray, PotionEffect>
{
	override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

	override fun getComplexType(): Class<PotionEffect> = PotionEffect::class.java

	override fun toPrimitive(complex: PotionEffect, context: PersistentDataAdapterContext): ByteArray
	{
		return ProtoBuf.encodeToByteArray(PotionEffectSerializer, complex)
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): PotionEffect
	{
		return ProtoBuf.decodeFromByteArray(PotionEffectSerializer, primitive)
	}
}

class PotionStat(namespacedKey: NamespacedKey) : BaseStat(namespacedKey)
{
	companion object
	{
		const val EFFECT_KEY = "effect"
		const val MAX_AMP_KEY = "max_amp"
	}

	override fun CombineInstance(instance: StatInstance, other: StatInstance): Boolean
	{
		if (!IsSame(instance, other)) return false

		val maxAmp = instance.additionalData[MAX_AMP_KEY, NBTDataType.INT] ?: 1
		val newAmp = (instance.Value + other.Value).coerceAtMost(maxAmp.toDouble())
		if (newAmp > instance.Value)
		{
			val bytes = instance.additionalData[EFFECT_KEY, NBTDataType.BYTE_ARRAY]
			if (bytes == null || bytes.isEmpty()) return false
			instance.Value = newAmp
			val effect: PotionEffect = ProtoBuf.decodeFromByteArray(PotionEffectSerializer, bytes)
			val newEffect = PotionEffect(effect.type, effect.duration, newAmp.toInt(), effect.isAmbient, effect.hasParticles())
			instance.additionalData[EFFECT_KEY, NBTDataType.BYTE_ARRAY] = ProtoBuf.encodeToByteArray(PotionEffectSerializer, newEffect)
			return true
		}
		return false
	}

	fun NewStatInstance(effect: PotionEffect, maxAmp: Int = 3): StatInstance
	{
		val nbt = NBTData()
		nbt[MAX_AMP_KEY, NBTDataType.INT] = maxAmp
		nbt[EFFECT_KEY, NBTDataType.BYTE_ARRAY] = ProtoBuf.encodeToByteArray(PotionEffectSerializer, effect)
		return super.NewStatInstance(effect.amplifier.toDouble(), AttributeModifier.Operation.ADD_NUMBER, nbt)
	}

	override fun ApplyToItemStack(instance: StatInstance, itemStack: ItemStack)
	{
		val bytes = instance.additionalData[EFFECT_KEY, NBTDataType.BYTE_ARRAY]
		if (bytes == null || bytes.isEmpty()) return
		val effect: PotionEffect = ProtoBuf.decodeFromByteArray(PotionEffectSerializer, bytes)
		itemStack.editMeta {
			it.persistentDataContainer.set(NamespacedKey(RuneCraft.INSTANCE, effect.type.name), PotionEffectTypeTag(), effect)
		}
	}

	override fun GetLocalName(instance: StatInstance): String
	{
		val bytes = instance.additionalData[EFFECT_KEY, NBTDataType.BYTE_ARRAY]
		if (bytes == null || bytes.isEmpty()) return ""
		val effect: PotionEffect = ProtoBuf.decodeFromByteArray(PotionEffectSerializer, bytes)
		val formattedName = effect.type.name.replace("_", " ")

		return "${formattedName[0]}${formattedName.substring(1).lowercase()}"
	}
}