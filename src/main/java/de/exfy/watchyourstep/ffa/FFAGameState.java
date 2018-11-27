package de.exfy.watchyourstep.ffa;

import de.exfy.core.ExfyCore;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import de.exfy.core.helper.player.PlayerUtils;
import de.exfy.core.helper.player.PlayerUtils.ResetFlags;
import de.exfy.watchyourstep.WatchYourStep;
import de.exfy.watchyourstep.gamestate.WatchYourStepInGameGameState;

public class FFAGameState extends WatchYourStepInGameGameState {

	@Override
	public void onEnable() {
		super.onEnable();
		Bukkit.getPluginManager().registerEvents(new FFAChatListener(), ExfyCore.getInstance());
	}

	@Override
	protected void preparePlayer(Player player) {
		PlayerUtils.reset(player, ResetFlags.defaultResetBuilder().gameMode(GameMode.ADVENTURE).build());
		teleportToSpawnLocation(player);
		initInventory(player);
	}

	@Override
	public void teleportToSpawnLocation(Player player) {
		player.teleport(WatchYourStep.getGameMap().getRandomSpawn("general"));
	}
}
