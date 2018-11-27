package de.exfy.watchyourstep;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

import com.google.common.cache.CacheLoader;

import de.exfy.core.helper.player.InfoScoreboard;
import de.exfy.core.helper.player.PlayerAssigner;
import de.exfy.watchyourstep.tdm.TDMPlayer;
import de.exfy.watchyourstep.tdm.TDMTeam;

public class WatchYourStepPlayer implements PlayerAssigner.Destroyable {

	private static int currentColor = 0;
	private byte markColor;
	
	private static final PlayerAssigner<WatchYourStepPlayer> assigner = new PlayerAssigner<>(new CacheLoader<Player, WatchYourStepPlayer>() {
		@Override
		public WatchYourStepPlayer load(Player player) throws Exception {
			return new WatchYourStepPlayer(player);
		}
	});

	public static WatchYourStepPlayer wrap(Player player) {
		return assigner.wrap(player);
	}

	public static void destroyAll() {
		assigner.getEntries().forEach(assigner::remove);
	}

	private final Player player;
	private InfoScoreboard scoreboard;

	public WatchYourStepPlayer(Player player) {
		this.player = player;
		if(WatchYourStep.getGameMap().getMapType() == MapType.FREE_FOR_ALL) {
			if(currentColor > 15) currentColor = 0;
			if(markColor > 15) markColor = 0;
			markColor = (byte) currentColor;
			currentColor++;
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void initScoreboard() {
		WatchYourStepMap map = WatchYourStep.getGameMap();

		this.scoreboard = new InfoScoreboard(player, "Watch Your Step");
		this.scoreboard.new InfoEntry("type", "Spielmodus", map.getMapType().getReadableName());

		switch (map.getMapType()) {
			case TEAM_DEATHMATCH:
				TDMTeam team = TDMPlayer.wrap(player).getTeam();
				this.scoreboard.new InfoEntry("team", "Team", team.getChatColor() + team.getLocalized().toUpperCase());
			break;
		}

		this.scoreboard.update();
	}

	public byte getMarkColor() {
		if(WatchYourStep.getGameMap().getMapType() == MapType.FREE_FOR_ALL) return markColor;
		return (byte) (TDMPlayer.wrap(player).getTeam().getDyeColor() == DyeColor.RED ? 14 : 9);
	}
	
	public InfoScoreboard getScoreboard() {
		return scoreboard;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public void destroy() {
		if (getScoreboard() != null) {
			getScoreboard().destroy();
		}
	}
}
