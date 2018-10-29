package facemywrath.slots.slots;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import facemywrath.slots.main.Main;
import facemywrath.slots.util.Animation;
import facemywrath.slots.util.ItemCreator;
import facemywrath.slots.util.WeightedRandom;
import net.md_5.bungee.api.ChatColor;

public class SlotAnimation extends Animation<Inventory> {

	public static HashMap<String, List<Location>> slots = new HashMap<>();

	public static void openSlot(Player player, String name)
	{
		Main main = Main.getPlugin(Main.class);
		main.slotting.add(player);
		Inventory inv = Bukkit.createInventory(player, 45, "Slot Machine: " + name);
		player.openInventory(inv);
		SlotAnimation anim = new SlotAnimation(main, name, player);
		main.getServer().getScheduler().runTaskLater(main, () -> anim.animate(inv), 1L);;
	}

	public static ItemStack getToken(String machine)
	{
		return new ItemCreator(Material.GHAST_TEAR).name("&aSlot Token: " + machine).amount(1).lore("Use this at a " + machine + " slot machine").build();
	}

	public static boolean isSlotMachine(Location loc)
	{
		for(List<Location> lists : slots.values())
		{
			for(Location location : lists)
			{
				if(location.equals(loc))
					return true;
			}
		}
		return false;
	}

	public static String getSlotMachine(Location loc)
	{
		for(String name : slots.keySet())
		{
			for(Location location : slots.get(name))
			{
				if(location.equals(loc))
					return name;
			}
		}
		return null;
	}

	public static void loadSlotMachine(Location loc, String name)
	{
		if(slots.containsKey(name))
		{
			List<Location> locs = slots.get(name);
			locs.add(loc);
			slots.put(name, locs);
		}else
			slots.put(name, Arrays.asList(loc));
		@SuppressWarnings("unchecked")
		Animation<String> particles = new Animation<>(Main.getPlugin(Main.class)).addFrame(str -> {
			if(slots.containsKey(str))
			{
				for(Location loc1 : slots.get(str))
				{
					loc1 = loc1.clone();
					loc1.add(new Vector(0.5 + ThreadLocalRandom.current().nextInt(-100,100)/100.0,0.5 + ThreadLocalRandom.current().nextInt(-100,100)/100.0,0.5 + ThreadLocalRandom.current().nextInt(-100,100)/100.0));
					loc1.getWorld().spawnParticle(Particle.PORTAL, loc1, 3, 0.1, 0.1, 0.1, 0f);
				}
			}
		}, 10L, 3).setLooping(true, 10L);
		particles.animate(name);
	}

