package facemywrath.cat.main;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class LotteryAnimation extends Animation<Inventory> {

	private Player player;

	public LotteryAnimation(Main main, Player player) {
		super(main);
		double speed = 2;
		this.player = player;
		this.addFrame(inv -> {
			for(int i = 0; i < inv.getSize(); i++)
			{
				inv.setItem(i, new ItemCreator(Material.WHITE_STAINED_GLASS_PANE).name(" ").amount(1).build());
			}
			inv.setItem(0, new ItemCreator(Material.RED_STAINED_GLASS_PANE).name("CLOSE MENU").lore("Forfeit winnings and", "close this menu early.").build());
			inv.setItem(37, new ItemCreator(Material.LIME_STAINED_GLASS_PANE).name("WINNING ROW").amount(1).build());
			inv.setItem(43, new ItemCreator(Material.LIME_STAINED_GLASS_PANE).name("WINNING ROW").amount(1).build());
			for(int i = 2; i < 7; i++)
			{
				inv.setItem(i, getRandomItem());
			}
		}, 1L);
		for(int x = 0; x < 45; x++)
		{
			this.addFrame(inv -> {
				for(int i = 38; i < 43; i++)
				{
					inv.setItem(i, inv.getItem(i-9));
				}
				for(int i = 29; i < 34; i++)
				{
					inv.setItem(i, inv.getItem(i-9));
				}
				for(int i = 20; i < 25; i++)
				{
					inv.setItem(i, inv.getItem(i-9));
				}
				for(int i = 11; i < 16; i++)
				{
					inv.setItem(i, inv.getItem(i-9));
				}
				for(int i = 2; i < 7; i++)
				{
					inv.setItem(i, getRandomItem());
				}
			}, (long) (speed), 1);
			speed*=1.04;
		}
		this.addFrame(inv -> {
			HashMap<Material, Integer> amounts = new HashMap<>();
			for(int i = 38; i < 43; i++)
			{
				if(inv.getItem(i) != null && inv.getItem(i).getType() != Material.AIR)
				{
					Material mat = inv.getItem(i).getType();
					if(amounts.containsKey(mat))
						amounts.put(mat, amounts.get(mat)*inv.getItem(i).getAmount() + inv.getItem(i).getAmount());
					else
						amounts.put(mat, inv.getItem(i).getAmount());
				}
			}
			String str = ChatColor.translateAlternateColorCodes('&', "&9You won ");
			if(amounts.isEmpty()) str += "NOTHING!";
			amounts.keySet().stream().forEach(mat -> player.getInventory().addItem(new ItemStack(mat, amounts.get(mat))));
			str += amounts.keySet().stream().map(mat -> amounts.get(mat) + " " + StringUtils.capitaliseAllWords(mat.toString().toLowerCase().replaceAll("_", " "))).collect(Collectors.joining(", "));
			player.sendMessage(str);
			player.closeInventory();
		}, 30L);
	}

	private ItemStack getRandomItem()
	{
		return new ItemStack(getRandomMaterial(), getWeightedAmount());
	}
	
	private int getWeightedAmount()
	{
		int i = ThreadLocalRandom.current().nextInt(0, 100);
		if(i < 3) return 7;
		if(i < 7) return 6;
		if(i < 15) return 5;
		if(i < 30) return 4;
		if(i < 55) return 3;
		if(i < 78) return 2;
		return 1;
	}

	private Material getRandomMaterial()
	{
		int i = ThreadLocalRandom.current().nextInt(0, 100);
		if(i < 5) return Material.DIAMOND;
		if(i < 12) return Material.GOLD_INGOT;
		if(i < 25) return Material.IRON_INGOT;
		if(i < 40) return Material.COAL;
		if(i < 60) return Material.COBBLESTONE;
		if(i < 96) return Material.AIR;
		if(i < 97) return Material.IRON_BLOCK;
		if(i < 98) return Material.GOLD_BLOCK;
		if(i < 99) return Material.DIAMOND_BLOCK;
		return Material.AIR;
	}
}
