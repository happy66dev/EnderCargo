package fr.birdo.endercargo;

import fr.birdo.endercargo.Utils.EnderCargoData;
import fr.birdo.endercargo.items.EnderCargoAdvancedOutput;
import fr.birdo.endercargo.items.EnderCargoInput;
import fr.birdo.endercargo.items.EnderCargoLinker;
import fr.birdo.endercargo.items.EnderCargoOutput;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class EnderCargo extends JavaPlugin implements SlimefunAddon {

    public static String dataFolderPath;

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            getLogger().log(Level.SEVERE, "本插件需要 鬼斩前置库插件(GuizhanLibPlugin) 才能运行!");
            getLogger().log(Level.SEVERE, "从此处下载: https://50l.cc/gzlib");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new EnderCargoInput(this), this);
        getServer().getPluginManager().registerEvents(new EnderCargoOutput(this), this);
        getServer().getPluginManager().registerEvents(new EnderCargoAdvancedOutput(this), this);
        getServer().getPluginManager().registerEvents(new EnderCargoLinker(this), this);

        //Creating a new category
        ItemStack itemGroupItem = new CustomItemStack(Material.ENDER_EYE, "&5末影接口", "", "&a> 单击打开");
        NamespacedKey itemGroupId = new NamespacedKey(this, "endercargo_category");
        ItemGroup itemGroup = new ItemGroup(itemGroupId, itemGroupItem);

        //Create Slimefun Itemstacks
        SlimefunItemStack ender_input = new SlimefunItemStack("ENDER_INPUT", EnderCargoInput.blockMaterial, EnderCargoInput.blockName, "&7利用末影的力量", "&7跨维度运输物品");
        SlimefunItemStack ender_output = new SlimefunItemStack("ENDER_OUTPUT", EnderCargoOutput.blockMaterial, EnderCargoOutput.blockName, "&7利用末影的力量", "&7跨维度运输物品");
        SlimefunItemStack advanced_ender_output = new SlimefunItemStack("ADVANCED_ENDER_OUTPUT", EnderCargoAdvancedOutput.blockMaterial, EnderCargoAdvancedOutput.blockName, "&7利用末影的力量", "&7跨维度运输物品");
        SlimefunItemStack ender_linker = new SlimefunItemStack("ENDER_LINKER", EnderCargoLinker.itemMaterial, EnderCargoLinker.itemName, "&7&eShift+右击&7 链接两个接口", "&7&e左击&7 重置");

        //Creating Recipes
        ItemStack[] ender_input_recipe = {null, SlimefunItems.INFUSED_HOPPER, null, SlimefunItems.ENDER_LUMP_3, SlimefunItems.CARGO_CONNECTOR_NODE, SlimefunItems.ENDER_LUMP_3, null, SlimefunItems.INFUSED_HOPPER, null};
        ItemStack[] ender_output_recipe = {null, SlimefunItems.INFUSED_HOPPER, null, SlimefunItems.MAGIC_LUMP_3, SlimefunItems.CARGO_CONNECTOR_NODE, SlimefunItems.MAGIC_LUMP_3, null, SlimefunItems.INFUSED_HOPPER, null};
        ItemStack[] advanced_ender_output_recipe = {null, SlimefunItems.CARGO_MOTOR, null, SlimefunItems.REINFORCED_PLATE, ender_output, SlimefunItems.REINFORCED_PLATE, null, SlimefunItems.CARGO_MOTOR, null};
        ItemStack[] ender_linker_recipe = {SlimefunItems.HARDENED_METAL_INGOT, SlimefunItems.ESSENCE_OF_AFTERLIFE, SlimefunItems.HARDENED_METAL_INGOT, SlimefunItems.HARDENED_METAL_INGOT, new ItemStack(Material.STICK), SlimefunItems.HARDENED_METAL_INGOT, null, new ItemStack(Material.STICK), null};

        //Registering Items
        SlimefunItem ender_input_item = new SlimefunItem(itemGroup, ender_input, RecipeType.ENHANCED_CRAFTING_TABLE, ender_input_recipe);
        ender_input_item.register(this);
        SlimefunItem ender_output_item = new SlimefunItem(itemGroup, ender_output, RecipeType.ENHANCED_CRAFTING_TABLE, ender_output_recipe);
        ender_output_item.register(this);
        SlimefunItem advanced_ender_output_item = new SlimefunItem(itemGroup, advanced_ender_output, RecipeType.ENHANCED_CRAFTING_TABLE, advanced_ender_output_recipe);
        advanced_ender_output_item.register(this);
        SlimefunItem ender_linker_item = new SlimefunItem(itemGroup, ender_linker, RecipeType.ENHANCED_CRAFTING_TABLE, ender_linker_recipe);
        ender_linker_item.register(this);

        //Creating data file and folder
        dataFolderPath = getDataFolder().getAbsolutePath();
        File folder = new File(dataFolderPath);
        if (!folder.exists())
            folder.mkdir();
        File enderCargoData = new File(dataFolderPath + "/EnderCargoData.json");
        if (!enderCargoData.exists()) {
            try {
                enderCargoData.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //Run ticker
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            setContent();
        }, 0L, 1L);
    }

    @Override
    public void onDisable() {
        // Logic for disabling the plugin...
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/SlimefunGuguProject/Ender-Cargo/issues";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        /*
         * You will need to return a reference to your Plugin here.
         * If you are using your main class for this, simply return "this".
         */
        return this;
    }

    public void setContent() {
        if (EnderCargoData.getLinkedCargo() != null) {
            for (String string : EnderCargoData.getLinkedCargo()) {

                String[] str = string.split(" ");
                Location location = new Location(Bukkit.getWorld(str[0]), Integer.parseInt(str[1]), Integer.parseInt(str[2]), Integer.parseInt(str[3]));

                Container outputContainer = (Container) location.getWorld().getBlockAt(location).getState();
                ItemStack[] outputContent = outputContainer.getInventory().getStorageContents();

                outputContainer.update();

                BukkitRunnable task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (location.getBlock().getType() == Material.DISPENSER && EnderCargoData.getInputCargo(location) != null && EnderCargoData.getInputCargo(location).getBlock().getType() == EnderCargoInput.blockMaterial) {

                            Container outputContainerTick = (Container) location.getWorld().getBlockAt(location).getState();
                            ItemStack[] outputContentTick = outputContainerTick.getInventory().getStorageContents();

                            Container inputContainer = (Container) EnderCargoData.getInputCargo(location).getWorld().getBlockAt(EnderCargoData.getInputCargo(location)).getState();
                            ItemStack[] inputContent = inputContainer.getInventory().getStorageContents();

                            for (int i = 0; i < 9; i++) {
                                int nb;
                                if (outputContent[i] != null) {
                                    if (outputContentTick[i] == null) {
                                        nb = outputContent[i].getAmount();
                                    } else {
                                        nb = outputContent[i].getAmount() - outputContentTick[i].getAmount();
                                    }
                                    ItemStack item = inputContent[i];
                                    item.setAmount(inputContent[i].getAmount() - nb);
                                    inputContainer.getInventory().setItem(i, item);
                                }
                            }
                            outputContainerTick.getInventory().setContents(inputContainer.getInventory().getContents());
                        }
                    }
                };
                task.runTaskLater(this, 1);
            }
        }
    }
}
