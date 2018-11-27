package de.exfy.watchyourstep;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.exfy.core.helper.GsonHelper;
import de.exfy.gamelib.maps.MinecraftMap;
import de.exfy.gamelib.maps.extra.Border;
import de.exfy.gamelib.maps.extra.BorderProvider;
import de.exfy.gamelib.maps.extra.Spawns;
import de.exfy.gamelib.maps.extra.SpawnsProvider;
import de.exfy.watchyourstep.tdm.TDMTeam;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WatchYourStepMap {

    private static LoadingCache<MinecraftMap, WatchYourStepMap> mapCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<MinecraftMap, WatchYourStepMap>() {
                @Override
                public WatchYourStepMap load(MinecraftMap minecraftMap) throws Exception {
                    return new WatchYourStepMap(minecraftMap);
                }
            });

    public static WatchYourStepMap wrap(MinecraftMap map) {
        return mapCache.getUnchecked(map);
    }

    public static void reload(MinecraftMap map) {
        mapCache.invalidate(map);
    }

    public static void reloadAll() {
        mapCache.invalidateAll();
    }

    private MinecraftMap map;
    @Getter
    @Setter
    private int mines;
    private Spawns spawns;
    private MapType mapType;
    private Border border;
    private int maxPlayers;
    private int minPlayers;
    private MapDifficulty difficulty;
    private Location pos1;
    private Location pos2;
    private static Multimap<String, Location> usedSpawns = HashMultimap.create();

    private WatchYourStepMap(MinecraftMap map) {
        this.map = map;
        this.spawns = SpawnsProvider.load(map);
        this.border = BorderProvider.load(map);

        if (!isFullyConfigured()) {
            return;
        }

        this.mapType = MapType.valueOf(map.getExtraData().get("mapType").getAsString());
        this.maxPlayers = map.getExtraData().get("max_players").getAsInt();
        this.minPlayers = map.getExtraData().get("min_players").getAsInt();
        this.difficulty = MapDifficulty.valueOf(map.getExtraData().get("mapDifficulty").getAsString().toUpperCase());
        this.pos1 = GsonHelper.getBukkitGson().fromJson(map.getExtraData().get("pos1").getAsJsonObject(), Location.class);
        this.pos2 = GsonHelper.getBukkitGson().fromJson(map.getExtraData().get("pos2").getAsJsonObject(), Location.class);
    }

    public MapType getMapType() {
        return mapType;
    }

    public Spawns getSpawns() {
        return spawns;
    }

    public Border getBorder() {
        return border;
    }

    public boolean shouldBeUsed() {
        return isFullyConfigured();
    }

    public boolean isFullyConfigured() {
        return getConfigureError() == null;
    }

    public String getConfigureError() {
        if (!map.getExtraData().has("mapType")) {
            return "Der Kartentyp wurde nicht konfiguriert!";
        }

        if (!map.getExtraData().has("mapDifficulty")) {
            return "Die Schwierigkeit wurde nicht konfiguriert!";
        }

        if (!map.getExtraData().has("pos1")) {
            return "Der Arenaboden wurde nicht definiert!";
        }

        if (!map.getExtraData().has("pos2")) {
            return "Der Arenaboden wurde nicht definiert!";
        }

        if (!map.getExtraData().has("min_players") || !map.getExtraData().has("max_players")) {
            return "Die Karte hat keine Spielerzahlen definiert.";
        }

        if (!border.isDefined()) {
            return "Der Kartenrand muss noch definiert werden!";
        }

        MapType mapType = MapType.valueOf(map.getExtraData().get("mapType").getAsString());
        if (mapType == null) {
            // type wrong
            return "Der Kartentyp wurde nicht erkannt! Evtl. Plugin veraltet oder falsch manuell konfiguriert?";
        }

        switch (mapType) {
            case FREE_FOR_ALL:
                if (spawns.getGeneralSpawns().size() < 1) {
                    return "Es wurden noch keine Spawns für Free-For-All konfiguriert!";
                }
                break;
            case TEAM_DEATHMATCH:
                for (TDMTeam team : TDMTeam.values()) {
                    if (spawns.getSpawns(team.name().toLowerCase()).size() < 1) {
                        return "Es wurden noch keine Spawns für Team " + team.getChatColor() + team.name() + " §fdefiniert.";
                    }
                }
        }

        return null;
    }

    public MinecraftMap getMinecraftMap() {
        return map;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public MapDifficulty getDifficulty() {
        return difficulty;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public Location getRandomSpawn(String team) {
        if (!usedSpawns.containsKey(team) || usedSpawns.get(team).size() < 1) {
            usedSpawns.putAll(team, spawns.getSpawns(team));
        }

        List<Location> uSpawns = new ArrayList<>(usedSpawns.get(team));
        Collections.shuffle(uSpawns);
        Location spawn = uSpawns.remove(0);
        usedSpawns.remove(team, spawn);
        return spawn;
    }


    public enum MapDifficulty {
        BEGINNER(0.5, "§dAnfänger"), EASY(2, "§7Einfach"), MEDIUM(3, "&6Normal"), HARD(6.5, "§cSchwer"), EXPERT(10, "§4Experte");

        private double mines;
        private String localization;

        private MapDifficulty(double mines, String localization) {
            this.mines = mines;
            this.localization = localization;
        }

        public double getMines() {
            return mines;
        }

        public String getLocalization() {
            return localization;
        }
    }
}
