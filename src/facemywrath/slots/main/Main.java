package facemywrath.slots.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import facemywrath.slots.commands.SlotCommand;
import facemywrath.slots.slots.SlotAnimation;
import facemywrath.slots.util.Events;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
	public List<Player> slotting = new ArrayList<>();
	@SuppressWarnings("unchecked")
	public void onEnable()
	{
		loadFiles();
		registerEvents();
		registerCommands();
		loadSlotMachines();
	}

	public void loadSlotMachines()
	{
		File file = new File(this.getDataFolder(), "slots.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		for(String key : config.getConfigurationSection("Slots").getKeys(false))
		{
			SlotAnimation.loadSlotMachine(new Location(Bukkit.getWorld(config.getString("Slots." + key + ".Location.World")),config.getInt("Slots." + key + ".Location.X"),config.getInt("Slots." + key + ".Location.Y"),config.getInt("Slots." + key + ".Location.Z")), config.getString("Slots." + key + ".Name"));
		}
	}

	public void loadFiles()
	{
		this.saveResource("slotTypes.yml", false);
		this.saveResource("slots.yml", false);
	}

	public void registerCommands()
	{
		this.getCommand("slot").setExecutor(new SlotCommand(this.getDataFolder()));
	}

	public void registerEvents()
	{
		HashMap<Player, Long> cooldowns = new HashMap<>();
		//PLAYER INTERACT EVENT: LOAD A SLOT MACHINE IF CLICKING WITH A TOKEN
		Events.listen(this, PlayerInteractEvent.class, event -> {
			if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
			Location loc = event.getClickedBlock().getLocation();
			Player player = event.getPlayer();
			if(!cooldowns.containsKey(player) || cooldowns.get(player) <= System.currentTimeMillis())cooldowns.put(player, System.currentTimeMillis()+100L);
			else if(cooldowns.get(player) > System.currentTimeMillis()) {
				event.setCancelled(true);
				return; 
			}
			if(!SlotAnimation.isSlotMachine(loc)) return;
			if(slotting.contains(player)) return;
			String name = SlotAnimation.getSlotMachine(loc);
			if(player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR) 
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Must be holding a " + name + " token to use this."));
				return;
			}
			ItemStack token = SlotAnimation.getToken(name);
			ItemStack item = player.getInventory().getItemInMainHand();
			if(!item.isSimilar(token))
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Must be holding a " + name + " token to use this."));
				return;
			}
			player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aPlaying " + name));
			SlotAnimation.openSlot(player, name);
		});
		//INVENTORY CLICK EVENT: CANCEL CLICKS IN A SLOT MACHINE, EXCEPT FOR THE CLOSE MENU BUTTON
		Events.listen(this, InventoryClickEvent.class, event -> {
			if(slotting.contains(event.getWhoClicked())) 
			{
				event.setCancelled(true);
				if(event.getSlot() == 0)
				{
					slotting.remove(event.getWhoClicked());
					this.getServer().getScheduler().runTaskLater(this, () -> event.getWhoClicked().closeInventory(), 5L);
				}
			}
		});
		//INVENTORY DRAG EVENT: CANCEL DRAGS IN A SLTO MACHINE.
		Events.listen(this, InventoryDragEvent.class, event -> {
			if(slotting.contains(event.getWhoClicked())) event.setCancelled(true);
		});
	}

}
