package de.exfy.watchyourstep.gamestate;

import de.exfy.core.modules.intake.IntakeModule;
import de.exfy.gamelib.gameState.general.ConfigGameState;
import de.exfy.watchyourstep.WatchYourStep;
import de.exfy.watchyourstep.gamestate.config.*;

public class WatchYourStepConfigGameState extends ConfigGameState {

	private GeneralConfigCommands generalCommands;

	public WatchYourStepConfigGameState(String error) {
		super(WatchYourStep.getInstance()::checkPrerequisites, error);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		
		generalCommands = new GeneralConfigCommands();
		
		IntakeModule.getCommandGraph().commands()
				.group("lobbyconfig")
					.registerMethods(new LobbyCommands())
					.parent()
				.group("map")
					.registerMethods(new MapCommands())
					.group("spawns")
						.registerMethods(new SpawnCommands())
						.parent()
					.group("border")
						.registerMethods(new BorderCommand())
						.parent()
					.parent()
				.registerMethods(generalCommands);
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		
		IntakeModule.getCommandGraph().commands()
				.unregisterGroup("lobby")
				.unregisterGroup("map")
				.unregisterMethods(generalCommands);
	}
}
