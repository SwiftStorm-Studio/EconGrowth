package net.rk4z.s1.econgrowth.core.utils

import java.util.*
import java.nio.ByteBuffer
import java.util.Base64

/**
 * A utility class for handling UUIDs and their shorter string representations.
 * This class provides methods to generate, convert, and validate UUIDs and their short string forms.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ShortUUID private constructor(
    private val uuid: UUID?,
    private val shortString: String?
) : Comparable<ShortUUID> {

    companion object {
        /**
         * Generates a new ShortUUID with a random UUID.
         * @return A new ShortUUID instance.
         */
        fun randomUUID(): ShortUUID {
            val uuid = UUID.randomUUID()
            return ShortUUID(uuid, null)
        }

        /**
         * Creates a ShortUUID from a standard UUID string.
         * @param uuid The UUID string.
         * @return A new ShortUUID instance.
         */
        fun fromUUIDString(uuid: String): ShortUUID {
            return ShortUUID(UUID.fromString(uuid), null)
        }

        /**
         * Creates a ShortUUID from a short string representation.
         * @param shortString The short string representation of the UUID.
         * @return A new ShortUUID instance.
         */
        fun fromShortString(shortString: String): ShortUUID {
            require(isValidShortString(shortString)) { "Invalid short string for UUID" }
            return ShortUUID(null, shortString)
        }

        /**
         * Validates if a given short string is a valid short UUID.
         * @param shortString The short string to validate.
         * @return True if the short string is valid, false otherwise.
         */
        fun isValidShortString(shortString: String): Boolean {
            return try {
                val bytes = Base64.getUrlDecoder().decode(shortString)
                bytes.size == 16
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        /**
         * Creates a ShortUUID from a byte array.
         * @param byteArray The byte array representing the UUID.
         * @return A new ShortUUID instance.
         */
        fun fromByteArray(byteArray: ByteArray): ShortUUID {
            require(byteArray.size == 16) { "Byte array must be exactly 16 bytes long" }
            val bb = ByteBuffer.wrap(byteArray)
            val high = bb.long
            val low = bb.long
            return ShortUUID(UUID(high, low))
        }

        /**
         * Creates a ShortUUID from various types of input.
         * @param value The input value, which can be a String, UUID, or ShortUUID.
         * @return A new ShortUUID instance or null if the input type is not supported.
         */
        fun fromAny(value: Any): ShortUUID? {
            return when (value) {
                is String -> if (isValidShortString(value)) fromShortString(value) else fromUUIDString(value)
                is UUID -> ShortUUID(value, null)
                is ShortUUID -> value
                else -> null
            }
        }
    }

    // UUID and short string representations cached using lazy initialization
    private val computedUUID: UUID? by lazy { uuid ?: decodeShortStringToUUID() }
    private val computedShortString: String by lazy { shortString ?: encodeUUIDToShortString() }

    private constructor(uuid: UUID) : this(uuid, null)

    /**
     * Converts the ShortUUID to a standard UUID.
     * @return The UUID or null if conversion is not possible.
     */
    fun toUUID(): UUID? = computedUUID

    /**
     * Converts the ShortUUID to its short string representation.
     * @return The short string representation of the UUID.
     */
    fun toShortString(): String = computedShortString

    /**
     * Converts the ShortUUID to a byte array.
     * @return The byte array representation of the UUID.
     */
    fun toByteArray(): ByteArray {
        val bb = ByteBuffer.wrap(ByteArray(16))
        val uuid = computedUUID ?: throw IllegalStateException("UUID is not initialized")
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return bb.array()
    }

    /**
     * Returns the string representation of the ShortUUID.
     * @return The string representation of the UUID or short string.
     */
    override fun toString(): String {
        return computedUUID?.toString() ?: computedShortString
    }

    /**
     * Checks if this ShortUUID is equal to another object.
     * @param other The object to compare with.
     * @return True if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShortUUID) return false
        return this.toUUID() == other.toUUID()
    }

    /**
     * Returns the hash code of the ShortUUID.
     * @return The hash code.
     */
    override fun hashCode(): Int {
        return toUUID()?.hashCode() ?: computedShortString.hashCode()
    }

    /**
     * Compares this ShortUUID with another ShortUUID.
     * @param other The other ShortUUID to compare with.
     * @return A negative integer, zero, or a positive integer as this ShortUUID is less than,
     * equal to, or greater than the specified ShortUUID.
     */
    override fun compareTo(other: ShortUUID): Int {
        return this.toUUID()?.compareTo(other.toUUID()) ?: 0
    }

    // Private helper method to decode short string to UUID
    private fun decodeShortStringToUUID(): UUID? {
        return shortString?.let {
            val bytes = Base64.getUrlDecoder().decode(it)
            val bb = ByteBuffer.wrap(bytes)
            val high = bb.long
            val low = bb.long
            UUID(high, low)
        }
    }

    // Private helper method to encode UUID to short string
    private fun encodeUUIDToShortString(): String {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid!!.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array())
    }
}