package com.youtube.hempfest.paginatedmenu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class PaginatedClick {

	private final InventoryView view;

	private final Player p;

	private final ItemStack clickedItem;

	protected PaginatedClick(Player p, InventoryView view, ItemStack item) {
		this.p = p;
		this.view = view;
		this.clickedItem = item;
	}



	public ItemStack getClickedItem() {
		return clickedItem;
	}

	public Player getPlayer() {
		return p;
	}

	public InventoryView getView() {
		return view;
	}
}
