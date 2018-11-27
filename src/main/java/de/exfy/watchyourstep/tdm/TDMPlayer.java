package de.exfy.watchyourstep.tdm;

import com.google.common.cache.CacheLoader;
import de.exfy.core.helper.player.PlayerAssigner;
import org.bukkit.entity.Player;

public final class TDMPlayer {
	private static final PlayerAssigner<TDMPlayer> assigner = new PlayerAssigner<>(new CacheLoader<Player, TDMPlayer>() {
		@Override
		public TDMPlayer load(Player player) throws Exception {
			return new TDMPlayer(player);
		}
	});

	public static TDMPlayer wrap(Player player) {
		return assigner.wrap(player);
	}

	private final Player player;
	private TDMTeam team;

	public TDMPlayer(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public TDMTeam getTeam() {
		return team;
	}

	public void setTeam(TDMTeam team) {
		this.team = team;
	}
}
