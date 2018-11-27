package de.exfy.watchyourstep.gamestate;

import java.util.Collection;

import de.exfy.watchyourstep.helper.LobbyChatListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;
import com.sk89q.intake.parametric.annotation.Switch;

import de.exfy.core.ExfyCore;
import de.exfy.core.modules.intake.IntakeModule;
import de.exfy.core.modules.intake.module.classifier.Sender;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.features.catchGameFeature.CatchGameFeature;
import de.exfy.gamelib.features.lobbyFeature.LobbySettings;
import de.exfy.gamelib.gameState.GameState;
import de.exfy.gamelib.gameState.general.LobbyGameState;
import de.exfy.gamelib.maps.Maps;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.gamelib.maps.extra.SpawnsProvider;
import de.exfy.watchyourstep.LobbyPlayer;
import de.exfy.watchyourstep.MapType;
import de.exfy.watchyourstep.WatchYourStep;
import de.exfy.watchyourstep.WatchYourStepMap;
import de.exfy.watchyourstep.tdm.TDMGameState;
import de.exfy.watchyourstep.tdm.TDMTeam;

public class WatchYourStepLobbyGameState extends LobbyGameState implements Listener {

	private Listener chatListener;

	@Override
	public void onEnable() {
		super.onEnable();

		CatchGameFeature feature = GameLib.getFeatureManager().getFeature(CatchGameFeature.class);
		//feature.enable();

		Bukkit.getPluginManager().registerEvents(this, ExfyCore.getInstance());
		Bukkit.getPluginManager().registerEvents(chatListener = new LobbyChatListener(), ExfyCore.getInstance());
		IntakeModule.getCommandGraph().commands().registerMethods(this);
	}

	@Override
	public void onDisable() {
		super.onDisable();

		HandlerList.unregisterAll(this);
		HandlerList.unregisterAll(chatListener);

		CatchGameFeature feature = GameLib.getFeatureManager().getFeature(CatchGameFeature.class);
		feature.disable();

		if(Bukkit.getOnlinePlayers().size() != 0) LobbyPlayer.destroyAll();
		if(IntakeModule.getCommandGraph() != null) IntakeModule.getCommandGraph().commands().unregisterMethods(this);
	}

	@Override
	public String getMapName() {
		return WatchYourStep.getGameMap().getMinecraftMap().getName();
	}

	@Override
	protected LobbySettings getLobbySettings() {
		MinecraftMap lobby = Maps.getLobby();
		Collection<Location> spawns = SpawnsProvider.load(lobby).getGeneralSpawns();
		WatchYourStepMap map = WatchYourStep.getGameMap();
		return new LobbySettings(map.getMinPlayers(), map.getMaxPlayers(), 30, lobby.getWorldFolderName(), spawns.iterator().next());
	}

	@Override
	protected GameState getNextGameState() {
		WatchYourStepMap map = WatchYourStep.getGameMap();
		return map.getMapType().getGameState();
	}
	
	@Override
	protected GameState getConfigureGameState() {
		return new WatchYourStepConfigGameState(null);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		LobbyPlayer.wrap(p).initScoreboard();
	}
	
	@Command(aliases = "forceteam", desc = "Bestimmt, dass der angegebene Spieler dem angegebenen Team angehört")
	@Require("exfy.forceteam")
	public void forceTeam(@Sender Player player, String team, @Optional Player target, @Switch('s') boolean silent) {
		if (WatchYourStep.getGameMap().getMapType() != MapType.TEAM_DEATHMATCH) {
			// nooooooope
			player.sendMessage(GameLib.getCurrentGamePrefix() +
					"Du kannst §a/forceteam §7nicht benutzen, da der Spielmodus keine Teams unterstützt!");
			return;
		}

		TDMTeam tdmTeam = TDMTeam.fromUserInput(team);
		if (tdmTeam == null) {
			player.sendMessage(GameLib.getCurrentGamePrefix() +
					"§cDas eingegebene Team wurde nicht gefunden!");
			return;
		}

		TDMGameState state = (TDMGameState) WatchYourStep.getGameMap().getMapType().getGameState();
		state.addPreference(target != null ? target : player, tdmTeam);

		if (target == null) {
			player.sendMessage(GameLib.getCurrentGamePrefix() +
					"Du wirst im folgenden Spiel bevorzugt zum Team " + tdmTeam.getChatColor().toString() +
					tdmTeam.getLocalized().toUpperCase() + " §7gehören!");
		} else {
			if (!silent) {
				target.sendMessage(GameLib.getCurrentGamePrefix() +
						"§a" + player.getName() + " §7hat dich zum Team " + tdmTeam.getChatColor().toString() +
						tdmTeam.getLocalized().toUpperCase() + " §7zugeordnet!");
			}

			player.sendMessage(GameLib.getCurrentGamePrefix() +
					"Du hast den Spieler §a" + target.getName() + " §7zum Team " + tdmTeam.getChatColor().toString() +
					tdmTeam.getLocalized().toUpperCase() + " §7hinzugefügt.");
		}
	}

}
