package com.youtube.hempfest.paginatedmenu;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public final class PaginatedMenu {

	private final PaginatedBuilder builder;

	protected PaginatedMenu(PaginatedBuilder builder) {
		this.builder = builder;
	}

	public void open(Player p) {
		p.openInventory(builder.adjust().getInventory());
	}

	public void unregister() {
		HandlerList.unregisterAll(builder.getListener());
	}

	public UUID getId() {
		return builder.getId();
	}

}
