package com.github.cyberryan1.combat;

import com.github.cyberryan1.combat.features.combattag.CombatTagCommand;
import com.github.cyberryan1.combat.features.combattag.CombatTagEvents;
import com.github.cyberryan1.combat.utils.yml.YMLUtils;
import com.github.cyberryan1.cybercore.CyberCore;
import org.bukkit.plugin.java.JavaPlugin;

public final class Combat extends JavaPlugin {

    @Override
    public void onEnable() {
        CyberCore.setPlugin( this );

        // Update or reload config/data files
        YMLUtils.getConfig().getYMLManager().reloadConfig();
        YMLUtils.getConfig().getYMLManager().updateConfig();

        // Register all commands
        registerAllCommands();
        // Register all events
        registerAllEvents();
    }

    private void registerAllCommands() {
        // /combattag [player]
        CombatTagCommand combatTag = new CombatTagCommand();
        this.getCommand( "combattag" ).setExecutor( combatTag );
        this.getCommand( "combattag" ).setTabCompleter( combatTag );
    }

    private void registerAllEvents() {
        // CombatTagEvents class
        this.getServer().getPluginManager().registerEvents( new CombatTagEvents(), this );
    }
}
