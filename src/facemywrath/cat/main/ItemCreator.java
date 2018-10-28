package facemywrath.cat.main;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemCreator {

    private ItemMeta meta;
    private ItemStack itemStack;

    public ItemCreator(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.meta = itemStack.getItemMeta();
    }

    public static ItemCreator copyOf(ItemStack itemStack) {
        return new ItemCreator(new ItemStack(itemStack));
    }

    public ItemCreator(Material material) {
        this(new ItemStack(material));
    }

    public ItemCreator type(Material material) {
        this.itemStack.setType(material);
        return this;
    }

    public ItemCreator name(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemCreator itemMeta(ItemMeta meta) {
        this.meta = meta;
        return this;
    }

    public ItemCreator durability(short dura) {
        this.itemStack.setDurability(dura);
        return this;
    }

    public ItemCreator data(MaterialData data) {
        this.itemStack.setData(data);
        return this;
    }

    public ItemCreator lore(String... lore) {
        lore(Arrays.asList(lore));
        return this;
    }

    public ItemCreator lore(List<String> lore) {
        this.meta.setLore(lore);
        return this;
    }

    public ItemCreator enchantments(Map<Enchantment, Integer> enchantments, boolean safe) {
        if (safe) itemStack.addEnchantments(enchantments);
        else itemStack.addUnsafeEnchantments(enchantments);
        return this;
    }

    public ItemCreator enchantment(Enchantment enchantment, int level, boolean safe) {
        if (safe) itemStack.addEnchantment(enchantment, level);
        else itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemCreator addItemFlags(ItemFlag... flags) {
        this.meta.addItemFlags(flags);
        return this;
    }

    public ItemCreator amount(int amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(meta);
        return itemStack;
    }


}