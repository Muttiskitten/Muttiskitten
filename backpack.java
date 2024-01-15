import java.util.ArrayList;
import java.util.List;

import org.bukkit.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class BackpackPlugin extends JavaPlugin implements Listener {
    private BackpackItem backpackItem;
    private int backpackSize;

    @Override
    public void onEnable() {
        backpackItem = new BackpackItem();
        backpackSize = 36;
        
        BackpackListener backpackListener = new BackpackListener(backpackItem, backpackSize);
        Bukkit.getServer().getPluginManager().registerEvents(backpackListener, this);

        ItemStack backpack = createBackpackItem();
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "backpack_recipe"), backpack);
        recipe.shape("LLL", "L L", "LLL");
        recipe.setIngredient('L', Material.LEATHER);
	recipe.setIngredient('C',Material,CHEST);
        Bukkit.addRecipe(recipe);
    }

    private ItemStack createBackpackItem() {
        ItemStack backpack = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = backpack.getItemMeta();
        meta.setDisplayName("Backpack");
        backpack.setItemMeta(meta);
        return backpack;
    }
}

class BackpackItem {
    private final List<ItemStack> inventory;

    public BackpackItem() {
        this.inventory = new ArrayList<>();
    }

    public void addItem(ItemStack item) {
        inventory.add(item);
    }

    public void removeItem(ItemStack item) {
        inventory.remove(item);
    }

    public List<ItemStack> getInventory() {
        return inventory;
    }
}

class BackpackListener implements Listener {
    private final BackpackItem backpackItem;
    private final int backpackSize;

    public BackpackListener(BackpackItem backpackItem, int backpackSize) {
        this.backpackItem = backpackItem;
        this.backpackSize = backpackSize;
    }

    @EventHandler
    public void onSneakRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack chestplate = player.getInventory().getChestplate();

        if (event.getAction().name().contains("RIGHT_CLICK") && player.isSneaking() && chestplate != null
                && chestplate.getType() == Material.ENDER_CHEST) {
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item != null && item.getType() == Material.ENDER_CHEST && item.hasItemMeta()
                    && item.getItemMeta().getDisplayName().equals("Backpack")) {
                equipBackpack(player, item);
                player.openInventory(new BackpackInventory(player, backpackItem));
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Backpack")) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (backpackItem.getInventory().contains(clickedItem)) {
            backpackItem.removeItem(clickedItem);
            player.getInventory().addItem(clickedItem);
        } else if (event.getSlot() < backpackSize) {
            backpackItem.addItem(clickedItem);
            player.getInventory().removeItem(clickedItem);
        }
    }

    private void equipBackpack(Player player, ItemStack backpack) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack currentChestplate = playerInventory.getChestplate();

        if (currentChestplate != null && currentChestplate.isSimilar(backpack)) {
            playerInventory.setChestplate(null);
            backpackItem.removeItem(backpack);
        } else {
            playerInventory.setChestplate(backpack);
        }
    }
}

class BackpackInventory {
    private final Player player;
    private final BackpackItem backpackItem;

    public BackpackInventory(Player player, BackpackItem backpackItem) {
        this.player = player;
        this.backpackItem = backpackItem;
        openInventory();
    }

    private void openInventory() {
        Inventory inventory = Bukkit.createInventory(null, 36, "Backpack");

        for (int i = 0; i < backpackItem.getInventory().size(); i++) {
            ItemStack item = backpackItem.getInventory().get(i);

            if (item != null) {
                inventory.setItem(i, item);
            }
        }

        player.openInventory(inventory);
    }
}
