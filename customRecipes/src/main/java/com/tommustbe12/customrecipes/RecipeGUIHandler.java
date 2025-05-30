package com.tommustbe12.customrecipes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class RecipeGUIHandler implements Listener {

    private final Plugin plugin;
    private static final String TITLE = ChatColor.DARK_GREEN + "Custom Recipe Editor";

    // 3x3 crafting grid slots set
    private static final Set<Integer> CRAFTING_SLOTS = Set.of(
            1, 2, 3,
            10, 11, 12,
            19, 20, 21
    );

    private static final int OUTPUT_SLOT = 22;
    private static final int SUBMIT_SLOT = 24;
    private static final int CANCEL_SLOT = 26;

    public RecipeGUIHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void openRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        // fill all other slots with black glass panes
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta paneMeta = blackPane.getItemMeta();
        paneMeta.setDisplayName(" ");
        blackPane.setItemMeta(paneMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, blackPane);
        }

        // clear slots for next use
        for (int slot : CRAFTING_SLOTS) {
            gui.setItem(slot, null);
        }
        gui.setItem(OUTPUT_SLOT, null);

        // submit (lime wool)
        ItemStack submit = new ItemStack(Material.LIME_WOOL);
        ItemMeta submitMeta = submit.getItemMeta();
        submitMeta.setDisplayName(ChatColor.GREEN + "Submit");
        submit.setItemMeta(submitMeta);
        gui.setItem(SUBMIT_SLOT, submit);

        // cancel (red wool)
        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancel.setItemMeta(cancelMeta);
        gui.setItem(CANCEL_SLOT, cancel);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!TITLE.equals(event.getView().getTitle())) return;

        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();

        // prevent interaction except for all allowed slots
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            // Interaction outside custom inventory (player inventory)
            return; // allow normal player inventory interaction
        }

        // Allowed slots for interaction:
        boolean isCraftingOrOutput = CRAFTING_SLOTS.contains(slot) || slot == OUTPUT_SLOT;
        boolean isButton = slot == SUBMIT_SLOT || slot == CANCEL_SLOT;

        if (!isCraftingOrOutput && !isButton) {
            event.setCancelled(true); // block interaction on black panes and others
            return;
        }

        // Handle Submit button
        if (slot == SUBMIT_SLOT && event.getClick().isLeftClick()) {
            event.setCancelled(true);
            // recipe submission
            Inventory inv = event.getInventory();
            try {
                processRecipeSubmission(player, inv);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Error while submitting recipe: " + e.getMessage());
                e.printStackTrace();
            }
            player.closeInventory();
            return;
        }

        // Handle Cancel button
        if (slot == CANCEL_SLOT && event.getClick().isLeftClick()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Recipe creation canceled.");
            player.closeInventory();
            return;
        }

        // allows user to interact with the inventory while in the custom gui
        // dont cancel event here otherwise it will not work :(
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!TITLE.equals(event.getView().getTitle())) return;

        // dragging can only work in crafting grid or output
        for (int slot : event.getRawSlots()) {
            if (!CRAFTING_SLOTS.contains(slot) && slot != OUTPUT_SLOT
                    && slot != SUBMIT_SLOT && slot != CANCEL_SLOT) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void processRecipeSubmission(Player player, Inventory inv) {
        // grab contents of 3x3 grid
        ItemStack[][] grid = new ItemStack[3][3];
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = (row * 9) + (col + 1); // Your grid mapping
                ItemStack item = inv.getItem(slot);
                grid[row][col] = (item != null && item.getType() != Material.AIR) ? item.clone() : null;
            }
        }

        // get non-empty bounds
        int minRow = 3, maxRow = -1, minCol = 3, maxCol = -1;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (grid[row][col] != null) {
                    minRow = Math.min(minRow, row);
                    maxRow = Math.max(maxRow, row);
                    minCol = Math.min(minCol, col);
                    maxCol = Math.max(maxCol, col);
                }
            }
        }

        if (minRow > maxRow || minCol > maxCol) {
            player.sendMessage(ChatColor.RED + "You must add at least one ingredient!");
            return;
        }

        // ingredient mapping, build shape
        List<String> shape = new ArrayList<>();
        Map<Character, RecipeChoice> choices = new HashMap<>();
        char currentChar = 'A';

        for (int row = minRow; row <= maxRow; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = minCol; col <= maxCol; col++) {
                ItemStack item = grid[row][col];
                if (item == null) {
                    line.append(' ');
                } else {
                    line.append(currentChar);
                    choices.put(currentChar, new RecipeChoice.ExactChoice(item));
                    currentChar++;
                }
            }
            shape.add(line.toString());
        }

        // output
        ItemStack output = inv.getItem(OUTPUT_SLOT);
        if (output == null || output.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must set an output item!");
            return;
        }

        // register
        NamespacedKey key = new NamespacedKey(plugin, "custom_recipe_" + System.currentTimeMillis());
        ShapedRecipe recipe = new ShapedRecipe(key, output);
        recipe.shape(shape.toArray(new String[0]));

        for (Map.Entry<Character, RecipeChoice> entry : choices.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }

        Bukkit.addRecipe(recipe);

        player.sendMessage(ChatColor.GREEN + "Custom recipe registered!");
    }

    private void saveRecipeToConfig(String key, ItemStack[] ingredients, ItemStack output) {
        FileConfiguration config = plugin.getConfig();

        // save the ingredients
        for (int i = 0; i < ingredients.length; i++) {
            ItemStack item = ingredients[i];
            String path = "recipes." + key + ".ingredients." + i;
            if (item == null) {
                config.set(path, null);
            } else {
                config.set(path, item);
            }
        }

        // output save
        config.set("recipes." + key + ".output", output);

        plugin.saveConfig();
    }
}
