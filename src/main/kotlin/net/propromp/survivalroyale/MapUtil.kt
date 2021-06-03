package net.propromp.survivalroyale

import com.google.common.collect.Iterables
import com.google.common.collect.LinkedHashMultiset
import com.google.common.collect.Multiset
import com.google.common.collect.Multisets
import net.minecraft.server.v1_16_R3.*
import net.minecraft.server.v1_16_R3.BlockPosition.MutableBlockPosition
import net.minecraft.server.v1_16_R3.WorldMap.WorldMapHumanTracker

class MapUtil {
    companion object {
        fun fillMap(itemWorldMap: ItemWorldMap,world:World, entity:Entity,worldmap:WorldMap){
            if (world.getDimensionKey() === worldmap.map && entity is EntityHuman) {
                val i = 1 shl worldmap.scale.toInt()
                val j: Int = worldmap.centerX
                val k: Int = worldmap.centerZ
                val l = MathHelper.floor(entity.locX() - j.toDouble()) / i + 64
                val i1 = MathHelper.floor(entity.locZ() - k.toDouble()) / i + 64
                var j1 = 128 / i
                if (world.getDimensionManager().hasCeiling()) {
                    j1 /= 2
                }
                val worldmap_worldmaphumantracker: WorldMapHumanTracker = worldmap.a(entity as EntityHuman?)
                ++worldmap_worldmaphumantracker.b
                var flag = false
                for (k1 in l - j1 + 1 until l + j1) {
                    if (k1 and 15 == worldmap_worldmaphumantracker.b and 15 || flag) {
                        flag = false
                        var d0 = 0.0
                        for (l1 in i1 - j1 - 1 until i1 + j1) {
//                            if (k1 >= 0 && l1 >= -1 && k1 < 128 && l1 < 128) {
                                val i2 = k1 - l
                                val j2 = l1 - i1
                                val flag1 = i2 * i2 + j2 * j2 > (j1 - 2) * (j1 - 2)
                                val k2 = (j / i + k1 - 64) * i
                                val l2 = (k / i + l1 - 64) * i
                                val multiset: Multiset<MaterialMapColor> = LinkedHashMultiset.create()
                                val chunk: Chunk = world.getChunkAtWorldCoords(BlockPosition(k2, 0, l2))
                                if (!chunk.isEmpty) {
                                    val chunkcoordintpair = chunk.pos
                                    val i3 = k2 and 15
                                    val j3 = l2 and 15
                                    var k3 = 0
                                    var d1 = 0.0
                                    if (world.getDimensionManager().hasCeiling()) {
                                        var l3 = k2 + l2 * 231871
                                        l3 = l3 * l3 * 31287121 + l3 * 11
                                        if (l3 shr 20 and 1 == 0) {
                                            multiset.add(Blocks.DIRT.blockData.d(world, BlockPosition.ZERO), 10)
                                        } else {
                                            multiset.add(Blocks.STONE.blockData.d(world, BlockPosition.ZERO), 100)
                                        }
                                        d1 = 100.0
                                    } else {
                                        val blockposition_mutableblockposition = MutableBlockPosition()
                                        val blockposition_mutableblockposition1 = MutableBlockPosition()
                                        for (i4 in 0 until i) {
                                            for (j4 in 0 until i) {
                                                var k4 = chunk.getHighestBlock(
                                                    HeightMap.Type.WORLD_SURFACE,
                                                    i4 + i3,
                                                    j4 + j3
                                                ) + 1
                                                var iblockdata: IBlockData
                                                if (k4 <= 1) {
                                                    iblockdata = Blocks.BEDROCK.blockData
                                                } else {
                                                    do {
                                                        --k4
                                                        blockposition_mutableblockposition.d(
                                                            chunkcoordintpair.d() + i4 + i3,
                                                            k4,
                                                            chunkcoordintpair.e() + j4 + j3
                                                        )
                                                        iblockdata = chunk.getType(blockposition_mutableblockposition)
                                                    } while (iblockdata.d(
                                                            world,
                                                            blockposition_mutableblockposition
                                                        ) === MaterialMapColor.b && k4 > 0
                                                    )
                                                    if (k4 > 0 && !iblockdata.fluid.isEmpty) {
                                                        var l4 = k4 - 1
                                                        blockposition_mutableblockposition1.g(
                                                            blockposition_mutableblockposition
                                                        )
                                                        var iblockdata1: IBlockData
                                                        do {
                                                            blockposition_mutableblockposition1.p(l4--)
                                                            iblockdata1 =
                                                                chunk.getType(blockposition_mutableblockposition1)
                                                            ++k3
                                                        } while (l4 > 0 && !iblockdata1.fluid.isEmpty)
                                                        val itemWorldMapAMethod = ItemWorldMap::class.java.getDeclaredMethod("a",World::class.java,IBlockData::class.java,BlockPosition::class.java)
                                                        itemWorldMapAMethod.isAccessible=true
                                                        iblockdata = itemWorldMapAMethod.invoke(itemWorldMap,world as World?,
                                                            iblockdata as IBlockData,
                                                            blockposition_mutableblockposition as BlockPosition) as IBlockData
                                                    }
                                                }
                                                worldmap.a(
                                                    world,
                                                    chunkcoordintpair.d() + i4 + i3,
                                                    chunkcoordintpair.e() + j4 + j3
                                                )
                                                d1 += k4.toDouble() / (i * i).toDouble()
                                                multiset.add(iblockdata.d(world, blockposition_mutableblockposition))
                                            }
                                        }
                                    }
                                    k3 /= i * i
                                    var d2 =
                                        (d1 - d0) * 4.0 / (i + 4).toDouble() + ((k1 + l1 and 1).toDouble() - 0.5) * 0.4
                                    var b0: Byte = 1
                                    if (d2 > 0.6) {
                                        b0 = 2
                                    }
                                    if (d2 < -0.6) {
                                        b0 = 0
                                    }
                                    val materialmapcolor = Iterables.getFirst(
                                        Multisets.copyHighestCountFirst(multiset),
                                        MaterialMapColor.b
                                    )
                                    if (materialmapcolor === MaterialMapColor.n) {
                                        d2 = k3.toDouble() * 0.1 + (k1 + l1 and 1).toDouble() * 0.2
                                        b0 = 1
                                        if (d2 < 0.5) {
                                            b0 = 2
                                        }
                                        if (d2 > 0.9) {
                                            b0 = 0
                                        }
                                    }
                                    d0 = d1
                                    if (l1 >= 0 && i2 * i2 + j2 * j2 < j1 * j1 && (!flag1 || k1 + l1 and 1 != 0)) {
                                        val b1: Byte = worldmap.colors.get(k1 + l1 * 128)
                                        val b2 = (materialmapcolor!!.aj * 4 + b0).toByte()
                                        if (b1 != b2) {
                                            worldmap.colors[k1 + l1 * 128] = b2
                                            worldmap.flagDirty(k1, l1)
                                            flag = true
                                        }
                                    }
                                }
//                            }
                        }
                    }
                }
            }
        }
    }
}