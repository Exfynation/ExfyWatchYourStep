package de.exfy.watchyourstep.gamestate.config;

import org.bukkit.command.CommandSender;

import com.sk89q.intake.Command;

import de.exfy.core.modules.intake.module.classifier.Sender;

public class GeneralConfigCommands {
	
	@Command(aliases = "help", desc = "Get configuration help")
	public void help(@Sender CommandSender sender) {
		sender.sendMessage(new String[] { "Um ExfyWatchYourStep zu benutzen, müssen folgende Punkte erfolgt sein:",
				" - Eine Lobby-Map muss definiert sein (die erstdefinierte Map)",
				" - Mindestens eine Spiele-Map muss definiert sein",
				" - Für die Lobby-Map muss mindestens ein Spawn definiert sein",
				" - Für die Spiele-Map muss mindestens ein Spawn für jedes Team definiert sein",
				"bzw. bei Free-for-All-Maps mindestens ein Spawn" });
	}
}
