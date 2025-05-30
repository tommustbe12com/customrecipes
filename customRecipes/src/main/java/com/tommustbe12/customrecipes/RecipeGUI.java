package com.tommustbe12.customrecipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class RecipeGUI implements CommandExecutor, Listener {
    private static final Map<UUID, Inventory> playerGUIs = new HashMap<>();

    public RecipeGUI() {
        Bukkit.getPluginManager().registerEvents(this, CustomRecipes.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return false;

        Inventory gui = Bukkit.createInventory(p, 27, "Create Custom Recipe");

        // 3x3 custom crafting recipe grid
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, new ItemStack(Material.AIR));
        }

        // result slot 13
        gui.setItem(13, new ItemStack(Material.AIR));

        // save and cancel buttons
        gui.setItem(22, new ItemStack(Material.LIME_WOOL)); // save
        gui.setItem(23, new ItemStack(Material.RED_WOOL));  // cancel

        p.openInventory(gui);
        playerGUIs.put(p.getUniqueId(), gui);
        return true;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (!playerGUIs.containsKey(p.getUniqueId())) return;

        if (!e.getView().getTitle().equals("Create Custom Recipe")) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();
        if (slot >= 0 && slot < 27) {
            if (slot < 9 || slot == 13) {
                e.setCancelled(false); // allow editing grid and output
            } else if (slot == 22) {
                saveRecipe(p, e.getInventory());
                p.closeInventory();
            } else if (slot == 23) {
                p.closeInventory();
                p.sendMessage("§cRecipe creation cancelled.");
            }
        }
    }

    private void saveRecipe(Player p, Inventory inv) {
        List<String> shape = Arrays.asList("ABC", "DEF", "GHI");
        Map<Character, ItemStack> ingredients = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                ingredients.put((char) ('A' + i), item.clone());
            }
        }
        ItemStack result = inv.getItem(13);
        if (result == null || result.getType() == Material.AIR) {
            p.sendMessage("§cYou must add a result item!");
            return;
        }

        String id = "recipe_" + System.currentTimeMillis();
        RecipeManager.saveRecipe(id, shape, ingredients, result);
        RecipeManager.loadRecipes();

        p.sendMessage("§aRecipe saved!");
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }
}
