package facemywrath.cat.main;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	@SuppressWarnings("unchecked")
	public void onEnable()
	{
		List<Player> lotterying = new ArrayList<>();
		Events.listen(this, PlayerCommandPreprocessEvent.class, event -> {
			if(event.getMessage().contains("test"))
			{
				event.setCancelled(true);
				LotteryAnimation animation = new LotteryAnimation(this, event.getPlayer());
				Inventory inv = Bukkit.createInventory(event.getPlayer(), 45, "LOTTERY");
				event.getPlayer().openInventory(inv);
				lotterying.add(event.getPlayer());
				this.getServer().getScheduler().runTaskLater(this, () -> {
					animation.animate(inv);
				}, 1L);
			}
		});
		Events.listen(this, InventoryClickEvent.class, event -> {
			if(lotterying.contains(event.getWhoClicked())) 
			{
				event.setCancelled(true);
				if(event.getSlot() == 0)
				{
					event.getWhoClicked().closeInventory();
					lotterying.remove(event.getWhoClicked());
				}
			}
		});
		Events.listen(this, InventoryDragEvent.class, event -> {
			if(lotterying.contains(event.getWhoClicked())) event.setCancelled(true);
		});
		Events.listen(this, InventoryCloseEvent.class, event -> {
			if(lotterying.contains(event.getPlayer())) lotterying.remove(event.getPlayer());
		});
	}

}
