package de.exfy.watchyourstep;

import de.exfy.gamelib.gameState.GameState;
import de.exfy.watchyourstep.ffa.FFAGameState;
import de.exfy.watchyourstep.tdm.TDMGameState;

public enum MapType {
	FREE_FOR_ALL("Free for All", new FFAGameState()),
	TEAM_DEATHMATCH("TeamDeathmatch", new TDMGameState());

	private final String readableName;
	private final GameState gameState;

	MapType(String readableName, GameState gameState) {
		this.readableName = readableName;
		this.gameState = gameState;
	}

	public String getReadableName() {
		return readableName;
	}

	public GameState getGameState() {
		return gameState;
	}

	public static MapType fromUserInput(String input) {
		MapType type = null;

		try {
			type = MapType.valueOf(input.toUpperCase());
			if (type != null) {
				// user is a dev
				return type;
			}
		} catch (Exception ignore) {}

		boolean found = false;
		for (MapType t : MapType.values()) {
			if (t.getReadableName().toLowerCase().startsWith(input.toLowerCase())) {
				if (type == null) {
					type = t;
					found = true;
				} else {
					// found second map, ambiguous
					found = false;
					break;
				}
			}
		}

		if (found) {
			return type;
		}

		// special cases
		if (input.equalsIgnoreCase("ffa")) {
			return FREE_FOR_ALL;
		} else if (input.equalsIgnoreCase("tdm")) {
			return TEAM_DEATHMATCH;
		}

		// no idea
		return null;
	}
}
