package engineers.workshop.client.container.slot.smelting;

import engineers.workshop.client.container.slot.SlotUnit;
import engineers.workshop.client.page.Page;
import engineers.workshop.common.table.TileTable;
import engineers.workshop.common.unit.Unit;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SlotUnitFurnaceResult extends SlotUnit {

	public SlotUnitFurnaceResult(TileTable table, Page page, int id, int x, int y, Unit unit) {
		super(table, page, id, x, y, unit);
	}

	@Override
	public boolean isBig() {
		return true;
	}

	@Override
	public boolean isItemValid(ItemStack itemstack) {
		return false;
	}

	@Override
	public boolean canSupplyItems() {
		return true;
	}

	@Override
	public boolean canAcceptItems() {
		return false;
	}

	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack item) {
		item = super.onTake(player, item);
		FMLCommonHandler.instance().firePlayerSmeltedEvent(player, item);
		item.onCrafting(player.getEntityWorld(), player, item.getCount());
		givePlayerXP(item, player);
		return item;
	}

	//Taken from vanilla
	public void givePlayerXP(ItemStack stack, EntityPlayer player) {
		if (!player.world.isRemote) {
			int stackSize = stack.getCount();
			float experience = FurnaceRecipes.instance().getSmeltingExperience(stack);
			if (experience == 0.0F) {
				stackSize = 0;
			} else if (experience < 1.0F) {
				int xpCount = MathHelper.floor((float) stackSize * experience);
				if (xpCount < MathHelper.ceil((float) stackSize * experience) && Math.random() < (double) ((float) stackSize * experience - (float) xpCount)) {
					++xpCount;
				}
				stackSize = xpCount;
			}
			while (stackSize > 0) {
				int xpSplit = EntityXPOrb.getXPSplit(stackSize);
				stackSize -= xpSplit;
				player.world.spawnEntity(new EntityXPOrb(player.world, player.posX, player.posY + 0.5D, player.posZ + 0.5D, xpSplit));
			}
		}
	}

}
