package com.github.cyberryan1.combat.features.spawn;

import com.github.cyberryan1.combat.features.BaseCommand;
import com.github.cyberryan1.combat.features.combattag.CombatTagEvents;
import com.github.cyberryan1.combat.utils.yml.YMLUtils;
import com.github.cyberryan1.cybercore.CyberCore;
import com.github.cyberryan1.cybercore.utils.CoreUtils;
import com.github.cyberryan1.cybercore.utils.VaultUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class SpawnCommand extends BaseCommand {

    //                    player who is being sent to spawn
    //                            when the player started spawning
    public static HashMap<Player, Long> playersSpawning = new HashMap<>();
    //                    player who is being sent to spawn
    //                            where the player started spawning
    public static HashMap<Player, Location> playersLocation = new HashMap<>();

    private static String SPAWN_OTHERS_PERM = YMLUtils.getConfig().getStr( "commands.spawn.spawn-others" );
    private static int SPAWN_DELAY = YMLUtils.getConfig().getInt( "commands.spawn.delay" );
    private static Location SPAWN_LOC;

    public SpawnCommand() {
        super( "spawn", null, null, null );

        double x = YMLUtils.getConfig().getDouble( "commands.spawn.location.x" );
        double y = YMLUtils.getConfig().getDouble( "commands.spawn.location.y" );
        double z = YMLUtils.getConfig().getDouble( "commands.spawn.location.z" );
        World world = Bukkit.getWorld( YMLUtils.getConfig().getStr( "commands.spawn.location.world" ) );
        SPAWN_LOC = new Location( world, x, y, z );
        SPAWN_LOC.setPitch( YMLUtils.getConfig().getFloat( "commands.spawn.location.pitch" ) );
        SPAWN_LOC.setYaw( YMLUtils.getConfig().getFloat( "commands.spawn.location.yaw" ) );
    }

    @Override
    public List<String> onTabComplete( CommandSender sender, Command command, String label, String[] args ) {
        if ( VaultUtils.hasPerms( sender, SPAWN_OTHERS_PERM ) ) {
            if ( args.length == 0 || args[0].length() == 0 ) {
                return getAllOnlinePlayerNames();
            }
            else if ( args.length == 1 ) {
                return matchOnlinePlayers( args[0] );
            }
        }
        return null;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

        if ( args.length == 0 ) {
            if ( demandPlayer( sender ) == false ) { return true; }

            Player player = ( Player ) sender;
            if ( player.getGameMode() == GameMode.CREATIVE ) {
                player.teleport( SPAWN_LOC );
                return true;
            }

            if ( CombatTagEvents.tags.containsKey( player ) ) {
                player.sendMessage( CoreUtils.getColored( "&7You cannot spawn while in combat!" ) );
                return true;
            }

            playersSpawning.put( player, getCurrentTimestamp() );
            playersLocation.put( player, player.getLocation() );
            player.sendMessage( CoreUtils.getColored( "&7You will be sent to spawn in &c" + SPAWN_DELAY + " seconds" ) );
            doSpawning( player );
        }

        else if ( VaultUtils.hasPerms( sender, SPAWN_OTHERS_PERM ) ) {

        }

        else {
            sender.sendMessage( CoreUtils.getColored( "&cInvalid permissions!" ) );
        }

        return true;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000; // returns it in seconds
    }

    public static long getTimeRemaining( Player player ) {
        if ( playersSpawning.containsKey( player ) == false ) { return -1; }
        long timestamp = System.currentTimeMillis() / 1000;
        return Math.abs( timestamp - playersSpawning.get( player ) - SPAWN_DELAY );
    }

    private void doSpawning( Player player ) {
        if ( playersSpawning.containsKey( player ) == false ) { return; }

        if ( CombatTagEvents.tags.containsKey( player ) ) {
            player.sendMessage( CoreUtils.getColored( "&7Your spawn teleport has been cancelled" ) );
            playersSpawning.remove( player );
            playersLocation.remove( player );
            return;
        }

        if ( getTimeRemaining( player ) <= 0 ) {
            player.teleport( SPAWN_LOC );
            player.sendMessage( CoreUtils.getColored( "&7You have been teleported to spawn" ) );
            playersSpawning.remove( player );
            playersLocation.remove( player );
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously( CyberCore.getPlugin(), () -> {
            doSpawning( player );
        }, 10 );
    }
}
