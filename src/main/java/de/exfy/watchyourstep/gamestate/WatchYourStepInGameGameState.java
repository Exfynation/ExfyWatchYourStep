package de.exfy.watchyourstep.gamestate;

import java.util.*;
import java.util.stream.Collectors;

import de.exfy.core.ExfyCore;
import de.exfy.core.helper.TextHighlightAnimation;
import de.exfy.core.modules.Achievements;
import de.exfy.core.modules.Stats;
import de.exfy.core.modules.stats.GameStat;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.exfy.core.helper.ItemBuilder;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.features.borderFeature.BorderFeature;
import de.exfy.gamelib.features.borderFeature.CircleInfluenceShape;
import de.exfy.gamelib.features.borderFeature.RectangleBorderShape;
import de.exfy.gamelib.features.disallowFeature.DisallowFeature;
import de.exfy.gamelib.features.disallowFeature.DisallowSettingsBuilder;
import de.exfy.gamelib.features.spectatorFeature.SpectatorFeature;
import de.exfy.gamelib.gameState.general.IngameGameState;
import de.exfy.gamelib.maps.extra.Border;
import de.exfy.watchyourstep.WatchYourStep;
import de.exfy.watchyourstep.WatchYourStepMap;
import de.exfy.watchyourstep.WatchYourStepPlayer;
import de.exfy.watchyourstep.gamestate.ingame.IngameListener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public abstract class WatchYourStepInGameGameState extends IngameGameState {

	private DisallowFeature disallowFeature;
	private SpectatorFeature spectatorFeature;
	private List<Listener> listeners;
	@Getter private Map<UUID, Integer> coins;

	protected Scoreboard scoreboard;
	protected Objective objective;
	private TextHighlightAnimation scoreboardAnimation;

	private BukkitTask scoreboardSwitchTask;

	@Override
	public void onEnable() {
		super.onEnable();

		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective("top", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		scoreboardAnimation = new TextHighlightAnimation("« Legende »", (t) -> this.objective.setDisplayName(t), 'a', 'a', '2');
		scoreboardAnimation.setPause(10);
		scoreboardAnimation.start(2);

		objective.getScore("§a8 Minen §7= §cGrau").setScore(0);
		objective.getScore("§a7 Minen §7= §cBlau").setScore(1);
		objective.getScore("§a6 Minen §7= §cHellblau").setScore(2);
		objective.getScore("§a5 Minen §7= §cMagenta").setScore(3);
		objective.getScore("§a4 Minen §7= §cRot").setScore(4);
		objective.getScore("§a3 Minen §7= §cOrangerot").setScore(5);
		objective.getScore("§a2 Minen §7= §cGelb").setScore(6);
		objective.getScore("§a1 Mine §7= §cHellgrün").setScore(7);
		objective.getScore("§a0 Minen §7= §cWeiß").setScore(8);


		MutableBoolean state = new MutableBoolean(false);
		scoreboardSwitchTask = Bukkit.getScheduler().runTaskTimer(WatchYourStep.getInstance(), () -> {
			if (!state.booleanValue()) {
				// show this scoreboard
				for (Player p : getIngamePlayers()) {
					p.setScoreboard(scoreboard);
				}
			} else {
				for (Player p : getIngamePlayers()) {
					WatchYourStepPlayer pp = WatchYourStepPlayer.wrap(p);
					pp.getScoreboard().setActive();
				}
			}

			state.setValue(!state.booleanValue());
		}, 10 * 20, 10 * 20);

		disallowFeature = GameLib.getFeatureManager().getFeature(DisallowFeature.class);

		disallowFeature.setSettings(
				DisallowSettingsBuilder
						.defaultDisallowBuilder()
						.allowAllInteraction()
						.allowMove()
						.build()
		);

		disallowFeature.enable();

		coins = new HashMap<>();
		spectatorFeature = GameLib.getFeatureManager().getFeature(SpectatorFeature.class);
		spectatorFeature.enable();
		
		listeners = new ArrayList<>();
		listeners.add(new IngameListener(this));
		
		listeners.forEach(l -> Bukkit.getPluginManager().registerEvents(l, WatchYourStep.getInstance()));

		WatchYourStep.getGameMap().getMinecraftMap().announce();

		Bukkit.getOnlinePlayers().forEach(this::initPlayer);

		Bukkit.getOnlinePlayers().forEach(p -> {
			GameStat games = Stats.getStats(p.getUniqueId()).getGameStats("WatchYourStep").getStat("stat.games");
			games.addScore(1);
		});

		// enable borders
		BorderFeature borderFeature = GameLib.getFeatureManager().getFeature(BorderFeature.class);

		WatchYourStepMap map = WatchYourStep.getGameMap();
		Border border = map.getBorder();
		Location sm = border.getSmallCorner();
		Location lg = border.getLargeCorner();

		borderFeature.setWorld(WatchYourStep.getGameMap().getMinecraftMap().getWorld());
		borderFeature.setBorderShape(new RectangleBorderShape(sm.getBlockX(), sm.getBlockZ(), lg.getBlockX(), lg.getBlockZ()));
		borderFeature.setPlayerShape(new CircleInfluenceShape(7));

		Bukkit.getOnlinePlayers().forEach(this::preparePlayer);

		Bukkit.getScheduler().runTaskLater(ExfyCore.getInstance(), () -> {
			Bukkit.getOnlinePlayers().forEach(p -> WatchYourStepPlayer.wrap(p).initScoreboard());
			borderFeature.enable();
			super.loadingDone();
		}, 20L);

	}

	public void initInventory(Player player) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);

		player.getInventory().setItem(0, new ItemBuilder(Material.STICK).setName("§aMarkierer §7(Rechtsklick)").toItemStack());
		
		ItemStack spade = new ItemBuilder(Material.IRON_SPADE).setName("§aZerstörer §7(Linksklick)").addEnchant(Enchantment.DIG_SPEED, 1).toItemStack();
		ItemMeta spadeMeta = spade.getItemMeta();
		spadeMeta.spigot().setUnbreakable(true);
		spade.setItemMeta(spadeMeta);

		net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(spade);
		NBTTagCompound tag = stack.hasTag() ? stack.getTag() : new NBTTagCompound();

		NBTTagList list = new NBTTagList();
		list.add(new NBTTagString("minecraft:sand"));
		tag.set("CanDestroy", list);
		stack.setTag(tag);

		spade = CraftItemStack.asBukkitCopy(stack);
		player.getInventory().setItem(1, spade);
	}


	public void playerOffline(Player player) {
		if (isSpectator(player)) {
			return;
		}

		coins.remove(player.getUniqueId());

		if(getIngamePlayers().size() <= 2) {
			finishRound();
		}
	}

	public void finishRound() {
		Player winner = null;

		int mostCoins = 0;

		for(Map.Entry<UUID,Integer> e : coins.entrySet()) {
			if(e.getValue() >= mostCoins) {
				winner = Bukkit.getPlayer(e.getKey());
				mostCoins = e.getValue();
			}
		}

		GameLib.getGameStateManager().setGameState(new WatchYourStepFinishedGameState(winner,coins,getIngamePlayers()));
	}

	protected void initPlayer(Player player) {}

	protected abstract void preparePlayer(Player player);

	protected abstract void teleportToSpawnLocation(Player player);

	public void respawn(Player player) {
		teleportToSpawnLocation(player);
	}
	
	public Collection<Player> getActivePlayers() {
		return spectatorFeature.getPlayingPlayers().stream().filter(this::isActivelyPlaying).collect(Collectors.toList());
	}

	public Collection<Player> getIngamePlayers() {
		return spectatorFeature.getPlayingPlayers();
	}

	@Override
	public String getMapName() {
		return WatchYourStep.getGameMap().getMinecraftMap().getName();
	}

	public boolean isActivelyPlaying(Player player) {
		return !isSpectator(player);
	}

	public boolean isSpectator(Player player) {
		return spectatorFeature.getSpectators().contains(player);
	}

	@Override
	public void onDisable() {
		super.onDisable();

		scoreboardSwitchTask.cancel();
		scoreboardAnimation.stop();

		Bukkit.getOnlinePlayers().forEach(p -> p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()));

		try {
			objective.unregister();
		}
		catch (Exception ex) { }

		listeners.forEach(HandlerList::unregisterAll);
		listeners.clear();
		
		disallowFeature.disable();

		BorderFeature borderFeature = GameLib.getFeatureManager().getFeature(BorderFeature.class);
		borderFeature.disable();

		spectatorFeature.disable();

		spectatorFeature.getPlayingPlayers().forEach(pl -> WatchYourStepPlayer.wrap(pl).destroy());
	}
}
