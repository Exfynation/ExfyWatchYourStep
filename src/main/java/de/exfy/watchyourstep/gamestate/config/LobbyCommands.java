package de.exfy.watchyourstep.gamestate.config;

import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;
import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.parametric.annotation.Optional;
import com.sk89q.intake.parametric.annotation.Switch;

import de.exfy.core.modules.intake.module.classifier.Sender;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.maps.Maps;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.gamelib.maps.extra.Spawns;
import de.exfy.gamelib.maps.extra.SpawnsProvider;

public class LobbyCommands {
	
	@Command(aliases = "set", desc = "Einstellen der Lobby-Map", min = 0)
	public void setLobby(@Sender Player player, @Optional World world, @Switch('b') String builder) {
		if (world == null) {
			world = player.getWorld();
		}
		
		if (builder == null) {
			builder = "Exfy-Team";
		}
		
		MinecraftMap lobbyMap = Maps.getLobby();
		if (lobbyMap == null) {
			lobbyMap = new MinecraftMap("Lobby", builder, world.getName(), 0, true, new JsonObject(), new JsonObject());
		}
		
		Maps.setMap(lobbyMap);
		
		player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Lobby wurde auf §a" + world.getName() + " §7gesetzt.");
	}
	
	@Command(aliases = "spawn", desc = "Setzen des Lobby-Spawns")
	public void setSpawn(@Sender Player player) throws CommandException {
		MinecraftMap lobbyMap = Maps.getLobby();
		if (lobbyMap == null) {
			throw new CommandException("Zuerst muss die Lobby-Map mit §a/lobby set §7gesetzt werden!");
		}
		
		if (!player.getWorld().getName().equalsIgnoreCase(lobbyMap.getWorldFolderName())) {
			throw new CommandException("Du musst in der Lobby sein, um den Spawn-Punkt zu setzen!");
		}
		
		Location loc = player.getLocation();
		Spawns spawns = SpawnsProvider.load(lobbyMap);
		spawns.setGeneralSpawns(Collections.singleton(loc));
		spawns.writeToMap(lobbyMap);
		Maps.setMap(lobbyMap);
		
		player.sendMessage(GameLib.getCurrentGamePrefix() + "Der Spawn der Lobby wurde auf deine aktuelle Position gesetzt.");
	}
}
