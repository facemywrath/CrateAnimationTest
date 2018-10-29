package facemywrath.slots.slots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SlotItem {
	
	private ItemStack item;
	private List<String> commands = new ArrayList<>();
	private int minimum;
	private int maximum;
	
	public SlotItem setItem(ItemStack item)
	{
		this.item = item;
		return this;
	}
	
	public SlotItem addCommands(List<String> list)
	{
		this.commands.addAll(list);
		return this;
	}
	
	public SlotItem addCommand(String cmd)
	{
		this.commands.add(cmd);
		return this;
	}
	
	public SlotItem setMinimumAmount(int min)
	{
		this.minimum = min;
		return this;
	}
	
	public SlotItem setMaximumAmount(int max)
	{
		this.maximum = max;
		return this;
	}
	
	public ItemStack getItem() {
		return item;
	}

	public List<String> getCommands() {
		return commands;
	}

	public int getMinimum() {
		return minimum;
	}

	public int getMaximum() {
		return maximum;
	}

	public void doCommands(Player player, int amount)
	{
		if(commands.isEmpty())  return;
		for(String command : commands)
		{
			if(command.contains("<amount>")) command = command.replaceAll("<amount>", "" + amount);
			if(command.contains("<player>")) command = command.replaceAll("<player>", "" + player.getName());
			if(command.startsWith("op: "))
			{
				command = command.substring(4, command.length());
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
				continue;
			}
			Bukkit.getServer().dispatchCommand(player, command);
		}
	}

}
