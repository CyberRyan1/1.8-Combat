package com.github.cyberryan1.combat.features.spawn;

import com.github.cyberryan1.cybercore.utils.CoreUtils;
import org.apache.commons.lang.math.DoubleRange;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class SpawnEvents implements Listener {

    @EventHandler
    public void onPlayerMove( PlayerMoveEvent event ) {
        if ( SpawnCommand.playersSpawning.containsKey( event.getPlayer() ) ) {
            Location startLoc = SpawnCommand.playersLocation.get( event.getPlayer() );
            Location current = event.getPlayer().getLocation();

            if ( ( new DoubleRange( startLoc.getX(), startLoc.getX() + 1 ).containsDouble( current.getX() )
                        || new DoubleRange( startLoc.getX(), startLoc.getX() - 1 ).containsDouble( current.getX() ) )
                    && ( new DoubleRange( startLoc.getY(), startLoc.getY() + 1 ).containsDouble( current.getY() ) )
                    && ( new DoubleRange( startLoc.getZ(), startLoc.getZ() + 1 ).containsDouble( current.getZ() )
                        || new DoubleRange( startLoc.getZ(), startLoc.getZ() - 1 ).containsDouble( current.getZ() ) ) ) {
                return;
            }

            SpawnCommand.playersSpawning.remove( event.getPlayer() );
            SpawnCommand.playersLocation.remove( event.getPlayer() );
            event.getPlayer().sendMessage( CoreUtils.getColored( "&7Your spawn teleport has been cancelled" ) );
        }
    }
}