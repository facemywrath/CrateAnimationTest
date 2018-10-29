package facemywrath.slots.commands;

import java.io.File;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import facemywrath.slots.main.Main;
import facemywrath.slots.slots.SlotAnimation;
import facemywrath.slots.util.Events;

public class SlotCommand implements CommandExecutor {

	private File dataFolder;

	public SlotCommand(File dataFolder)
	{
		this.dataFolder = dataFolder;
		
		Events.listen(Main.getPlugin(Main.class), PlayerInteractEvent.class, event -> {
			if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
			Player player = event.getPlayer();
			if(!placing.containsKey(player)) return;
			String name = placing.get(player);
			if(SlotAnimation.isSlotMachine(event.getClickedBlock().getLocation()))
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4That block is already a slot machine. Try again."));
				return;
			}
			SlotAnimation.createSlotMachine(event.getClickedBlock().getLocation(),name);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou set the location for a new " + name + " slot machine."));
			placing.remove(player);
		});
	}
	
	private HashMap<Player, String> placing = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(!(sender instanceof Player))
		{
			sender.sendMessage("Only players can do this command");
			return true;
		}
		Player player = (Player) sender;
		File file1 = new File(dataFolder, "slots.yml");
		File file2 = new File(dataFolder, "slotTypes.yml");
		if(!file1.exists())
		{
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4ERROR: slots.yml FILE NOT GENERATED! RELOAD OR RESTART SERVER!"));
			return true;
		}
		if(!file2.exists())
		{
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4ERROR: slotTypes.yml FILE NOT GENERATED! RELOAD OR RESTART SERVER!"));
			return true;
		}
		FileConfiguration slots = YamlConfiguration.loadConfiguration(file1);
		FileConfiguration slotTypes = YamlConfiguration.loadConfiguration(file2);
		if(args.length == 0)
		{
			sendHelpMessage(player);
			return true;
		}
		if(args[0].equalsIgnoreCase("test"))
		{
			if(!player.hasPermission("slots.test"))
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4You don't have permission for that command."));
				return true;
			}
			if(args.length == 1)
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Must specify a name to test. Use &e\"/slot types\" &4to list them."));
				return true;
			}
			if(!slotTypes.isConfigurationSection("SlotTypes." + args[1]))
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4That slot type doesn't exist. You can use &e\"/slot types\" &4to list them."));
				return true;
			}
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTesting " + args[1]));
			SlotAnimation.openSlot(player, args[1]);
			return true;
		}
		if(args[0].equalsIgnoreCase("types"))
		{
			if(!player.hasPermission("slots.types"))
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4You don't have permission for that command."));
				return true;
			}
			listSlotTypes(slotTypes, player);
		}
		if(args[0].equalsIgnoreCase("create"))
		{
			if(!player.hasPermission("slots.create"))
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4You don't have permission for that command."));
				return true;
			}
			if(args.length == 1)
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Must specify a name for this slot machine. Use &e\"/slot types\" &4to list them."));
				return true;
			}
			if(!slotTypes.isConfigurationSection("SlotTypes." + args[1]))
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4That slot type doesn't exist. You can use &e\"/slot types\" &4to list them."));
				return true;
			}
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aNow right-click a block to set it as the slot machine block."));
			placing.put(player, args[1]);
			return true;
		}
		if(args[0].equalsIgnoreCase("token"))
		{
			if(!player.hasPermission("slots.token"))
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4You don't have permission for that command."));
				return true;
			}
			if(args.length == 1)
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Must specify a name for this token's slot machine. Use &e\"/slot types\" &4to list them."));
				return true;
			}
			if(!slotTypes.isConfigurationSection("SlotTypes." + args[1]))
			{
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4That slot type doesn't exist. You can use &e\"/slot types\" &4to list them."));
				return true;
			}
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou were given a " + args[1] + " Token"));
			player.getInventory().addItem(SlotAnimation.getToken(args[1]));
			return true;
		}
		return true;
	}

	private void listSlotTypes(FileConfiguration slotTypes, Player player)
	{
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aValid Slot Types: &3" + slotTypes.getConfigurationSection("SlotTypes").getKeys(false).stream().collect(Collectors.joining(", "))));
	}

	private void sendHelpMessage(Player player) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4S&cL&6O&eT &aM&2A&bC&3H&9I&1N&5E&dS"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "- \"&e/slot create (name)\" &7to create a new slot machine for that type of slot."));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "- \"&e/slot remove\" &7to remove an existing slot machine."));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "- \"&e/slot remove (id)\" &7to remove an existing slot machine by id."));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "- \"&e/slot list (page)\" &7to list existing slot machines by id."));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "- \"&e/slot types\" &7to list existing slot machine types by name."));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "- \"&e/slot test (name)\" &7to test an existing slot machine by id."));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "- \"&e/slot token (name) --(player) --(amount)\" &7to get or give a, or several, slot tokens to a player or yourself. If you don't specify a player or amount it defaults to you and to 1."));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4-&c-&6-&e-&a-&2-&b-&3-&9-&1-&5-&d-"));
	}

}
