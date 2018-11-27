package de.exfy.watchyourstep;

import de.exfy.gamelib.GameLib;
import org.bukkit.entity.Player;

import com.google.common.cache.CacheLoader;

import de.exfy.core.helper.player.InfoScoreboard;
import de.exfy.core.helper.player.PlayerAssigner;
import de.exfy.gamelib.maps.MinecraftMap;

public class LobbyPlayer implements PlayerAssigner.Destroyable {
	private static final PlayerAssigner<LobbyPlayer> assigner = new PlayerAssigner<>(new CacheLoader<Player, LobbyPlayer>() {
		@Override
		public LobbyPlayer load(Player player) throws Exception {
			return new LobbyPlayer(player);
		}
	});

	public static LobbyPlayer wrap(Player player) {
		return assigner.wrap(player);
	}

	public static void destroyAll() {
		assigner.getEntries().forEach(assigner::remove);
	}

	private final Player player;
	private InfoScoreboard scoreboard;

	public LobbyPlayer(Player player) {
		this.player = player;
	}

	public void initScoreboard() {
		WatchYourStepMap map = WatchYourStep.getGameMap();
		MinecraftMap m = map.getMinecraftMap();

		this.scoreboard = new InfoScoreboard(player, "Watch Your Step");
		this.scoreboard.new InfoEntry("game", "Spiel-Code", GameLib.getGameKey());
		this.scoreboard.new InfoEntry("map", "Map", m.getName());
		this.scoreboard.new InfoEntry("difficulty", "Schwierigkeit", map.getDifficulty().getLocalization());
		this.scoreboard.new InfoEntry("builder", "Erbauer", m.getBuilder());
		this.scoreboard.new InfoEntry("type", "Spielmodus", map.getMapType().getReadableName());
		this.scoreboard.update();
	}

	public InfoScoreboard getScoreboard() {
		return scoreboard;
	}

	public void destroy() {
		getScoreboard().destroy();
	}
}
