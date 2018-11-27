package de.exfy.watchyourstep.tdm;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.exfy.core.ExfyCore;
import de.exfy.core.helper.player.PlayerUtils;
import de.exfy.core.helper.player.PlayerUtils.ResetFlags;
import de.exfy.core.modules.TabList;
import de.exfy.core.modules.TitleApi;
import de.exfy.watchyourstep.WatchYourStep;
import de.exfy.watchyourstep.gamestate.WatchYourStepInGameGameState;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;
import java.util.Map;

public class TDMGameState extends WatchYourStepInGameGameState {

    private Multiset<TDMTeam> teamUsages = HashMultiset.create();
    private Map<String, TDMTeam> preferences = new HashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(new TDMChatListener(), ExfyCore.getInstance());
    }

    public void addPreference(Player player, TDMTeam team) {
        preferences.put(player.getUniqueId().toString(), team);
    }

    @Override
    protected  void initPlayer(Player player) {
        TDMPlayer tdmP = TDMPlayer.wrap(player);
        if (!preferences.containsKey(player.getUniqueId().toString())) {
            TDMTeam least = TDMTeam.values()[0];
            for (TDMTeam team : TDMTeam.values()) {
                if (teamUsages.count(team) < teamUsages.count(least)) {
                    least = team;
                }
            }

            tdmP.setTeam(least);
            teamUsages.add(least);
        } else {
            TDMTeam preference = preferences.get(player.getUniqueId().toString());
            tdmP.setTeam(preference);
            teamUsages.add(preference);
        }
    }

    @Override
    protected void preparePlayer(Player player) {
        TDMPlayer tdmP = TDMPlayer.wrap(player);

        teleportToSpawnLocation(player);
        PlayerUtils.reset(player, ResetFlags.defaultResetBuilder().gameMode(GameMode.ADVENTURE).build());
        initInventory(player);

        Bukkit.getScheduler().runTaskAsynchronously(ExfyCore.getInstance(), () -> {
            TDMTeam team = tdmP.getTeam();
            TitleApi.sendTitleTimes(player, 20, 100, 20);
            TitleApi.sendSubTitle(player, "Du bist im Team " + team.getChatColor() + team.getLocalized().toUpperCase());
            TitleApi.sendTitle(player, "Team " + team.getChatColor() + team.getLocalized().toUpperCase());

            TabList.setCustomTabColor(player, team.getChatColor());
        });

    }

    @Override
    public void initInventory(Player player) {
        super.initInventory(player);

        TDMPlayer tdmP = TDMPlayer.wrap(player);
        TDMTeam team = tdmP.getTeam();

        player.getInventory().setArmorContents(new ItemStack[]{
                createTeamArmorPiece(Material.LEATHER_BOOTS, team),
                createTeamArmorPiece(Material.LEATHER_LEGGINGS, team),
                createTeamArmorPiece(Material.LEATHER_CHESTPLATE, team),
                createTeamArmorPiece(Material.LEATHER_HELMET, team)
        });
    }

    @Override
    public void teleportToSpawnLocation(Player player) {
        TDMPlayer tdmP = TDMPlayer.wrap(player);
        TDMTeam team = tdmP.getTeam();

        player.teleport(WatchYourStep.getGameMap().getRandomSpawn(team.name().toLowerCase()));
    }

    private ItemStack createTeamArmorPiece(Material material, TDMTeam team) {
        ItemStack is = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) is.getItemMeta();
        meta.setColor(team == TDMTeam.BLUE ? Color.BLUE : Color.RED);
        is.setItemMeta(meta);
        return is;
    }

}
