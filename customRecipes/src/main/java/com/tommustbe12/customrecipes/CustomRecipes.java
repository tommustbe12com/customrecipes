package com.tommustbe12.customrecipes;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class CustomRecipes extends JavaPlugin {

    private static CustomRecipes instance;
    private RecipeGUIHandler guiHandler;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        guiHandler = new RecipeGUIHandler(this);
        getServer().getPluginManager().registerEvents(guiHandler, this);

        getCommand("recipe").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            guiHandler.openRecipeGUI(player);
            return true;
        });

        loadRecipesFromConfig();
    }

    @Override
    public void onDisable() {
        // nothing, maybe will add log later
    }

    public static CustomRecipes getInstance() {
        return instance;
    }

    public void loadRecipesFromConfig() {
        FileConfiguration config = getConfig();
        ConfigurationSection recipesSection = config.getConfigurationSection("recipes");
        if (recipesSection == null) {
            getLogger().info("No saved recipes found in config.");
            return;
        }

        Set<String> recipeIds = recipesSection.getKeys(false);
        for (String id : recipeIds) {
            ConfigurationSection recipeSection = recipesSection.getConfigurationSection(id);
            if (recipeSection == null) continue;

            ItemStack[] ingredients = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                ItemStack ingredient = recipeSection.getItemStack("ingredients." + i);
                if (ingredient == null) {
                    ingredient = new ItemStack(org.bukkit.Material.AIR);
                }
                ingredients[i] = ingredient;
            }

            ItemStack output = recipeSection.getItemStack("output");
            if (output == null || output.getType() == org.bukkit.Material.AIR) {
                getLogger().warning("Recipe " + id + " has no valid output, skipping.");
                continue;
            }

            NamespacedKey key = new NamespacedKey(this, "custom_recipe_" + id);
            ShapedRecipe recipe = new ShapedRecipe(key, output);
            recipe.shape("abc", "def", "ghi");

            char c = 'a';
            for (ItemStack ingredient : ingredients) {
                if (ingredient.getType() != org.bukkit.Material.AIR) {
                    recipe.setIngredient(c, ingredient.getType());
                }
                c++;
            }

            getServer().addRecipe(recipe);
            getLogger().info("Loaded recipe: " + id);
        }
    }

    public void saveRecipeToConfig(String id, ItemStack[] ingredients, ItemStack output) {
        FileConfiguration config = getConfig();
        String basePath = "recipes." + id;

        for (int i = 0; i < 9; i++) {
            config.set(basePath + ".ingredients." + i, ingredients[i]);
        }

        config.set(basePath + ".output", output);

        saveConfig();
        getLogger().info("Saved recipe " + id + " to config.");
    }
}
