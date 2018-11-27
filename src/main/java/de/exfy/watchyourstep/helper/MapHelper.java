package de.exfy.watchyourstep.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import de.exfy.core.ExfyCore;
import de.exfy.gamelib.maps.Maps;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.watchyourstep.WatchYourStep;
import de.exfy.watchyourstep.WatchYourStepMap;

public class MapHelper implements Listener {
	
	private static List<Integer> freeCellList = new ArrayList<>();
	private static final int CELL_NOT_SET = -2;
	private static final int CELL_IS_MINE = -1;
	private static final byte[] MINE_INDICATOR = {
		0, 5, 4, 1, 14, 2, 3, 11, 7
	};

	private static int mines;

	public static void initMap(String mapName) {
		if (Maps.getLobby() == null || Maps.getMaps().size() == 0) {
			return;
		}

		for(World w : Bukkit.getWorlds()) {
			if(w.getName().equals(Maps.getLobby().getName())) continue;
			Bukkit.unloadWorld(w, false);
		}


		List<MinecraftMap> mapList = new ArrayList<>(Maps.getMaps());

		MinecraftMap map = null;

		for(MinecraftMap mapA: mapList) {
			if(mapA.getName().equals(mapName)) {
				map = mapA;
			}
		}

		map.load();
		WatchYourStep.setGameMap(WatchYourStepMap.wrap(map));

		MinecraftMap m = WatchYourStep.getGameMap().getMinecraftMap();

		World w = m.getWorld();
		w.setPVP(true);
		w.setStorm(false);
		w.setThundering(false);
		w.setWeatherDuration(0);
		w.setTime(9000);
		w.setDifficulty(Difficulty.PEACEFUL);
		w.setGameRuleValue("doDaylightCycle", "false");
		w.setGameRuleValue("doFireTick", "false");
		w.setGameRuleValue("doMobSpawning", "false");
		w.setGameRuleValue("keepInventory", "true");
		w.setGameRuleValue("mobGriefing", "false");
		w.setGameRuleValue("naturalRegeneration", "false");

		Bukkit.setDefaultGameMode(GameMode.ADVENTURE);
		Bukkit.setSpawnRadius(0);

		Bukkit.getScheduler().runTaskTimer(ExfyCore.getInstance(), () -> {
			w.setStorm(false);
			w.setThundering(false);
			w.setTime(9000);
		}, 0, 20);
		generateMap();

		WatchYourStep.getGameMap().setMines(mines);
	}
	
	
	
	@SuppressWarnings("deprecation")
	public static void generateMap() {
		if(WatchYourStep.getGameMap() == null || !WatchYourStep.getGameMap().shouldBeUsed()) return;
				
		Vector max = Vector.getMaximum(WatchYourStep.getGameMap().getPos1().toVector(), WatchYourStep.getGameMap().getPos2().toVector());
		Vector min = Vector.getMinimum(WatchYourStep.getGameMap().getPos1().toVector(), WatchYourStep.getGameMap().getPos2().toVector());
		
		int size = (max.getBlockX() - min.getBlockX()) * (max.getBlockZ() - min.getBlockZ());
		
		int xLength = (max.getBlockX() - min.getBlockX());
		int zLength = (max.getBlockZ() - min.getBlockZ());
				
		if(xLength != zLength) {
			ExfyCore.getInstance().getLogger().severe("!!! Die Map muss ein Quadrat sein um richtig generiert werden zu können !!!");
			return;
		}
		
		size = xLength;
		int[] map = generateArea((int)(WatchYourStep.getGameMap().getDifficulty().getMines() * xLength), xLength);

		int i, j, k;
		
		Location location = WatchYourStep.getGameMap().getPos1().clone();
		Block block;
		for (i = 0; i < size; i++) {
			for (j = 0; j < size; j++) {
				k = map[i * size + j];
				if (k == CELL_IS_MINE) {
					location.getBlock().setType(Material.TNT);
					mines++;
				} else if (k >= 0 && k <= 8) {
					block = location.getBlock();
					block.setType(Material.STAINED_CLAY);
					block.setData(MINE_INDICATOR[k]);
				}
				location.setX(location.getX() + 1);
			}
			location.setX(location.getX() - size);
			location.setZ(location.getZ() + 1);
		}

		i = 0;
		j = 0;
		k = 0;
		
		location = WatchYourStep.getGameMap().getPos1().clone();
		location.setY(location.getY() + 1);
				
				
		while(freeCellList.size() != WatchYourStep.getGameMap().getMaxPlayers()) {
			if(freeCellList.size() < WatchYourStep.getGameMap().getMaxPlayers()) break;
			freeCellList.remove(ThreadLocalRandom.current().nextInt(((freeCellList.size() - 1) + 1)));
		}
		
		for (i = 0; i < size; i++) {
			for (j = 0; j < size; j++) {
				k = map[i * size + j];
				location.getBlock().setType(Material.SAND);
				if(k == 0  && freeCellList.contains(i*size+j)) {
					location.getBlock().setType(Material.AIR);
				}
				location.setX(location.getX() + 1);
			}
			location.setX(location.getX() - size);
			location.setZ(location.getZ() + 1);
		}
	}
	
	private static int[] generateArea(final int mines, final int size) {
		// create and initialize array
		int i, j, k;
		final int[] map = new int[size * size];
		for (i = 0; i < map.length; i++) {
			map[i] = CELL_NOT_SET;
		}
		
		// add random mines
		for (i = 0; i < mines; i++) {
			do {
				j = (int) (Math.random() * map.length);
			} while (map[j] != CELL_NOT_SET);
			map[j] = CELL_IS_MINE;
		}
		
		
		// set other values
		for (i = 0; i < size; i++) {
			for (j = 0; j < size; j++) {
				k = i * size + j;
				if (map[k] == CELL_NOT_SET) {
					map[k] = 0;
					if (i > 0 && map[(i - 1) * size + j] == CELL_IS_MINE) {
						map[k]++;
					}
					if (i < size - 1 && map[(i + 1) * size + j] == CELL_IS_MINE) {
						map[k]++;
					}
					if (j > 0 && map[i * size + j - 1] == CELL_IS_MINE) {
						map[k]++;
					}
					if (j < size - 1 && map[i * size + j + 1] == CELL_IS_MINE) {
						map[k]++;
					}
					if (i > 0 && j > 0 && map[(i - 1) * size + j - 1] == CELL_IS_MINE) {
						map[k]++;
					}
					if (i > 0 && j < size - 1 && map[(i - 1) * size + j + 1] == CELL_IS_MINE) {
						map[k]++;
					}
					if (i < size - 1 && j > 0 && map[(i + 1) * size + j - 1] == CELL_IS_MINE) {
						map[k]++;
					}
					if (i < size - 1 && j < size - 1 && map[(i + 1) * size + j + 1] == CELL_IS_MINE) {
						map[k]++;
					}
					if(map[k] == 0) {
						freeCellList.add(i*size+j);
					}
				}
			}
		}
		
		return map;
	}
	
}
