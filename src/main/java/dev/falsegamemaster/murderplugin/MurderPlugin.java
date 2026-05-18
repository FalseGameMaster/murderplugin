package dev.falsegamemaster.murderplugin;

import dev.falsegamemaster.murderplugin.prop.doors.SWUtilityDoorProp;
import dev.falsegamemaster.murderplugin.prop.npcs.TestNPCProp;
import dev.falsegamemaster.murderplugin.prop.statics.PlayerBodyProp;
import dev.falsegamemaster.propengine.PropEnginePlugin;
import dev.falsegamemaster.propengine.prop.Prop;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.logging.Logger;

public final class MurderPlugin extends JavaPlugin {

    public static Logger LOGGER;
    public static PropEnginePlugin PROP_ENGINE;

    @Nullable
    private PropEnginePlugin requirePropEngine() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("propengine");
        if (plugin instanceof PropEnginePlugin propEngine) return propEngine;
        LOGGER.severe("PropEngine plugin is missing or incompatible.");
        Bukkit.getPluginManager().disablePlugin(this);
        return null;
    }

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        PROP_ENGINE = requirePropEngine();
        if (PROP_ENGINE == null) return;
        Prop.register(PROP_ENGINE.propRegistrar, PlayerBodyProp::new);
        Prop.register(PROP_ENGINE.propRegistrar, SWUtilityDoorProp::new);
        Prop.register(PROP_ENGINE.propRegistrar, TestNPCProp::new);
    }

}
