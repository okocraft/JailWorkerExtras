package net.okocraft.jailworkerextras;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.github.siroshun09.punishmentlistener.event.PunishmentEvent;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import fr.alienationgaming.jailworker.config.JailConfig;
import fr.alienationgaming.jailworker.config.Prisoners;
import fr.alienationgaming.jailworker.config.WantedPlayers;

public class JailWorkerExtras extends JavaPlugin implements Listener {

	FileConfiguration config;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = getConfig();

		Bukkit.getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Listener) this);
	};

    @EventHandler
    public void onPunishment(PunishmentEvent event) {
        if (!event.getType().equals("warn")) {
            return;
        }

        int warns = event.getCurrentWarns();

        List<Integer> warnPointList = config.getIntegerList("punishment-point-per-warn");
        if (warnPointList.isEmpty()) {
            return;
        }
        if (warns > warnPointList.size()) {
            warns = warnPointList.size();
        } else if (warns < 1) {
            warns = 1;
        }
        int punishmentPoint = warnPointList.get(warns - 1);

        List<String> jails = JailConfig.getJails();
        jails.removeIf(jail -> !JailConfig.exist(jail));
        String jailName = config.getString("jail-name-for-auto-punishment", "");
        if (jails.isEmpty()) {
            return;
        } else if (!jails.contains(jailName)) {
            jailName = jails.get(new Random().nextInt(jails.size()));
        }

        String uuidString = convertUUIDString(event.getUuid());
        OfflinePlayer punishedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuidString));

        if (punishedPlayer.isOnline()) {
            Prisoners.punishPlayer(punishedPlayer.getPlayer(), jailName, null, punishmentPoint, event.getReason());
            return;
        } else {
            WantedPlayers.addWantedPlayer(punishedPlayer, jailName, punishmentPoint, event.getReason());
            return;
        }
    }

    private static String convertUUIDString(String uuidString) {
        return uuidString.replaceAll("^(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})$", "$1-$2-$3-$4-$5");
	}
}
