package net.propromp.survivalroyale.map

import com.google.common.collect.Iterables
import com.google.common.collect.LinkedHashMultiset
import com.google.common.collect.Multiset
import com.google.common.collect.Multisets
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Chunk
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView


class SurvivalRoyaleMapRenderer:MapRenderer() {
    companion object {
        val chunks = mutableMapOf<Pair<Int,Int>,Chunk>()
    }
    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        for(x in (map.centerX-512)/16..(map.centerX+512)/16){
            for(z in (map.centerZ-512)/16..(map.centerZ+512)/16){
                chunks[Pair(x,z)]?.let{chunk->
                    val multiset: Multiset<MaterialMapColor> = LinkedHashMultiset.create()
                    for( x2 in 0..15){
                        for(z2 in 0..15){
                            var highest = (chunk as CraftChunk).handle.getHighestBlock(HeightMap.Type.WORLD_SURFACE,x2,z2)
                            var iBlockData: IBlockData
                            do {
                                val position = BlockPosition.MutableBlockPosition(x2+x*15,highest,z2+z*15)
                                iBlockData = (chunk as CraftChunk).handle.getType(position)
                                highest--
                            } while(iBlockData.d((map.world as CraftWorld).handle,BlockPosition(x2+x*15,highest,z2+z*15))==MaterialMapColor.b)
                            multiset.add(iBlockData.d((map.world as CraftWorld).handle,BlockPosition(x2+x*15,highest,z2+z*15)))
                        }
                    }
                    var materialmapcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialMapColor.b)!!
                    val color = materialmapcolor.aj*4


                    // 原点移動&反時計回りに90度回転
                    val drawX = -z + 64
                    val drawY = x + 64
                    if(canvas.getPixel(drawX,drawY)!=(color+2).toByte()) {
                        canvas.setPixel(
                            drawX, drawY,
                            (color + 2).toByte()
                        )
                    }

                }
            }
        }
    }
}