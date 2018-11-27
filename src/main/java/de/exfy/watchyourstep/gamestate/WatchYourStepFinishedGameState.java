package de.exfy.watchyourstep.gamestate;

import de.exfy.core.ExfyCore;
import de.exfy.core.modules.Coins;
import de.exfy.core.modules.Stats;
import de.exfy.core.modules.TitleApi;
import de.exfy.core.modules.stats.GameStat;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.features.borderFeature.BorderFeature;
import de.exfy.gamelib.features.borderFeature.CircleInfluenceShape;
import de.exfy.gamelib.features.borderFeature.RectangleBorderShape;
import de.exfy.gamelib.features.spectatorFeature.SpectatorFeature;
import de.exfy.gamelib.gameState.GameState;
import de.exfy.gamelib.maps.extra.Border;
import de.exfy.watchyourstep.WatchYourStep;
import de.exfy.watchyourstep.WatchYourStepMap;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class WatchYourStepFinishedGameState extends GameState implements Listener {

    private Player winner;
    private Map<UUID, Integer> coins;
    private Collection<Player> ingamePlayers;

    public WatchYourStepFinishedGameState(Player winner,Map<UUID,Integer> coins,Collection<Player> ingamePlayers) {
        this.winner = winner;
        this.coins = coins;
        this.ingamePlayers = ingamePlayers;
    }

    @Override
    public String getName() {
        return "Finished";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, ExfyCore.getInstance());
        Bukkit.getOnlinePlayers().forEach(p -> p.getInventory().clear());

        SpectatorFeature feature = GameLib.getFeatureManager().getFeature(SpectatorFeature.class);
        Bukkit.getOnlinePlayers().forEach(feature::addSpectator);

        for (Player player : ingamePlayers) {
            if(player.hasPermission("exfy.premiumplus")) {
                player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a400 §7⛀ fürs Mitspielen, da du ein §aPremium+ §7Mitglied bist!");
                Coins.addCoins(player.getUniqueId(), 400);
                continue;
            }
            if(player.hasPermission("exfy.premium")) {
                player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a300 §7⛀ fürs Mitspielen, da du ein §aPremium §7Mitglied bist!");
                Coins.addCoins(player.getUniqueId(), 300);
                continue;
            }
            player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a200 §7⛀ fürs Mitspielen!");
            Coins.addCoins(player.getUniqueId(), 200);
        }

        // enable borders
        BorderFeature borderFeature = GameLib.getFeatureManager().getFeature(BorderFeature.class);

        WatchYourStepMap map = WatchYourStep.getGameMap();
        Border border = map.getBorder();
        Location sm = border.getSmallCorner();
        Location lg = border.getLargeCorner();

        borderFeature.setWorld(WatchYourStep.getGameMap().getMinecraftMap().getWorld());
        borderFeature.setBorderShape(new RectangleBorderShape(sm.getBlockX(), sm.getBlockZ(), lg.getBlockX(), lg.getBlockZ()));
        borderFeature.setPlayerShape(new CircleInfluenceShape(7));
        borderFeature.enable();

        Color[] niceColors = new Color[]{
                Color.AQUA, Color.BLUE, Color.FUCHSIA, Color.GREEN, Color.LIME, Color.MAROON, Color.NAVY, Color.OLIVE,
                Color.ORANGE, Color.PURPLE, Color.RED, Color.TEAL, Color.YELLOW
        };

        if(winner != null) {
            Bukkit.broadcastMessage(GameLib.getCurrentGamePrefix() +
                    "Der Spieler §a" + winner.getName() + " §7hat gewonnen!");


            final Player finalWinner = winner;

            GameStat games = Stats.getStats(winner.getUniqueId()).getGameStats("WatchYourStep").getStat("stat.wins");
            games.addScore(1);

            Bukkit.getOnlinePlayers().forEach(player -> {
                TitleApi.sendTitleTimes(player, 0, 80, 20);
                TitleApi.sendSubTitle(player, "§7hat gewonnen!");
                TitleApi.sendTitle(player, "§a" + finalWinner.getName());
            });


            if(winner.isOnline()) {
                FireworkEffect effect = FireworkEffect.builder()
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .withColor(niceColors[ThreadLocalRandom.current().nextInt(niceColors.length)])
                        .withTrail()
                        .build();

                Firework firework = (Firework) winner.getWorld().spawnEntity(winner.getLocation(), EntityType.FIREWORK);
                FireworkMeta meta = firework.getFireworkMeta();

                meta.addEffect(effect);
                meta.setPower(1);

                firework.setFireworkMeta(meta);


                double multipler = 1;
                Player player = winner;

                if (player.hasPermission("exfy.premiumplus")) {
                    multipler = 2;
                }
                else if (player.hasPermission("exfy.premium")) {
                    multipler = 1.5;
                }

                winner.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a" + ( (int) (this.coins.getOrDefault(winner.getUniqueId(), 200) * multipler) * 2) + " §7⛀ für deinen Sieg!");
                Coins.addCoins(winner.getUniqueId(), (int) (this.coins.getOrDefault(winner.getUniqueId(), 200) * multipler) *2 );
            }

            for(Map.Entry<UUID,Integer> entry : coins.entrySet()) {
                if(entry.getKey().equals(winner.getUniqueId())) continue;
                if(!Bukkit.getPlayer(entry.getKey()).isOnline()) continue;

                double multipler = 1;
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player.hasPermission("exfy.premiumplus")) {
                    multipler = 2;
                }
                else if (player.hasPermission("exfy.premium")) {
                    multipler = 1.5;
                }

                Bukkit.getPlayer(entry.getKey()).sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a" +  (int) (this.coins.getOrDefault((entry.getKey()), 10) * multipler) + " §7⛀ für deinen markierten Minen!");
                Coins.addCoins(entry.getKey(), (int) (this.coins.getOrDefault((entry.getKey()), 10) * multipler));
            }
        }
        else {
            Bukkit.broadcastMessage(GameLib.getCurrentGamePrefix() +
                    "§7Kein Spieler hat gewonnen!");

            Bukkit.getOnlinePlayers().forEach(player -> {
                TitleApi.sendTitleTimes(player, 0, 80, 20);
                TitleApi.sendTitle(player, "§aUnentschieden :(");
            });
        }


        Bukkit.getScheduler().runTaskLater(WatchYourStep.getInstance(), Bukkit::shutdown, 25 * 10);
    }

    @Override
    public void onDisable() {
        // well :)
        // let's try to be clean
        GameLib.getFeatureManager().getFeature(SpectatorFeature.class).disable();
        GameLib.getFeatureManager().getFeature(BorderFeature.class).disable();
        HandlerList.unregisterAll(this);
    }


    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage("");
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        e.setLeaveMessage("");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        e.getPlayer().kickPlayer("lobby");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getPlayer().getInventory().getHeldItemSlot() == 8) {
            //Well, goodybe :)
            e.getPlayer().kickPlayer("lobby");
        }
    }
}
