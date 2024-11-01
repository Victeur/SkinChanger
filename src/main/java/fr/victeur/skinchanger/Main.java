package fr.victeur.skinchanger;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("skin").setExecutor(new SkinCommand());
    }

}
