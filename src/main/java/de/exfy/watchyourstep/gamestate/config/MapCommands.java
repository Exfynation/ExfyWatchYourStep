package de.exfy.watchyourstep.gamestate.config;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;
import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.parametric.annotation.Optional;
import com.sk89q.intake.parametric.annotation.Range;
import com.sk89q.intake.parametric.annotation.Switch;

import de.exfy.core.helper.GsonHelper;
import de.exfy.core.modules.intake.module.classifier.Sender;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.maps.Maps;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.watchyourstep.MapType;
import de.exfy.watchyourstep.WatchYourStepMap;

public class MapCommands {

	
	@Command(aliases = "define", desc = "Definiert eine neue Map")
	public void define(@Sender Player player, String name, @Optional String builder) throws CommandException {
		World world = player.getWorld();

		if (builder == null) {
			builder = "Exfy-Team";
		}

		if (Maps.getMap(name) != null) {
			throw new CommandException("Eine Map mit dem Namen " + name + " existiert bereits!");
		}

		MinecraftMap map = new MinecraftMap(name, builder, world.getName(), null);
		Maps.setMap(map);

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map wurde definiert! Sie muss noch konfiguriert werden.");
	}
	
	@Command(aliases = "pos1", desc="Setzt die erste Position des Bodens")
	public void pos1(@Sender Player player, MinecraftMap m) {
		Location loc = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
		JsonObject data = m.getExtraData();
		if(data.has("pos1")) data.remove("pos1");
		data.add("pos1", GsonHelper.getBukkitGson().toJsonTree(loc).getAsJsonObject());
		
		Maps.setMap(m);
		WatchYourStepMap.reload(m);
		player.sendMessage(GameLib.getCurrentGamePrefix() + "Die erste Bodenposition der Map §a" + m.getName()
				+ " §7wurde auf §a" + loc.getBlockX() + ", " + loc.getBlockY() +  ", " + loc.getBlockZ() +"  §7gesetzt.");	
	}
	
	@Command(aliases = "pos2", desc="Setzt die zweite Position des Bodens")
	public void pos2(@Sender Player player, MinecraftMap m) {
		Location loc = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
		JsonObject data = m.getExtraData();
		if(data.has("pos2")) data.remove("pos2");
		data.add("pos2", GsonHelper.getBukkitGson().toJsonTree(loc).getAsJsonObject());
		
		Maps.setMap(m);
		WatchYourStepMap.reload(m);
		player.sendMessage(GameLib.getCurrentGamePrefix() + "Die zweite Bodenposition der Map §a" + m.getName()
				+ " §7wurde auf §a" + loc.getBlockX() + ", " + loc.getBlockY() +  ", " + loc.getBlockZ() +"  §7gesetzt.");	
	}

