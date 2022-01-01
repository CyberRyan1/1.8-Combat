package com.github.cyberryan1.combat.features.combattag;

import com.github.cyberryan1.combat.utils.Utils;
import com.github.cyberryan1.combat.utils.yml.YMLUtils;
import com.github.cyberryan1.cybercore.CyberCore;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CombatTagEvents implements Listener {

    //             Player in combat
    //                     Timestamp (seconds) they were last tagged
    public static HashMap<Player, Long> tags = new HashMap<>();
    public static final int TAG_TIME = YMLUtils.getConfig().getInt( "features.combattag.length" );
    public static List<String> combatLogBypass = new ArrayList<>();

    //                     Player in combat
    //                                  List of all other players they have tagged
    private static HashMap<Player, List<Player>> tagsToPlayers = new HashMap<>();

    public CombatTagEvents() {
        Collections.addAll( combatLogBypass, YMLUtils.getConfig().getStrList( "features.combattag.bypass" ) );
    }

    @EventHandler
    public void onEntityDamageByEntityEvent( EntityDamageByEntityEvent event ) {
        if ( event.getEntity() instanceof Player && event.getDamager() instanceof Player ) {
            Player attacker = ( Player ) event.getDamager();
            Player victim = ( Player ) event.getEntity();

            if ( Utils.getRegions( attacker.getLocation() ).stream()
                    .anyMatch( ( r1 ) -> (
                                    r1.getFlag( Flags.PVP ) == StateFlag.State.DENY
                            )
                    )
                    || Utils.getRegions( victim.getLocation() ).stream()
                    .anyMatch( ( r1 ) -> (
                                    r1.getFlag( Flags.PVP ) == StateFlag.State.DENY
                            )
                    )
            ) { return; }

            long timestamp = System.currentTimeMillis() / 1000; // gets the timestamp in seconds instead of milliseconds

            if ( attacker.getGameMode() == GameMode.CREATIVE ) { return; }
            if ( attacker != null && tags.containsKey( attacker ) == false ) {
                tags.put( attacker, timestamp );
                doCombatTimer( attacker );
                attacker.sendMessage( YMLUtils.getConfig().getColoredStr( "features.combattag.enter-msg" ) );

                List<Player> list = new ArrayList<>();
                list.add( victim );
                tagsToPlayers.put( attacker, list );
            }
            else if ( attacker != null ) {
                tags.put( attacker, timestamp );
                if ( tagsToPlayers.get( attacker ).contains( victim ) == false ) {
                    tagsToPlayers.get( attacker ).add( victim );
                }
            }

            if ( victim != null && tags.containsKey( victim ) == false ) {
                tags.put( victim, timestamp );
                doCombatTimer( victim );
                victim.sendMessage( YMLUtils.getConfig().getColoredStr( "features.combattag.enter-msg" ) );

                List<Player> list = new ArrayList<>();
                list.add( attacker );
                tagsToPlayers.put( victim, list );
            }
            else if ( victim != null ){
                tags.put( victim, timestamp );
                if ( tagsToPlayers.get( victim ).contains( attacker ) == false ) {
                    tagsToPlayers.get( victim ).add( attacker );
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit( PlayerQuitEvent event ) {
        if ( tags.containsKey( event.getPlayer() ) ) {
            if ( combatLogBypass.contains( event.getPlayer().getName() ) ) { return; }

            event.getPlayer().setHealth( 0 );
            Bukkit.broadcastMessage( YMLUtils.getConfig().getColoredStr( "features.combattag.combatlog-broadcast" )
                    .replace( "[PLAYER]", event.getPlayer().getName() ) );

            Bukkit.getScheduler().runTaskLaterAsynchronously( CyberCore.getPlugin(), () -> {
                tags.remove( event.getPlayer() );
            }, 2 );
        }
    }

    @EventHandler
    public void onPlayerDeathEvent( PlayerDeathEvent event ) {
        if ( tags.containsKey( event.getEntity() ) ) {
            tags.remove( event.getEntity() );
            Player victim = event.getEntity();
            for ( Player otherPlayer : tagsToPlayers.keySet() ) {
                tagsToPlayers.get( otherPlayer ).remove( victim );
            }
        }
    }

    public static long getTimeRemaining( Player player ) {
        if ( tags.containsKey( player ) == false ) { return -1; }
        long timestamp = System.currentTimeMillis() / 1000;
        return Math.abs( timestamp - tags.get( player ) - TAG_TIME );
    }

    private void doCombatTimer( Player player ) {
        if ( tags.containsKey( player ) == false ) { return; }
        long timeSince = getTimeRemaining( player );

        if ( timeSince <= 0 ) {
            tags.remove( player );
            player.sendMessage( YMLUtils.getConfig().getColoredStr( "features.combattag.exit-msg" ) );

            if ( tagsToPlayers.containsKey( player ) ) {
                if ( tagsToPlayers.get( player ) == null ) { return; }
                for ( int index = tagsToPlayers.get( player ).size() - 1; index >= 0 ; index-- ) {
                    Player otherPlayer = tagsToPlayers.get( player ).get( index );
                    tagsToPlayers.get( otherPlayer ).remove( player );
                }

                tagsToPlayers.remove( player );
            }
        }

        else {
            Bukkit.getScheduler().runTaskLaterAsynchronously( CyberCore.getPlugin(), () -> {
                doCombatTimer( player );
            }, 10 );
        }
    }

}