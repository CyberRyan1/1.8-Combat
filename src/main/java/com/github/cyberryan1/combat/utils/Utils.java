package com.github.cyberryan1.combat.utils;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Utils {

    public static WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin( "WorldGuard" );
        if ( plugin instanceof WorldGuardPlugin == false ) { return null; }

        return ( WorldGuardPlugin ) plugin;
    }

}