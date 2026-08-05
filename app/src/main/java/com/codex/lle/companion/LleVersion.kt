package com.codex.lle.companion

class LleVersion private constructor(private val parts: List<Long>) : Comparable<LleVersion> {
    override fun compareTo(other: LleVersion): Int {
        for (index in parts.indices) {
            val comparison = parts[index].compareTo(other.parts[index])
            if (comparison != 0) return comparison
        }
        return 0
    }

    override fun toString(): String = parts.joinToString(".")

    override fun equals(other: Any?): Boolean = other is LleVersion && parts == other.parts

    override fun hashCode(): Int = parts.hashCode()

    companion object {
        private val format = Regex("^(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)$")

        fun parse(value: String): LleVersion? {
            val normalized = value.trim().removePrefix("v")
            if (!format.matches(normalized)) return null
            val parts = normalized.split('.').map { it.toLongOrNull() ?: return null }
            return LleVersion(parts)
        }
    }
}