	@Command(aliases = "difficulty", desc = "Setzt die Schwierigkeit der Map")
	public void difficulty(@Sender Player player, MinecraftMap m, @Optional String difficulty) {
		if(difficulty != null) {
			try {
				if(WatchYourStepMap.MapDifficulty.valueOf(difficulty.toUpperCase()) == null) {
					player.sendMessage(GameLib.getCurrentGamePrefix() + "§7Die Schwierigkeit §a" + difficulty + "§7 ist keine valide Schwierigkeit!");
					return;
				}
			}
			catch(Exception ex) {
				player.sendMessage(GameLib.getCurrentGamePrefix() + "§7Die Schwierigkeit §a" + difficulty + "§7 ist keine valide Schwierigkeit!");
				return;
			}
			
			
			JsonObject data = m.getExtraData();
			data.remove("mapDifficulty");
			data.addProperty("mapDifficulty", difficulty.toUpperCase());
			Maps.setMap(m);
			WatchYourStepMap.reload(m);
			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Schwierigkeit der Map §a" + m.getName()
					+ " §7wurde auf §a" + difficulty.toUpperCase() + " §7gesetzt.");
		}
		else {
			JsonObject data = m.getExtraData();
			if(!data.has("mapDifficulty")) {
				player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
						+ " §7hat noch keine Schwierigkeit gesetzt!");
			}
			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
					+ " §7hat die Schwierigkeit: §a" + data.get("mapDifficulty").getAsString().toUpperCase() + "§7.");		
			}
	}
	
	@Command(aliases = {"min", "minPlayers"}, desc = "Liest oder setzt die minimale Spieleranzahl")
	public void min(@Sender Player player, MinecraftMap m, @Optional @Range(min = 1, max = Integer.MAX_VALUE) Integer min) {
		if (min != null) {
			// set value
			JsonObject data = m.getExtraData();
			data.remove("min_players");
			data.addProperty("min_players", min);
			Maps.setMap(m);
			WatchYourStepMap.reload(m);

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die minimale Spieleranzahl der Map §a" + m.getName()
					+ " §7wurde auf §a" + min + " §7gesetzt.");
		} else {
			// get value
			JsonObject data = m.getExtraData();

			if (!data.has("min_players")) {
				player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
						+ " §7hat noch keine minimale Spieleranzahl gesetzt.");
				return;
			}

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
					+ " §7hat eine minimale Spieleranzahl von §a" + data.get("min_players").getAsInt() + "§7.");
		}
	}

	@Command(aliases = {"max", "maxPlayers"}, desc = "Liest oder setzt die maximale Spieleranzahl")
	public void max(@Sender Player player, MinecraftMap m, @Optional @Range(min = 1, max = Integer.MAX_VALUE) Integer max) {
		if (max != null) {
			// set value
			JsonObject data = m.getExtraData();
			data.remove("max_players");
			data.addProperty("max_players", max);
			Maps.setMap(m);
			WatchYourStepMap.reload(m);

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die maximale Spieleranzahl der Map §a" + m.getName()
					+ " §7wurde auf §a" + max + " §7gesetzt.");
		} else {
			// get value
			JsonObject data = m.getExtraData();

			if (!data.has("max_players")) {
				player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
						+ " §7hat noch keine maximale Spieleranzahl gesetzt.");
				return;
			}

			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName()
					+ " §7hat eine maximale Spieleranzahl von §a" + data.get("max_players").getAsInt() + "§7.");
		}
	}

	@Command(aliases = "list", desc = "Listet alle Maps auf")
	public void list(@Sender Player player, @Switch('l') boolean local) {
		player.sendMessage(GameLib.getCurrentGamePrefix() + "Momentan" + (local ? " lokal " : " ") + "definierte Karten:");
		for (MinecraftMap map : Maps.getMaps()) {
			if (local && !map.getWorldFolderName().equalsIgnoreCase(player.getWorld().getName())) {
				continue;
			}

			WatchYourStepMap pbm = WatchYourStepMap.wrap(map);

			StringBuilder line = new StringBuilder(GameLib.getCurrentGamePrefix());
			line.append("  §a");
			line.append(map.getName());

			if (!pbm.isFullyConfigured()) {
				line.append(" §c(Unkonfiguriert)");
			}

			player.sendMessage(line.toString());
		}
	}

	@Command(aliases = "status", desc = "Gibt den Status einer Map zurück")
	public void status(@Sender Player player, MinecraftMap m) {
		WatchYourStepMap pbm = WatchYourStepMap.wrap(m);
		String status = pbm.getConfigureError();
		status = status != null ? "§c" + status.replace("§f", "§c") : "§aVoll konfiguriert!";
		player.sendMessage(GameLib.getCurrentGamePrefix() + "Der Status der Map §a" + m.getName() + " §7ist: " + status);
	}

	@Command(aliases = "type", desc = "Liest oder setzt den Spieltyp")
	public void type(@Sender Player player, MinecraftMap m, @Optional String type) {
		if (type == null) {
			getType(player, m);
		} else {
			setType(player, m, type);
		}
	}

	private void setType(Player player, MinecraftMap m, String typeStr) {
		MapType type = MapType.fromUserInput(typeStr);
		if (type == null) {
			player.sendMessage(GameLib.getCurrentGamePrefix() + "Der Typ §a" + typeStr + " §7konnte nicht erkannt werden.");
			player.sendMessage(GameLib.getCurrentGamePrefix() + "Valide Typen sind:");
			for (MapType typeI : MapType.values()) {
				player.sendMessage(GameLib.getCurrentGamePrefix() + "  §a" + typeI.getReadableName());
			}
			return;
		}

		m.getExtraData().remove("mapType");
		m.getExtraData().addProperty("mapType", type.name());
		Maps.setMap(m);
		WatchYourStepMap.reload(m);

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Der Typ der Map §a" + m.getName()
				+ " §7wurde erfolgreich auf §a" + type.getReadableName() + " §7gesetzt.");
	}

	private void getType(Player player, MinecraftMap m) {
		WatchYourStepMap map = WatchYourStepMap.wrap(m);
		if (map.getMapType() == null) {
			player.sendMessage(GameLib.getCurrentGamePrefix() + "Die Map §a" + m.getName() + " §7hat noch keinen Typ definiert.");
			return;
		}

		player.sendMessage(GameLib.getCurrentGamePrefix() + "Der Typ der Karte §a" + m.getName() + " §7ist §a"
				+ map.getMapType().getReadableName());
	}
}
