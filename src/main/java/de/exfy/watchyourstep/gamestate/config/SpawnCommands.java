package de.exfy.watchyourstep.gamestate.config;

import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;

import de.exfy.core.modules.intake.module.classifier.Sender;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.maps.Maps;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.watchyourstep.WatchYourStepMap;
import de.exfy.watchyourstep.tdm.TDMTeam;

import org.bukkit.entity.Player;

import java.util.Collections;

public class SpawnCommands {

	@Command(aliases = "clear", desc = "Leert die Spawns auf einer Map")
	public void clear(@Sender Player player, MinecraftMap m) {
		WatchYourStepMap map = WatchYourStepMap.wrap(m);

		map.getSpawns().setGeneralSpawns(Collections.emptyList());
		map.getSpawns().setSpawns("blue", Collections.emptyList());
		map.getSpawns().setSpawns("red", Collections.emptyList());

		map.getSpawns().writeToMap(m);
		Maps.setMap(m);
		WatchYourStepMap.reload(m);

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Spawns auf der Map §a" + m.getName() + " §7wurden gelöscht.");
	}

	@Command(aliases = "add", desc = "Fügt einen Spawn hinzu")
	public void add(@Sender Player player, String target, MinecraftMap m) throws CommandException {
		switch (target) {
			case "general":
				addGeneral(player, m);
				break;
			case "blue":
			case "red":
				addTeam(player, target, m);
				break;
			default:
				throw new CommandException("Das Ziel §a" + target + " §7konnte nicht gefunden werden.");
		}
	}

	private void addGeneral(Player player, MinecraftMap m) throws CommandException {
		WatchYourStepMap map = WatchYourStepMap.wrap(m);
		/*if (map.getMapType() != MapType.FREE_FOR_ALL) {
			// invalid type
			throw new CommandException("Die Map §a" + m.getName() + " §7ist nicht vom §aFree-For-All§7-Typ " +
					"und kann deshalb keinen generellen Spawn bekommen.");
		}*/

		map.getSpawns().addGeneralSpawn(player.getLocation());
		map.getSpawns().writeToMap(m);
		Maps.setMap(m);
		WatchYourStepMap.reload(m);

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Der Spawn-Punkt wurde zur Map §a" + m.getName() + " §7hinzugefügt.");
	}

	private void addTeam(Player player, String team, MinecraftMap m) throws CommandException {
		WatchYourStepMap map = WatchYourStepMap.wrap(m);
		/*if (map.getMapType() != MapType.TEAM_DEATHMATCH) {
			throw new CommandException("Die Map §a" + m.getName() + " §7ist nicht im §aTeam Deathmatch§7-Modus und " +
					"kann deshalb keinen blauen oder roten Spawnpunkt bekommen.");
		}*/

		map.getSpawns().addSpawn(team, player.getLocation());
		map.getSpawns().writeToMap(m);
		Maps.setMap(m);
		WatchYourStepMap.reload(m);

		TDMTeam teamE = TDMTeam.valueOf(team.toUpperCase());
		player.sendMessage(GameLib.getCurrentGamePrefix() + "Der Spawn-Punkt wurde zum Team " + teamE.getChatColor() + team + " §7auf der Map §a" +
				m.getName() + " §7hinzugefügt.");
	}
}
