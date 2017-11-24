package engineers.workshop.common.table;

import engineers.workshop.EngineersWorkshop;
import engineers.workshop.client.container.slot.SlotBase;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.GameData;

import java.util.Random;

import static engineers.workshop.common.util.Reference.Info.MODID;

public class BlockTable extends Block implements ITileEntityProvider {

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
		GameRegistry.registerTileEntity(TileTable.class, MODID + ":" + "blockTable");
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

	@SideOnly(Side.CLIENT)
	public void registerModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileTable();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			FMLNetworkHandler.openGui(playerIn, EngineersWorkshop.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
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

			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof TileTable) {
				TileTable table = (TileTable) tileEntity;
				for (SlotBase slot : table.getSlots()) {
					if (slot.shouldDropOnClosing()) {
						ItemStack itemStack = slot.getStack();
						if (!itemStack.isEmpty()) {
							Random random = new Random();

							float dX = random.nextFloat() * 0.8F + 0.1F;
							float dY = random.nextFloat() * 0.8F + 0.1F;
							float dZ = random.nextFloat() * 0.8F + 0.1F;

							EntityItem entityItem = new EntityItem(world, (double) ((float) x + dX),
								(double) ((float) y + dY), (double) ((float) z + dZ), itemStack.copy());
							if (itemStack.hasTagCompound()) {
								entityItem.getItem().setTagCompound(itemStack.getTagCompound().copy());
							}
							float factor = 0.05F;

							entityItem.motionX = random.nextGaussian() * (double) factor;
							entityItem.motionX = random.nextGaussian() * (double) factor + 0.2D;
							entityItem.motionX = random.nextGaussian() * (double) factor;

							world.spawnEntity(entityItem);
							itemStack.setCount(0);
						}
					}
				}
			}
		}
	}

	//Removes the tile from the world
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}
}