	public static void createSlotMachine(Location loc, String name)
	{
		Main main = Main.getPlugin(Main.class);
		File file = new File(main.getDataFolder(), "slots.yml");
		FileConfiguration slots = YamlConfiguration.loadConfiguration(file);
		int i = 0;
		if(slots.contains("Slots"))
			i = slots.getConfigurationSection("Slots").getKeys(false).size();
		slots.set("Slots." + i + ".Name", name);
		slots.set("Slots." + i + ".Location.World", loc.getWorld().getName());
		slots.set("Slots." + i + ".Location.X", loc.getBlockX());
		slots.set("Slots." + i + ".Location.Y", loc.getBlockY());
		slots.set("Slots." + i + ".Location.Z", loc.getBlockZ());
		try {
			slots.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadSlotMachine(loc, name);
	}

	private String name;
	private WeightedRandom<SlotItem> items = new WeightedRandom<>();
	private WeightedRandom<Integer> amounts = new WeightedRandom<>();
	private ItemStack filler;

	private void loadItems(ConfigurationSection section)
	{
		filler = new ItemCreator(Material.getMaterial(section.getString("Filler"))).name(" ").amount(1).build();
		for(String key : section.getConfigurationSection("Items").getKeys(false))
		{
			ConfigurationSection sec = section.getConfigurationSection("Items." + key);
			if(sec.getString("Material").equalsIgnoreCase("FILLER"))
			{
				SlotItem item = new SlotItem().setItem(filler).setMinimumAmount(1).setMaximumAmount(1);
				items.add(sec.getInt("Weight"), item);
			}
			else
			{
				SlotItem item = new SlotItem().
						setItem(new ItemCreator(
								Material.getMaterial(
										sec.getString("Material")))
								.lore(
										sec.getStringList("Lore"))
								.name(
										sec.getString("Display"))
								.build())
						.setMinimumAmount(
								sec.getInt("Amount.Min"))
						.setMaximumAmount(
								sec.getInt("Amount.Max"))
						.addCommands(
								sec.getStringList("Commands"));
				items.add(sec.getInt("Weight"), item);
			}
		}
	}
	private void loadAmounts(ConfigurationSection section)
	{
		for(String key : section.getConfigurationSection("Amounts").getKeys(false))
		{
			if(!StringUtils.isNumeric(key)) continue;
			amounts.add(section.getInt("Amounts." + key), Integer.parseInt(key));
		}
	}

	public SlotAnimation(Main main, String name, Player player) {
		super(main);
		File file = new File(main.getDataFolder(), "slotTypes.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		loadItems(config.getConfigurationSection("SlotTypes." + name));
		loadAmounts(config.getConfigurationSection("SlotTypes." + name));
		double speed = 2;
		filler = new ItemCreator(Material.WHITE_STAINED_GLASS_PANE).name(" ").amount(1).build();
		this.addFrame(inv -> {
			for(int i = 0; i < inv.getSize(); i++)
			{
				inv.setItem(i, filler);
			}
			inv.setItem(0, new ItemCreator(Material.RED_STAINED_GLASS_PANE).name("CLOSE MENU").lore("Forfeit winnings and", "close this menu early.").build());
			inv.setItem(19, new ItemCreator(Material.LIME_STAINED_GLASS_PANE).name("WINNING ROW").amount(1).build());
			inv.setItem(25, new ItemCreator(Material.LIME_STAINED_GLASS_PANE).name("WINNING ROW").amount(1).build());
			for(int i = 2; i < 7; i++)
			{
				inv.setItem(i, getRandomItem());
			}
		}, 1L);
		for(int x = 0; x < 45; x++)
		{
			this.addFrame(inv -> {
				if(!main.slotting.contains(player)) return;
				if(!player.getOpenInventory().equals(inv)) player.openInventory(inv);
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
		HashMap<Integer, ItemStack> tempStorage = new HashMap<>();
		for(int x = 0; x < 3; x++)
		{
			this.addFrame(inv -> {
				if(!main.slotting.contains(player)) return;
				for(int i = 20; i < 25; i++)
				{
					if(!player.getOpenInventory().equals(inv)) player.openInventory(inv);
					tempStorage.put(i, inv.getItem(i));
					inv.setItem(i, new ItemCreator(Material.LIME_STAINED_GLASS_PANE).name("WINNER").build());
				}
			}, 3L);
			this.addFrame(inv -> {
				if(!main.slotting.contains(player)) return;
				for(int i = 20; i < 25; i++)
				{
					if(!player.getOpenInventory().equals(inv)) player.openInventory(inv);
					inv.setItem(i, tempStorage.get(i));
				}
			}, 3L);
		}
		this.addFrame(inv -> {
			HashMap<ItemStack, Integer> amounts = new HashMap<>();
			if(!main.slotting.contains(player)) return;
			for(int i = 20; i < 25; i++)
			{
				if(inv.getItem(i) != null && inv.getItem(i).getType() != Material.AIR && !inv.getItem(i).getType().toString().contains("STAINED_GLASS_PANE"))
				{
					ItemStack item = inv.getItem(i);
					if(containsSimilarItem(amounts.keySet(), item))
					{
						ItemStack newItem = getSimilarItem(amounts.keySet(), item);
						amounts.put(newItem, amounts.get(newItem)*item.getAmount() + item.getAmount());
					}
					else
						amounts.put(item, item.getAmount());
				}
			}
			String str = ChatColor.translateAlternateColorCodes('&', "&9You won ");
			if(amounts.isEmpty()) str += "NOTHING!";
			for(ItemStack item : amounts.keySet())
			{
				SlotItem si = getItemByInformation(item);
				if(si != null)
				{
					si.doCommands(player, amounts.get(item));
				}
			}
			str += amounts.keySet().stream().map(item -> amounts.get(item) + " " + StringUtils.capitaliseAllWords(item.getType().toString().toLowerCase().replaceAll("_", " "))).collect(Collectors.joining(", "));
			player.sendMessage(str);
			main.slotting.remove(player);
			player.closeInventory();
		}, 30L);
	}

	private SlotItem getItemByInformation(ItemStack item)
	{
		for(SlotItem si : this.items.values())
		{
			if(si.getItem().isSimilar(item))
				return si;
		}
		return null;
	}

	public ItemStack getSimilarItem(Collection<ItemStack> items, ItemStack item)
	{
		for(ItemStack test : items)
		{
			if(test.isSimilar(item)) return test;
		}
		return null;

	}

	public boolean containsSimilarItem(Collection<ItemStack> items, ItemStack item)
	{
		for(ItemStack test : items)
		{
			if(test.isSimilar(item)) return true;
		}
		return false;
	}

	public ItemStack getRandomItem()
	{
		SlotItem item = items.next();
		int amount = amounts.next();
		if(!item.getItem().isSimilar(filler))
			return new ItemCreator(item.getItem()).amount((amount > item.getMaximum()?item.getMaximum():amount < item.getMinimum()?item.getMaximum():amount)).build();
		return item.getItem();
	}

	public String getName() {
		return name;
	}


}
