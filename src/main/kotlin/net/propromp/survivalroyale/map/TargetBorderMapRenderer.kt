package net.propromp.survivalroyale.map

import net.propromp.survivalroyale.SurvivalRoyale
import net.propromp.survivalroyale.SurvivalRoyaleGame
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import kotlin.math.pow

class TargetBorderMapRenderer:MapRenderer() {
    private val writtenPoints = mutableListOf<Pair<Double,Double>>()
    private var oldCenter: Location? = null
    private var oldR:Int? = null
    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        val center = SurvivalRoyaleGame.targetCenterLocation
        val r = SurvivalRoyale.plugin.game.targetSize/2
        if(center==oldCenter&&r==oldR){
            return
        }
        val scale = 2.0.pow(map.scale.ordinal)
        writtenPoints.forEach {
            canvas.setPixel(((it.first/scale)+64).toInt(),
                ((it.second/scale)+64).toInt(),canvas.getBasePixel(((it.first/scale)+64).toInt(),
                    ((it.second/scale)+64).toInt()
                ))
        }
        writtenPoints.clear()
        if(center==null){
            return
        }

        var x = r-1
        var y = r-1
        while(y>=-r){
            writtenPoints.add(Pair(center.x+x,center.z+y))
            y--
        }
        while(x>=-r){
            writtenPoints.add(Pair(center.x+x,center.z+y))
            x--
        }
        while(y<=r-1){
            writtenPoints.add(Pair(center.x+x,center.z+y))
            y++
        }
        while(x<=r-1){
            writtenPoints.add(Pair(center.x+x,center.z+y))
            x++
        }
        writtenPoints.forEach {
            val color = MapPalette.LIGHT_GREEN
            canvas.setPixel((it.first/scale+64).toInt(),(it.second/scale+64).toInt(),color)
        }
        oldCenter=center.clone()
        oldR = r
    }
}