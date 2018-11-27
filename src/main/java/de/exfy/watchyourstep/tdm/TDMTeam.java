package de.exfy.watchyourstep.tdm;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

public enum TDMTeam {
	BLUE(ChatColor.BLUE, DyeColor.BLUE, "Blau"),
	RED(ChatColor.RED, DyeColor.RED, "Rot");

	private final ChatColor chatColor;
	private final DyeColor dyeColor;
	private final String localized;

	TDMTeam(ChatColor chatColor, DyeColor dyeColor, String localized) {
		this.chatColor = chatColor;
		this.dyeColor = dyeColor;
		this.localized = localized;
	}

	public ChatColor getChatColor() {
		return chatColor;
	}

	public DyeColor getDyeColor() {
		return dyeColor;
	}

	public String getLocalized() {
		return localized;
	}
	
	public static TDMTeam fromUserInput(String input) {
		TDMTeam team = null;

		try {
			team = TDMTeam.valueOf(input.toUpperCase());
			if (team != null) {
				// user is a dev
				return team;
			}
		} catch (Exception ignore) {}

		boolean found = false;
		for (TDMTeam t : TDMTeam.values()) {
			if (t.getLocalized().toLowerCase().startsWith(input.toLowerCase())) {
				if (team == null) {
					team = t;
					found = true;
				} else {
					// found second map, ambiguous
					found = false;
					break;
				}
			}
		}

		if (found) {
			return team;
		}

		return null;
	}
}
