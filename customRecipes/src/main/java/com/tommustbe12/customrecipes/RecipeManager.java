package com.tommustbe12.customrecipes;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

public class RecipeManager {
    public static void saveRecipe(String id, List<String> shape, Map<Character, ItemStack> ingredients, ItemStack result) {
        var config = CustomRecipes.getInstance().getConfig();
        String base = "recipes." + id;

        config.set(base + ".shape", shape);

        for (Map.Entry<Character, ItemStack> entry : ingredients.entrySet()) {
            ItemStack item = entry.getValue();
            String path = base + ".ingredients." + entry.getKey();
            config.set(path + ".material", item.getType().name());

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) config.set(path + ".name", meta.getDisplayName());
                if (meta.hasLore()) config.set(path + ".lore", meta.getLore());
                if (meta.hasEnchants()) {
                    Map<String, Integer> enchants = new HashMap<>();
                    for (var e : meta.getEnchants().entrySet()) {
                        enchants.put(e.getKey().getKey().getKey(), e.getValue());
                    }
                    config.set(path + ".enchants", enchants);
                }
            }
        }

        String resPath = base + ".result";
        config.set(resPath + ".material", result.getType().name());
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) config.set(resPath + ".name", meta.getDisplayName());
            if (meta.hasLore()) config.set(resPath + ".lore", meta.getLore());
            if (meta.hasEnchants()) {
                Map<String, Integer> enchants = new HashMap<>();
                for (var e : meta.getEnchants().entrySet()) {
                    enchants.put(e.getKey().getKey().getKey(), e.getValue());
                }
                config.set(resPath + ".enchants", enchants);
            }
        }

        CustomRecipes.getInstance().saveConfig();
    }

    public static void loadRecipes() {
        var config = CustomRecipes.getInstance().getConfig();
        ConfigurationSection section = config.getConfigurationSection("recipes");
        if (section == null) return;

        // go thru every recipe config has
        for (String id : section.getKeys(false)) {
            ConfigurationSection rSec = section.getConfigurationSection(id);
            if (rSec == null) continue;

            List<String> shape = rSec.getStringList("shape");
            ItemStack result = buildItem(rSec.getConfigurationSection("result"));
            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(CustomRecipes.getInstance(), id), result);
            recipe.shape(shape.toArray(new String[0]));

            ConfigurationSection ingSec = rSec.getConfigurationSection("ingredients");
            if (ingSec != null) {
                for (String key : ingSec.getKeys(false)) {
                    char k = key.charAt(0);
                    ItemStack item = buildItem(ingSec.getConfigurationSection(key));
                    recipe.setIngredient(k, new RecipeChoice.ExactChoice(item));
                }
            }

	    // add bukkit recipe from config
            Bukkit.addRecipe(recipe);
        }
    }

    private static ItemStack buildItem(ConfigurationSection sec) {
        if (sec == null) return new ItemStack(Material.STONE);
        Material mat = Material.matchMaterial(sec.getString("material", "STONE"));
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (sec.contains("name")) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', sec.getString("name")));
        if (sec.contains("lore")) meta.setLore(sec.getStringList("lore"));
        if (sec.contains("enchants")) {
            for (String e : sec.getConfigurationSection("enchants").getKeys(false)) {
                Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(e));
                int level = sec.getInt("enchants." + e);
                if (ench != null) meta.addEnchant(ench, level, true);
            }
        }
        item.setItemMeta(meta);
        return item;
    }
}
