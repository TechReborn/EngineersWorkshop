package engineers.workshop.common.table;

import engineers.workshop.EngineersWorkshop;
import engineers.workshop.client.container.slot.SlotBase;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

public class BlockTable extends BlockWithEntity {

	public static final DirectionProperty FACING = HorizontalFacingBlock.field_11177;

	public BlockTable(Block.Settings settings) {
		super(settings);
		setDefaultState(this.stateFactory.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext itemPlacementContext) {
		return this.getDefaultState().with(FACING, itemPlacementContext.getPlayerFacing());
	}


	@Override
	public RenderTypeBlock getRenderType(BlockState state) {
		return RenderTypeBlock.MODEL;
	}


//	@Override
//	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, PlayerEntity playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
//		if (!worldIn.isRemote) {
//			FMLNetworkHandler.openGui(playerIn, EngineersWorkshop.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
//		}
//		return true;
//	}

//	@Override
//	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest) {
//		if (!world.isRemote) {
//			if (!player.isCreative()) {
//				dropInventory(world, pos);
//			}
//			world.destroyBlock(pos, !player.isCreative());
//		}
//		return false;
//	}

	protected void dropInventory(World world, BlockPos pos) {
		if (!world.isRemote) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();

			BlockEntity blockEntity = world.getBlockEntity(pos);

			if (blockEntity instanceof TileTable) {
				TileTable table = (TileTable) blockEntity;
				for (SlotBase slot : table.getSlots()) {
					if (slot.shouldDropOnClosing()) {
						ItemStack itemStack = slot.getStack();
						if (!itemStack.isEmpty()) {
							Random random = new Random();

							float dX = random.nextFloat() * 0.8F + 0.1F;
							float dY = random.nextFloat() * 0.8F + 0.1F;
							float dZ = random.nextFloat() * 0.8F + 0.1F;

							ItemEntity entityItem = new ItemEntity(world, (double) ((float) x + dX),
								(double) ((float) y + dY), (double) ((float) z + dZ), itemStack.copy());
							if (itemStack.hasTag()) {
								entityItem.getStack().setTag(itemStack.getTag().copy());
							}
							float factor = 0.05F;

							entityItem.velocityX = random.nextGaussian() * (double) factor;
							entityItem.velocityY = random.nextGaussian() * (double) factor + 0.2D;
							entityItem.velocityZ = random.nextGaussian() * (double) factor;

							world.spawnEntity(entityItem);
							itemStack.setAmount(0);
						}
					}
				}
			}
		}
	}

	@Override
	public BlockEntity createBlockEntity(BlockView blockView) {
		return new TileTable();
	}
}
