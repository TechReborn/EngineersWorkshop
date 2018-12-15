package engineers.workshop.common.items;

import net.minecraft.client.item.TooltipOptions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TextComponent;
import net.minecraft.world.World;

import java.util.List;

public class ItemUpgrade extends Item {

	Upgrade upgrade;

	public ItemUpgrade(Upgrade upgrade) {
		super(upgrade.createSettings());
		this.upgrade = upgrade;

	}

	public static Upgrade getUpgrade(ItemStack item) {
		if(item.getItem() instanceof ItemUpgrade){
			return ((ItemUpgrade) item.getItem()).upgrade;
		}
		return null;
	}

	@Override
	public void buildTooltip(ItemStack item, World world, List<TextComponent> list, TooltipOptions useExtraInfo) {
		super.buildTooltip(item, world, list, useExtraInfo);
		Upgrade upgrade = getUpgrade(item);
		upgrade.addInfo(list);
	}
}
