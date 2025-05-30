# customrecipes
A Minecraft plugin made in Java with Maven in Eclipse IDE. Brought it to GitHub because it's one of my main plugins that I don't want to lose.

## Version and Info
It uses Bukkit API and is built with Maven. It is a standard jar file and is 17kb in size.
It is meant to be used with 1.21.5 of Paper or later, if you want you can contact me about building it for an earlier version but the one here is compiled for that version and above. As long as libraries don't change drastically it should be good for a while. Contact me at admin@tommustbe12.com if you have any issues.

Java 1.21 servers require Java SE 21, as does this plugin. So it really shouldn't be a problem for the Java SE requirement.

## Usage
Just add the plugin to your server. It is Paper/Bukkit compatible. If it worked, you should see a /recipe command available to operators. Use it to create your first custom recipe.

Keep in mind that after creating a recipe, that item that is craftable via that new recipe will show up in the auto crafting list for selection. That means that you can't necessarily 'hide' a recipe, it will be able to be found in the list of recipes that are craftable in the Knowledge Book. Only if you have unlocked the right ingredients though, like regular Minecraft.

### GUI
This is the GUI that pops up when you use /recipe. It's pretty intuitive, but it might be a little confusing.
To create a recipe, take items from your inventory (just so you know, when you drop them in and submit your custom recipe, they will be lost and not given back to you) and drop them in the way you want it.
Finally, add an output item (custom NBT data is supported!) in the output slot. Then, click the green wool (Submit) item.

You added your first recipe.

![image](https://github.com/user-attachments/assets/f26b29ac-4fe7-4b24-bf60-323fac4786e1)

### Persistence
Custom recipes are persistent if you don't mess with the config. In default setups, it will create its own config.yml file that saves all the custom recipes you have made for future use even after server restarts.

You can also check server logs to see if these custom recipes in the config loaded, it will (for each recipe) say 'loaded recipe recipe_id' as a server log.

### Shaped/shapeless recipes.
As you may know, recipes might be shaped (have to be in a fixed position) or shapeless (can be put in the format required any way in the crafting table).
This plugin uses a general shapeless approach.

If you, for example, set a recipe to be one block crafts to another block, like a dirt block to a sand block, that dirt block can be placed **anywhere** on the crafting grid to create a sand block.
However, if you have stone and a dirt on top of it = a sand block, that will mean anywhere you put a stone and a dirt on top of it would equal a sand block, no matter where.

The only exception to this is if you have an item that can't move because it is 3 blocks in either direction. If it is 3 blocks by 3 blocks in both directions, that will mean that you can't put it anywhere in the crafting grid because thats the only way it **can** go.

I think it's pretty easy to understand what I'm saying. Contact me if you have any questions.
