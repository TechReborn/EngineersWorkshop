package engineers.workshop.common.items;

import engineers.workshop.EngineersWorkshop;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

import static engineers.workshop.common.util.Reference.Info.MODID;

public class ItemUpgrade extends Item {

	public ItemUpgrade() {
		setCreativeTab(EngineersWorkshop.tabWorkshop);
		setHasSubtypes(true);
		setRegistryName(MODID + ":" + "upgrade");
		GameData.register_impl(this);
	}

	public static Upgrade getUpgrade(int dmg) {
		return dmg >= 0 && dmg < Upgrade.values().length ? Upgrade.values()[dmg] : null;
	}

	public static Upgrade getUpgrade(ItemStack item) {
		return !item.isEmpty() && EngineersWorkshop.itemUpgrade.equals(item.getItem()) ? getUpgrade(item.getItemDamage()) : null;
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {
		Upgrade upgrade = getUpgrade(item);
		return MODID + ":" + "upgrade" + "." + (upgrade != null ? upgrade.getName() : "unknown");
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> lst) {
		if (!isInCreativeTab(tab)) {
			return;
		}
		for (int i = 0; i < Upgrade.values().length; ++i) {
			Upgrade[] upgrades = Upgrade.values().clone();
			lst.add(upgrades[i].getItemStack());
		}
	}

	@Override
	public void addInformation(ItemStack item, World world, List<String> list, ITooltipFlag useExtraInfo) {
		Upgrade upgrade = getUpgrade(item);
		if (upgrade != null) {
			upgrade.addInfo(list);
		} else {
			list.add(TextFormatting.RED + "This is not a valid item");
		}
	}
}
