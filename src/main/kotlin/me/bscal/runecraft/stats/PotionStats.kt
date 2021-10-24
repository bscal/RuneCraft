package me.bscal.runecraft.stats

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.protobuf.ProtoBuf
import me.bscal.runecraft.RuneCraft
import net.md_5.bungee.api.ChatColor
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
		return ProtoBuf.encodeToByteArray(complex)
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): PotionEffect
	{
		return ProtoBuf.decodeFromByteArray(primitive)
	}
}

class PotionStats(val PotionType: PotionEffectType, var Amplifier: Int, name: String, value: Double, operation: AttributeModifier.Operation,
	color: ChatColor) : Stat(name, value, operation, color)
{
	override fun ApplyToItemStack(itemStack: ItemStack): Boolean
	{
		itemStack.editMeta {
			it.persistentDataContainer.set(NamespacedKey(RuneCraft.INSTANCE, PotionType.name), PotionEffectTypeTag(),
				PotionEffect(PotionType, 3000, Amplifier))
		}
		return true
	}

	override fun Combine(other: Stat)
	{
		other as PotionStats
		Amplifier += other.Amplifier
	}
}