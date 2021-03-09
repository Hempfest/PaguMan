package com.youtube.hempfest.paginatedmenu;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class PaginatedBuilder {

	private final Inventory inv;
	private final Plugin plugin;
	private int amountPer;
	private int index;
	private int page;
	private LinkedList<String> collection;
	private Map<ItemStack, Integer> navLeft = new HashMap<>();
	private Map<ItemStack, Integer> navRight = new HashMap<>();
	private Map<ItemStack, Integer> navBack = new HashMap<>();
	public static PaginatedListener listener;
	private final NamespacedKey key;
	protected final LinkedList<ItemStack> contents = new LinkedList<>();
	protected final Map<ItemStack, InventoryClick> actions = new HashMap<>();

	public PaginatedBuilder(Plugin plugin, String title) {
		this.plugin = plugin;
		key = new NamespacedKey(plugin, "paginated_utility_manager");
		this.inv = Bukkit.createInventory(null, 54, title);
		if (listener == null) {
			listener = new PaginatedListener(this);
			Bukkit.getPluginManager().registerEvents(listener, plugin);
		}
	}

	public PaginatedBuilder collect(LinkedList<String> collection) {
		this.collection = collection;
		return this;
	}

	public PaginatedBuilder limit(int amountPer) {
		this.amountPer = amountPer;
		return this;
	}

	protected PaginatedBuilder adjust() {
			//addMenuBorder();
		if (collection == null) {
			collection = new LinkedList<>();
		}
		LinkedList<String> members = collection;
			if (!members.isEmpty()) {
				for (int i = 0; i < amountPer; i++) {
					index = amountPer * page + i;
					if (index >= members.size())
						break;
					if (members.get(index) != null) {
						boolean isNew = Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList()).contains("PLAYER_HEAD");
						ItemStack item;
						if (isNew) {
							item = new ItemStack(Material.valueOf("PLAYER_HEAD"));
						} else {
							item = new ItemStack(Material.valueOf("SKULL_ITEM"));
						}
						AsyncMenuItemFillingEvent event = new AsyncMenuItemFillingEvent(this, members.get(index), item);
						Bukkit.getPluginManager().callEvent(event);
						if (!contents.contains(event.getItem())) {
							contents.add(event.getItem());
						}
						inv.addItem(event.getItem());
					}
				}
			}
			//setFillerGlass();
		return this;
	}

	public PaginatedBuilder setNavigationLeft(ItemStack item, int slot, InventoryClick click) {
		this.navLeft.putIfAbsent(item, slot);
		this.actions.putIfAbsent(item, click);
		return this;
	}

	public PaginatedBuilder setNavigationRight(ItemStack item, int slot, InventoryClick click) {
		this.navRight.putIfAbsent(item, slot);
		this.actions.putIfAbsent(item, click);
		return this;
	}

	public PaginatedBuilder setNavigationBack(ItemStack item, int slot, InventoryClick click) {
		this.navBack.putIfAbsent(item, slot);
		this.actions.putIfAbsent(item, click);
		return this;
	}

	public PaginatedMenu build() {
		return new PaginatedMenu(this);
	}

	public Inventory getInventory() {
		return inv;
	}

	public static PaginatedListener getListener() {
		return listener;
	}

	public int getAmountPerPage() {
		return amountPer;
	}

	public NamespacedKey getKey() {
		return key;
	}

	public LinkedList<String> getCollection() {
		return collection;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	private static class PaginatedListener implements Listener {

		private final PaginatedBuilder builder;

		protected PaginatedListener(PaginatedBuilder builder) {
			this.builder = builder;
		}

		@EventHandler(priority = EventPriority.LOW)
		public void onFill(AsyncMenuItemFillingEvent e) {
			e.buildItem(() -> {
				ItemStack item = e.getItem();
				ItemMeta meta = item.getItemMeta();
				UUID id = UUID.fromString(e.getContext());
				meta.setDisplayName(Bukkit.getOfflinePlayer(id).getName());
				meta.getPersistentDataContainer().set(builder.key, PersistentDataType.STRING, e.getContext());
				item.setItemMeta(meta);
				return item;
			});
		}

		@EventHandler(priority = EventPriority.NORMAL)
		public void onClick(InventoryClickEvent e) {
			if (!(e.getWhoClicked() instanceof Player))
				return;
			if (e.getView().getTopInventory().getSize() < 54)
				return;

			if (e.getHotbarButton() != -1) {
				e.setCancelled(true);
				return;
			}

			if (e.getClickedInventory() == e.getInventory()) {
				Player p = (Player) e.getWhoClicked();
				if (e.getCurrentItem() != null) {
					ItemStack item = e.getCurrentItem();
					if (builder.contents.stream().anyMatch(i -> i.equals(item)) || builder.navBack.keySet().stream().anyMatch(i -> i.isSimilar(item))) {
						builder.actions.get(item).clickEvent(new PaginatedClick(builder, p, e.getView(), e.getCurrentItem()));
						e.setCancelled(true);
					}
					if (builder.navLeft.keySet().stream().anyMatch(i -> i.isSimilar(item))) {
						if (builder.page == 0) {
							p.sendMessage("Already on first page.");
						} else {
							builder.page -= 1;
							builder.actions.get(item).clickEvent(new PaginatedClick(builder, p, e.getView(), e.getCurrentItem()));
							e.setCancelled(true);
						}
					}
					if (builder.navRight.keySet().stream().anyMatch(i -> i.isSimilar(item))) {
						if (!((builder.index + 1) >= builder.collection.size())) {
							builder.page += 1;
							builder.actions.get(item).clickEvent(new PaginatedClick(builder, p, e.getView(), e.getCurrentItem()));
							e.setCancelled(true);
						} else {
							p.sendMessage("Already on last page.");
						}
					}
				}
			}

		}

	}


}
