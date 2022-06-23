package dev.cromo29.machines.utility;

import dev.cromo29.machines.MachinePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * author: don't version: 2.0
 */

public class Scroller {

    static {
        MachinePlugin plugin = MachinePlugin.get();

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent event) {
                if (event.getSlotType() == InventoryType.SlotType.OUTSIDE || event.getCurrentItem() == null) return;

                if (event.getInventory().getHolder() instanceof Holder) {
                    event.setCancelled(true);

                    Holder holder = (Holder) event.getInventory().getHolder();
                    Scroller scroller = holder.getScroller();
                    Player player = (Player) event.getWhoClicked();

                    if (scroller.getBackSlot() == event.getSlot()) {
                        scroller.getBackConsumer().accept(player);

                    } else if (scroller.getCustomItemActions().containsKey(event.getSlot())) {
                        scroller.getCustomItemActions().get(event.getSlot()).accept(player);

                    } else if (scroller.getNextPageSlot() == event.getSlot()) {

                        if (scroller.hasPage(holder.getPage() + 1)) {

                            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);

                            if (event.isRightClick() && scroller.skipPage) {
                                scroller.open(player, scroller.getTotalPages());
                                return;
                            }

                            scroller.open(player, holder.getPage() + 1);
                        }

                    } else if (scroller.getPreviousPageSlot() == event.getSlot()) {

                        if (scroller.hasPage(holder.getPage() - 1)) {

                            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);

                            if (event.isRightClick() && scroller.skipPage) {
                                scroller.open(player, 1);
                                return;
                            }

                            scroller.open(player, holder.getPage() - 1);
                        }

                    } else if (scroller.getAllowedSlots().contains(event.getSlot())) {

                        if (event.getCurrentItem().getType() != Material.AIR) {
                            holder.getScroller().getOnChooseItem().accept(player, event.getCurrentItem());
                            holder.getScroller().getOnChooseItemEvent().accept(event);
                        }

                    }
                }
            }
        }, plugin);
    }

    private String name;
    private int size;
    private List<ItemStack> items;
    private BiConsumer<Player, ItemStack> onChooseItem;
    private Consumer<InventoryClickEvent> onChooseItemEvent;
    private int nextPageSlot, previousPageSlot;
    private ItemStack nextPageItem, previousPageItem;
    private HashMap<Integer, ItemStack> customItems;
    private HashMap<Integer, Consumer<Player>> customItemActions;
    private List<Integer> allowedSlots;
    private HashMap<Integer, Inventory> pages;
    private int backSlot;
    private ItemStack backItem;
    private Consumer<Player> backConsumer;
    private boolean skipPage;

    private Scroller(Builder builder) {
        name = builder.name;
        size = builder.size;
        items = builder.items;
        onChooseItem = builder.onChooseItem;
        onChooseItemEvent = builder.onChooseItemEvent;
        nextPageSlot = builder.nextPageSlot;
        previousPageSlot = builder.previousPageSlot;
        nextPageItem = builder.nextPageItem;
        previousPageItem = builder.previousPageItem;
        customItems = builder.customItems;
        customItemActions = builder.customItemActions;
        allowedSlots = builder.allowedSlots;
        backSlot = builder.backSlot;
        backItem = builder.backItem;
        backConsumer = builder.backConsumer;
        skipPage = builder.skipPage;
        this.pages = new HashMap<>();
        build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private void build() {

        if (items.isEmpty()) {
            Inventory inventory = Bukkit.createInventory(new Holder(this, 1), size, name);

            if (backSlot != -1) inventory.setItem(backSlot, backItem);

            customItems.forEach(inventory::setItem);
            pages.put(1, inventory);
            return;
        }

        List<List<ItemStack>> lists = getPages(items, allowedSlots.size());
        int page = 1;

        for (List<ItemStack> list : lists) {
            Inventory inventory = Bukkit.createInventory(new Holder(this, page), size, name);

            int slot = 0;
            for (ItemStack it : list) {
                inventory.setItem(allowedSlots.get(slot), it);
                slot++;
            }

            customItems.forEach(inventory::setItem);

            inventory.setItem(previousPageSlot, editItem(previousPageItem.clone(), page - 1)); // se for a primeira página, não tem pra onde voltar
            inventory.setItem(nextPageSlot, editItem(nextPageItem.clone(), page + 1));

            if (backSlot != -1) inventory.setItem(backSlot, backItem);

            pages.put(page, inventory);
            page++;
        }

        pages.get(1).setItem(previousPageSlot, new ItemStack(Material.AIR)); // vai na primeira página e remove a flecha de ir pra trás
        pages.get(pages.size()).setItem(nextPageSlot, new ItemStack(Material.AIR)); // vai na última página e remove a flecha de ir pra frente
    }

    private ItemStack editItem(ItemStack item, int page) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(item.getItemMeta().getDisplayName().replace("<page>", page + ""));
        item.setItemMeta(meta);
        return item;
    }

    private <T> List<List<T>> getPages(Collection<T> c, Integer pageSize) { // créditos a https://stackoverflow.com/users/2813377/pscuderi
        List<T> list = new ArrayList<>(c);

        if (pageSize == null || pageSize <= 0 || pageSize > list.size()) pageSize = list.size();

        int numPages = (int) Math.ceil((double) list.size() / (double) pageSize);
        List<List<T>> pages = new ArrayList<>(numPages);

        for (int pageNum = 0; pageNum < numPages; )
            pages.add(list.subList(pageNum * pageSize, Math.min(++pageNum * pageSize, list.size())));

        return pages;
    }

    public int getTotalPages() {
        return pages.size();
    }

    private boolean hasPage(int page) {
        return pages.containsKey(page);
    }

    public void open(Player player) {
        open(player, 1);
    }

    public void open(Player player, int page) {
        player.openInventory(pages.get(page));
    }

    private BiConsumer<Player, ItemStack> getOnChooseItem() {
        return onChooseItem;
    }

    private Consumer<InventoryClickEvent> getOnChooseItemEvent() {
        return onChooseItemEvent;
    }

    public int getNextPageSlot() {
        return nextPageSlot;
    }

    public ItemStack getNextPageItem() {
        return nextPageItem;
    }

    public int getPreviousPageSlot() {
        return previousPageSlot;
    }

    public ItemStack getPreviousPageItem() {
        return previousPageItem;
    }

    private List<Integer> getAllowedSlots() {
        return allowedSlots;
    }

    private HashMap<Integer, Consumer<Player>> getCustomItemActions() {
        return customItemActions;
    }

    public HashMap<Integer, Inventory> getPages() {
        return pages;
    }

    private int getBackSlot() {
        return backSlot;
    }

    private Consumer<Player> getBackConsumer() {
        return backConsumer;
    }

    public static final class Builder {

        private final static List<Integer> ALLOWED_SLOTS = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34
                /* ,37,38,39,40,41,42,43 */); // slots para caso o inventário tiver 6 linhas
        private String name;
        private int size;
        private List<ItemStack> items;
        private BiConsumer<Player, ItemStack> onChooseItem;
        private Consumer<InventoryClickEvent> onChooseItemEvent;
        private int nextPageSlot;
        private int previousPageSlot;
        private ItemStack nextPageItem;
        private ItemStack previousPageItem;
        private int backSlot;
        private ItemStack backItem;
        private Consumer<Player> backConsumer;
        private HashMap<Integer, ItemStack> customItems;
        private HashMap<Integer, Consumer<Player>> customItemActions;
        private List<Integer> allowedSlots;
        private boolean skipPage;

        private Builder() {
            this.name = "";
            this.size = 45;
            this.items = new ArrayList<>();

            this.onChooseItem = (player, item) -> {
            };

            this.onChooseItemEvent = (clickEvent) -> {
            };

            this.nextPageSlot = 26;
            this.previousPageSlot = 18;
            this.customItems = new HashMap<>();
            this.customItemActions = new HashMap<>();
            this.allowedSlots = ALLOWED_SLOTS;
            this.backSlot = -1;

            this.backConsumer = player -> {
            };

            this.backItem = getBackFlecha();
            this.nextPageItem = getPageFlecha();
            this.previousPageItem = getPageFlecha();
            this.skipPage = false;
        }

        private ItemStack getBackFlecha() {
            ItemStack item = new ItemStack(Material.ARROW);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Voltar");
            item.setItemMeta(meta);
            return item;
        }

        private ItemStack getPageFlecha() {
            ItemStack item = new ItemStack(Material.ARROW);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Página <page>");
            item.setItemMeta(meta);
            return item;
        }

        public Builder withBackItem(int backSlot, ItemStack backItem, Consumer<Player> player) {
            this.backItem = backItem;
            this.backSlot = backSlot;
            this.backConsumer = player;
            return this;
        }

        public Builder withBackItem(int backSlot, Consumer<Player> player) {
            this.backSlot = backSlot;
            this.backConsumer = player;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSize(int size) {
            this.size = size;
            return this;
        }

        public Builder withItems(List<ItemStack> items) {
            this.items = items;
            return this;
        }

        public Builder withOnChooseItem(BiConsumer<Player, ItemStack> onChooseItem) {
            this.onChooseItem = onChooseItem;
            return this;
        }

        public Builder withOnChooseItemEvent(Consumer<InventoryClickEvent> clickEvent) {
            this.onChooseItemEvent = clickEvent;
            return this;
        }

        public Builder withNextPageSlot(int nextPageSlot) {
            this.nextPageSlot = nextPageSlot;
            return this;
        }

        public Builder withPreviousPageSlot(int previousPageSlot) {
            this.previousPageSlot = previousPageSlot;
            return this;
        }

        public Builder withNextPageItem(ItemStack nextPageItem) {
            this.nextPageItem = nextPageItem;
            return this;
        }

        public Builder withPreviousPageItem(ItemStack previousPageItem) {
            this.previousPageItem = previousPageItem;
            return this;
        }

        public Builder withCustomItem(int slot, ItemStack item) {
            this.customItems.put(slot, item);
            return this;
        }

        public Builder withCustomItem(int slot, ItemStack item, Consumer<Player> action) {
            this.customItems.put(slot, item);
            this.customItemActions.put(slot, action);
            return this;
        }

        public Builder withAllowedSlots(List<Integer> allowedSlots) {
            this.allowedSlots = allowedSlots;
            return this;
        }

        public Builder withSkipPage(boolean skipPage) {
            this.skipPage = skipPage;
            return this;
        }

        public Scroller build() {
            return new Scroller(this);
        }
    }

    public static final class Holder implements InventoryHolder {

        private Scroller scroller;
        private int page;

        public Holder(Scroller scroller, int page) {
            this.scroller = scroller;
            this.page = page;
        }

        public int getPage() {
            return page;
        }

        public Scroller getScroller() {
            return scroller;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
