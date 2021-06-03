package net.propromp.survivalroyale.map

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import kotlin.math.pow
import kotlin.math.roundToInt

class WorldBorderMapRenderer : MapRenderer() {
    var writtenPoints = mutableListOf<Pair<Double, Double>>()
    var colors = mutableMapOf<Pair<Int,Int>,Byte?>()
    var isDrawing = false
    fun update(map: MapView){
        Thread {
            isDrawing=true
            val scale = 2.0.pow(map.scale.ordinal)
            val border = map.world!!.worldBorder
            val writingPoints = mutableListOf<Pair<Double, Double>>()
            val r = border.size / 2
            val centerX = border.center.x
            val centerZ = border.center.z
            var x = r - 1
            var y = r - 1
            for (i in -r.toInt()..y.toInt()) {
                writingPoints.add(Pair(centerX + x, centerZ + i))
            }
            y = -r
            for (i in -r.toInt()..x.toInt()) {
                writingPoints.add(Pair(centerX + i, centerZ + y))
            }
            x = -r
            for (i in y.toInt()..(r - 1).toInt()) {
                writingPoints.add(Pair(centerX + x, centerZ + i))
            }
            y = r - 1
            for (i in x.toInt()..(r - 1).toInt()) {
                writingPoints.add(Pair(centerX + i, centerZ + y))
            }
            writtenPoints.forEach {
                colors[Pair((it.first / scale + 64).toInt(),(it.second / scale + 64).toInt())]=null
            }
            writingPoints.forEach {
                val color = if (writtenPoints.contains(it)) {
                    MapPalette.PALE_BLUE
                } else {
                    MapPalette.RED
                }
                colors[Pair((it.first / scale + 64).toInt(), (it.second / scale + 64).toInt())]=color
            }
            writtenPoints=ArrayList(writingPoints)
            isDrawing=false
        }.start()
    }
    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        if(!isDrawing){
            colors.forEach { (posPair, color) ->
                if (color == null) {
                    canvas.setPixel(posPair.first, posPair.second, canvas.getBasePixel(posPair.first, posPair.second))
                } else {
                    canvas.setPixel(posPair.first, posPair.second, color)
                }
            }
            update(map)
        }
    }
}