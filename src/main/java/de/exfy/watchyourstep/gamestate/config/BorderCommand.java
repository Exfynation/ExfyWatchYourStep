package de.exfy.watchyourstep.gamestate.config;

import com.sk89q.intake.Command;

import de.exfy.core.modules.intake.module.classifier.Sender;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.maps.Maps;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.watchyourstep.WatchYourStepMap;

import org.bukkit.entity.Player;

public class BorderCommand {
	@Command(aliases = "get", desc = "Zeigt die momentanen Ränder an")
	public void get(@Sender Player player, MinecraftMap m) {
		WatchYourStepMap map = WatchYourStepMap.wrap(m);
		if (!map.getBorder().isDefined()) {
			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName() + " §7hat noch keine Ränder.");
			return;
		}

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Die momentanen Grenzen der Map §a" + m.getName() + " §7sind:");
		player.sendMessage(GameLib.getCurrentGamePrefix() + "  $a" + map.getBorder().getSmallCorner().toString());
		player.sendMessage(GameLib.getCurrentGamePrefix() + "  $a" + map.getBorder().getLargeCorner().toString());
	}

	@Command(aliases = "p1", desc = "Setzt Punkt 1 der Umrandung")
	public void p1(@Sender Player player, MinecraftMap m) {
		WatchYourStepMap map = WatchYourStepMap.wrap(m);

		map.getBorder().setFirstPoint(player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5));

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Der Punkt 1 der Ränder der Map §a" + m.getName() + " §7wurde gesetzt.");
	}

	@Command(aliases = "p2", desc = "Setzt Punkt 2 der Umrandung")
	public void p2(@Sender Player player, MinecraftMap m) {
		WatchYourStepMap map = WatchYourStepMap.wrap(m);

		map.getBorder().setSecondPoint(player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5));
		map.getBorder().writeToMap(m);
		Maps.setMap(m);
		WatchYourStepMap.reload(m);

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Der Punkt 2 der Ränder der Map §a" + m.getName() + " §7wurde gesetzt.");
		player.sendMessage(GameLib.getCurrentGamePrefix() + "Hiermit ist die Definition der Ränder für §a" + m.getName() + " §7abgeschlossen.");
	}
}
