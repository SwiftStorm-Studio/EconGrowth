package net.ririfa.econgrowth.paper.utils

/**
 * Utility object for calculating and managing 16x16 chunk groups.
 * A chunk group is defined as a group of chunks that fit within
 * a 16x16 chunk grid in a Minecraft world.
 */
object ChunkGroupCalculator {
    /**
     * Calculates the group ID for a given chunk coordinate.
     *
     * The group ID is based on 16x16 chunk grids, where the top-left
     * chunk in each group defines the group coordinates `(groupX, groupZ)`.
     *
     * @param chunkX The X coordinate of the chunk.
     * @param chunkZ The Z coordinate of the chunk.
     * @return A [Pair] representing the group ID as `(groupX, groupZ)`.
     */
    fun getGroupID(chunkX: Int, chunkZ: Int): Pair<Int, Int> {
        val groupX = kotlin.math.floor(chunkX / 16.0).toInt()
        val groupZ = kotlin.math.floor(chunkZ / 16.0).toInt()

        return Pair(groupX, groupZ)
    }

    /**
     * Gets a list of all chunk coordinates within a specific group.
     *
     * The group coordinates `(groupX, groupZ)` determine the 16x16 chunk grid,
     * and this function calculates all chunk coordinates within that grid.
     *
     * @param groupX The X coordinate of the chunk group.
     * @param groupZ The Z coordinate of the chunk group.
     * @return A [List] of [Pair]s, where each pair represents a chunk's `(chunkX, chunkZ)` coordinates.
     */
    fun getChunksInGroup(groupX: Int, groupZ: Int): List<Pair<Int, Int>> {
        return (0 until 16).flatMap { x ->
            (0 until 16).map { z ->
                Pair(groupX * 16 + x, groupZ * 16 + z)
            }
        }
    }
}
