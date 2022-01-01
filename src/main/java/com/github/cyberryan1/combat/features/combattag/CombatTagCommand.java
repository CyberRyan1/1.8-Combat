package com.github.cyberryan1.combat.features.combattag;

import com.github.cyberryan1.combat.features.BaseCommand;
import com.github.cyberryan1.cybercore.utils.CoreUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CombatTagCommand extends BaseCommand {

    public CombatTagCommand() {
        super( "combattag", null, null, null );
    }

    @Override
    public List<String> onTabComplete( CommandSender sender, Command command, String label, String[] args ) {
        if ( args.length == 1 ) {
            return matchOnlinePlayers( args[0] );
        }
        else if ( args.length == 0 ) {
            return getAllOnlinePlayerNames();
        }

        return null;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

        if ( demandPlayer( sender ) == false ) { return true; }
        Player player = ( Player ) sender;

        if ( args.length == 0 ) {
            if ( CombatTagEvents.tags.containsKey( player ) ) {
                long timeRemaining = CombatTagEvents.getTimeRemaining( player );
                if ( timeRemaining != 1 ) {
                    player.sendMessage(
                            CoreUtils.getColored( "&7Your combat time remaining: &b" + timeRemaining + " seconds" ) );
                }
                else {
                    player.sendMessage(
                            CoreUtils.getColored( "&7Your combat time remaining: &b" + timeRemaining + " second" ) );
                }
            }
            else {
                player.sendMessage( CoreUtils.getColored( "&7You are not in combat" ) );
            }
        }

        else if ( CoreUtils.isValidUsername( args[0] ) ) {
            Player target = Bukkit.getPlayer( args[0] );
            if ( target != null ) {
                if ( CombatTagEvents.tags.containsKey( target ) ) {
                    long timeRemaining = CombatTagEvents.getTimeRemaining( target );
                    if ( timeRemaining != 1 ) {
                        player.sendMessage(
                                CoreUtils.getColored( "&b" + target.getName()
                                        + "&7's combat time remaining: &b" + timeRemaining + " seconds" ) );
                    }
                    else {
                        player.sendMessage(
                                CoreUtils.getColored( "&b" + target.getName()
                                        + "&7's combat time remaining: &b" + timeRemaining + " second" ) );
                    }
                }

                else {
                    player.sendMessage( CoreUtils.getColored( "&b" + target.getName() + " &7is not in combat" ) );
                }
            }

            else {
                sendInvalidPlayerArg( sender, args[0] );
            }
        }

        else {
            sendInvalidPlayerArg( sender, args[0] );
        }

        return false;
    }
}