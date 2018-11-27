package de.exfy.watchyourstep.gamestate.ingame;

import de.exfy.core.ExfyCore;
import de.exfy.core.modules.Coins;
import de.exfy.core.modules.Stats;
import de.exfy.core.modules.stats.GameStat;
import de.exfy.gamelib.GameLib;
import de.exfy.gamelib.features.spectatorFeature.SpectatorFeature;
import de.exfy.watchyourstep.WatchYourStep;
import de.exfy.watchyourstep.WatchYourStepPlayer;
import de.exfy.watchyourstep.gamestate.WatchYourStepInGameGameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IngameListener implements Listener {

    private WatchYourStepInGameGameState gameState;
    private List<UUID> markedRightBombs = new ArrayList<>();
    private List<UUID> markedFalseBombs = new ArrayList<>();

    private List<Location> cooldown = new ArrayList<>();
    private int mines;

    public IngameListener(WatchYourStepInGameGameState state) {
        this.gameState = state;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        e.setBuild(false);
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.setExpToDrop(0);
        e.setCancelled(true);
        if (!(e.getBlock().getType() == Material.SAND)) return;
        if (!(e.getPlayer().getItemInHand().getType() == Material.IRON_SPADE)) return;

        if (e.getBlock().getRelative(BlockFace.DOWN).getType() == Material.TNT) {
            gameState.respawn(e.getPlayer());
            GameLib.getFeatureManager().getFeature(SpectatorFeature.class).addSpectator(e.getPlayer());

            e.getPlayer().sendMessage(GameLib.getCurrentGamePrefix() + "Du hast eine §aMine§7 abgebaut und bist somit §aausgeschieden§7!");

            if (gameState.getActivePlayers().size() == 0 || gameState.getActivePlayers().size() == 1) {
                Player player = e.getPlayer();

                if (player.hasPermission("exfy.premiumplus")) {
                    player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a400 §7⛀ fürs Mitspielen, da du ein §aPremium+ §7Mitglied bist!");
                    Coins.addCoins(player.getUniqueId(), 400);
                }
                else if (player.hasPermission("exfy.premium")) {
                    player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a300 §7⛀ fürs Mitspielen, da du ein §aPremium §7Mitglied bist!");
                    Coins.addCoins(player.getUniqueId(), 300);
                }
                else {
                    player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a200 §7⛀ fürs Mitspielen!");
                    Coins.addCoins(player.getUniqueId(), 200);
                }

                gameState.finishRound();
            } else {
                Player player = e.getPlayer();

                double multipler = 1;

                if (player.hasPermission("exfy.premiumplus")) {
                    player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a400 §7⛀ fürs Mitspielen, da du ein §aPremium+ §7Mitglied bist!");
                    Coins.addCoins(player.getUniqueId(), 400);
                    multipler = 2;
                }
                else if (player.hasPermission("exfy.premium")) {
                    player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a300 §7⛀ fürs Mitspielen, da du ein §aPremium §7Mitglied bist!");
                    Coins.addCoins(player.getUniqueId(), 300);
                    multipler = 1.5;
                }
                else {
                    player.sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a200 §7⛀ fürs Mitspielen!");
                    Coins.addCoins(player.getUniqueId(), 200);
                }

                int coins = gameState.getCoins().remove(e.getPlayer().getUniqueId());
                e.getPlayer().sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a" + Math.max(10, (int)(coins*multipler)) + " §7⛀ für deinen markierten Minen!");
                Coins.addCoins(e.getPlayer().getUniqueId(), Math.max(10, (int)(coins*multipler)));

            }

            return;
        }


        e.getBlock().setType(Material.AIR);
        checkBlocks(e.getBlock());

    }


    private void checkBlocks(Block block) {
        BlockFace[] faces = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
        BlockFace[] facesFull = {BlockFace.EAST, BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.NORTH, BlockFace.SOUTH};

        for(BlockFace face : faces) {
            if(block.getRelative(face).getType() != Material.SAND) continue;


            if(block.getRelative(face).getRelative(BlockFace.DOWN).getType() == Material.STAINED_CLAY && block.getRelative(face).getRelative(BlockFace.DOWN).getData() == ((byte) 0)) {
                block.getRelative(face).setType(Material.AIR);

                for(BlockFace newFace: facesFull) {
                    Block down = block.getRelative(face).getRelative(BlockFace.DOWN);

                    if(down.getRelative(newFace).getType() == Material.STAINED_CLAY && down.getRelative(newFace).getData() != ((byte) 0)) {
                        down.getRelative(newFace).getRelative(BlockFace.UP).setType(Material.AIR);
                    }
                }

                checkBlocksWithout(block.getRelative(face), face);
            }
            continue;
        }
    }

    private void checkBlocksWithout(Block block, BlockFace without) {
        BlockFace[] faces = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
        BlockFace[] facesFull = {BlockFace.EAST, BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.NORTH, BlockFace.SOUTH};

        if(without == BlockFace.EAST) {
            BlockFace[] faces_new = {BlockFace.EAST,  BlockFace.NORTH, BlockFace.SOUTH};
            faces = faces_new;
        }
        else if(without == BlockFace.WEST) {
            BlockFace[] faces_new = {BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
            faces = faces_new;
        }
        else if(without == BlockFace.NORTH) {
            BlockFace[] faces_new = {BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH};
            faces = faces_new;
        }
        else if(without == BlockFace.SOUTH) {
            BlockFace[] faces_new = {BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST};
            faces = faces_new;
        }


        for(BlockFace face : faces) {
            if(block.getRelative(face).getType() != Material.SAND) continue;

            if(block.getRelative(face).getRelative(BlockFace.DOWN).getType() == Material.STAINED_CLAY && block.getRelative(face).getRelative(BlockFace.DOWN).getData() == ((byte) 0)) {
                block.getRelative(face).setType(Material.AIR);

                for(BlockFace newFace: facesFull) {
                    Block down = block.getRelative(face).getRelative(BlockFace.DOWN);

                    if(down.getRelative(newFace).getType() == Material.STAINED_CLAY && down.getRelative(newFace).getData() != ((byte) 0)) {
                        down.getRelative(newFace).getRelative(BlockFace.UP).setType(Material.AIR);
                    }
                }

                checkBlocksWithout(block.getRelative(face), face);
            }
            continue;
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!(e.getPlayer().getItemInHand().getType() == Material.STICK) && !(e.getPlayer().getItemInHand().getType() == Material.IRON_SPADE)) return;
        if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (e.getClickedBlock().getRelative(BlockFace.DOWN).getType() != Material.STAINED_CLAY && e.getClickedBlock().getRelative(BlockFace.DOWN).getType() != Material.TNT)
            return;

        if (e.getClickedBlock().getType() == Material.SAND) {
            byte color = WatchYourStepPlayer.wrap(e.getPlayer()).getMarkColor();

            if (e.getClickedBlock().getRelative(BlockFace.DOWN).getType() == Material.TNT) {
                gameState.getCoins().put(e.getPlayer().getUniqueId(), gameState.getCoins().getOrDefault(e.getPlayer().getUniqueId(), 0) + (int) (7.5 * WatchYourStep.getGameMap().getDifficulty().getMines()));
                System.out.println("PUTTING ADD for " + e.getPlayer().getName());
                GameStat games = Stats.getStats(e.getPlayer().getUniqueId()).getGameStats("WatchYourStep").getStat("stat.mines");
                games.addScore(1);
                mines++;

                if (mines == WatchYourStep.getGameMap().getMines()) {
                    gameState.finishRound();
                }
            }

            if (e.getClickedBlock().getRelative(BlockFace.DOWN).getType() != Material.TNT ) {
                gameState.getCoins().put(e.getPlayer().getUniqueId(), Math.max(0, gameState.getCoins().getOrDefault(e.getPlayer().getUniqueId(), 0) - ((int) (7.5 * WatchYourStep.getGameMap().getDifficulty().getMines()))));
                System.out.println("PUTTING REMOVE for " + e.getPlayer().getName());
            }


            e.getClickedBlock().setType(Material.WOOL);
            e.getClickedBlock().setData(color);
            cooldown.add(e.getClickedBlock().getLocation());
            Bukkit.getScheduler().runTaskLater(WatchYourStep.getInstance(), () -> cooldown.remove(e.getClickedBlock().getLocation()),100L);

            if (!e.getClickedBlock().hasMetadata("player"))
                e.getClickedBlock().setMetadata("player", new FixedMetadataValue(WatchYourStep.getInstance(), e.getPlayer().getUniqueId().toString()));
            return;
        }

        if (e.getClickedBlock().getType() == Material.WOOL) {
            if (e.getClickedBlock().hasMetadata("player")) {
                WatchYourStepPlayer p = WatchYourStepPlayer.wrap(Bukkit.getPlayer(UUID.fromString(e.getClickedBlock().getMetadata("player").get(0).asString())));
                if (!e.getPlayer().getUniqueId().toString().equals(p.getPlayer().getUniqueId().toString())) {
                    if (e.getClickedBlock().getRelative(BlockFace.DOWN).getType() == Material.TNT) {
                        if(cooldown.contains(e.getClickedBlock().getLocation())) {
                            return;
                        }

                        if (!markedRightBombs.contains(e.getPlayer().getUniqueId())) {
                            e.getPlayer().sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a650 §7⛀ für das de-markieren einer §crichtigen §7Mine §cabgezogen§7!");
                            Coins.updateCoins(e.getPlayer().getUniqueId(), Math.max(Coins.getCoins(e.getPlayer().getUniqueId()) - 650, 0));
                            markedRightBombs.add(e.getPlayer().getUniqueId());
                        } else {
                            e.getPlayer().sendMessage(ExfyCore.getPrefix() + "§7Wuhu! Dies war eine richtig markierte Mine alles klar?!");
                        }
                        return;

                    } else {
                        if (!markedFalseBombs.contains(e.getPlayer().getUniqueId())) {
                            e.getPlayer().sendMessage(ExfyCore.getPrefix() + "§7Du bekommst §a250 §7⛀ für das de-markieren einer §afalschen Mine!");
                            Coins.addCoins(e.getPlayer().getUniqueId(), 250);
                            markedFalseBombs.add(e.getPlayer().getUniqueId());
                        } else {
                            e.getPlayer().sendMessage(ExfyCore.getPrefix() + "§7Wuhu! Dies war eine falsch markierte Mine und du hast es gewusst!");
                        }

                    }
                }
            }


            if (e.getClickedBlock().getRelative(BlockFace.DOWN).getType() == Material.TNT) {
                gameState.getCoins().put(e.getPlayer().getUniqueId(), gameState.getCoins().getOrDefault(e.getPlayer().getUniqueId(), 0) - (int) (7.5 * WatchYourStep.getGameMap().getDifficulty().getMines()));
                System.out.println("PUTTING ADD (invert) for " + e.getPlayer().getName());
            }
            else {
                gameState.getCoins().put(e.getPlayer().getUniqueId(), gameState.getCoins().getOrDefault(e.getPlayer().getUniqueId(), 0) + (int) (7.5 * WatchYourStep.getGameMap().getDifficulty().getMines()));
                System.out.println("PUTTING REMOVE (invert) for " + e.getPlayer().getName());
            }

            e.getClickedBlock().setType(Material.SAND);

        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        gameState.playerOffline(e.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        gameState.playerOffline(e.getPlayer());
    }
}
