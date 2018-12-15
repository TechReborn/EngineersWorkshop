package engineers.workshop.common.table;

import engineers.workshop.EngineersWorkshop;
import engineers.workshop.client.container.slot.SlotBase;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

import static engineers.workshop.common.util.Reference.Info.MODID;

public class BlockTable extends BlockWithEntity implements IBlockEntityProvider {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockTable() {
		super(Material.ROCK);
		setHardness(3.5f);
		setCreativeTab(EngineersWorkshop.tabWorkshop);
		setRegistryName(MODID + ":" + "blockTable");
		setUnlocalizedName(MODID + ":" + "blockTable");
		GameData.register_impl(this);
		ItemBlock itemBlock = new ItemBlock(this);
		itemBlock.setRegistryName(getRegistryName());
		GameData.register_impl(itemBlock);
		GameRegistry.registerBlockEntity(TileTable.class, MODID + ":" + "blockTable");
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta >= EnumFacing.HORIZONTALS.length) {
			meta = 0;
		}
		return getDefaultState().withProperty(FACING, EnumFacing.HORIZONTALS[meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).ordinal();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public void registerModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}

	@Override
	public BlockEntity createNewBlockEntity(World worldIn, int meta) {
		return new TileTable();
	}

	@Override
	public boolean hasBlockEntity(IBlockState state) {
		return true;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, PlayerEntity playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			FMLNetworkHandler.openGui(playerIn, EngineersWorkshop.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest) {
		if (!world.isRemote) {
			if (!player.isCreative()) {
				dropInventory(world, pos);
			}
			world.destroyBlock(pos, !player.isCreative());
		}
		return false;
	}

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
