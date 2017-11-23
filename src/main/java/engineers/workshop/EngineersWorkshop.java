package engineers.workshop;

import engineers.workshop.client.GuiHandler;
import engineers.workshop.common.items.ItemUpgrade;
import engineers.workshop.common.items.Upgrade;
import engineers.workshop.common.Config;
import engineers.workshop.common.network.DataPacket;
import engineers.workshop.common.table.BlockTable;
import engineers.workshop.common.util.Logger;
import engineers.workshop.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import reborncore.RebornCore;
import reborncore.common.network.RegisterPacketEvent;
import reborncore.common.util.RebornCraftingHelper;

import static engineers.workshop.common.util.Reference.Info.MODID;
import static engineers.workshop.common.util.Reference.Info.NAME;
import static engineers.workshop.common.util.Reference.Paths.CLIENT_PROXY;
import static engineers.workshop.common.util.Reference.Paths.COMMON_PROXY;
import static net.minecraft.init.Blocks.*;
import static net.minecraft.init.Blocks.IRON_BARS;
import static net.minecraft.init.Items.*;
import static net.minecraft.init.Items.REDSTONE;

@Mod(modid = MODID, name = NAME, dependencies = "required-after:reborncore", certificateFingerprint = "8727a3141c8ec7f173b87aa78b9b9807867c4e6b")
public class EngineersWorkshop {

	@SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
	public static CommonProxy proxy;

	@Instance(MODID)
	public static EngineersWorkshop instance;

	public static BlockTable blockTable;
	public static Item itemUpgrade;

	public static CreativeTabs tabWorkshop = new CreativeTabs(MODID) {
		@Override
		public ItemStack getTabIconItem() {return new ItemStack(EngineersWorkshop.blockTable);}
	};

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.loadConfig(event.getSuggestedConfigurationFile());
		blockTable = new BlockTable();
		itemUpgrade = new ItemUpgrade();
		MinecraftForge.EVENT_BUS.register(this);
		proxy.preInit(event);
		loadRecipes();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(EngineersWorkshop.instance, new GuiHandler());
	}

	@SubscribeEvent
	public void packetEvent(RegisterPacketEvent event) {
		event.registerPacket(DataPacket.class, Side.SERVER);
		event.registerPacket(DataPacket.class, Side.CLIENT);
	}

	@Mod.EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		RebornCore.proxy.invalidFingerprints.add("Invalid fingerprint detected for Engineers Workshop Reborn!");
	}

	public static void loadRecipes() {
		RebornCraftingHelper.addShapedOreRecipe(new ItemStack(EngineersWorkshop.blockTable), "PPP", "CUC", "CCC", 'P', PLANKS, 'C', COBBLESTONE, 'U', Upgrade.BLANK.getItemStack());

		addRecipe(Upgrade.BLANK, "SP", "PS", 'S', STONE, 'P', PLANKS);
		addRecipe(Upgrade.STORAGE, "C", "U", 'C', Blocks.CHEST, 'U', Upgrade.BLANK.getItemStack());
		addRecipe(Upgrade.AUTO_CRAFTER, "PPP", "CTC", "CUC", 'P', PLANKS, 'C', COBBLESTONE, 'T', PISTON, 'U', Upgrade.BLANK.getItemStack());
		addRecipe(Upgrade.SPEED, "IRI", "LUL", "IRI", 'I', IRON_INGOT, 'R', REDSTONE, 'L', new ItemStack(DYE, 1, 4), 'U', Upgrade.BLANK.getItemStack());
		addRecipe(Upgrade.QUEUE, "PPP", "IUI", "PPP", 'I', IRON_INGOT, 'P', PLANKS, 'U', Upgrade.BLANK.getItemStack());

		addRecipe(Upgrade.AUTO_TRANSFER, "GGG", "HUH", "GGG", 'G', GOLD_INGOT, 'H', HOPPER, 'U', Upgrade.BLANK.getItemStack());
		addRecipe(Upgrade.FILTER, "III", "GBG", "IUI", 'G', Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 'I', IRON_INGOT, 'B', Blocks.IRON_BARS, 'U', Upgrade.BLANK.getItemStack());
		addRecipe(Upgrade.CHARGED, "IRI", "IUI", "IRI", 'I', IRON_INGOT, 'R', REDSTONE, 'U', Upgrade.BLANK.getItemStack());
		addRecipe(Upgrade.TRANSFER, "III", "GRG", "GUG", 'G', GOLD_INGOT, 'I', IRON_INGOT, 'R', REDSTONE_BLOCK, 'U', Upgrade.BLANK.getItemStack());
		addRecipe(Upgrade.AXE, "FAF", "RUR", "III", 'F', FLINT, 'A', IRON_AXE, 'R', REDSTONE, 'U', Upgrade.BLANK.getItemStack(), 'I', IRON_BARS);
	}

	private static void addRecipe(Upgrade upgrade, Object... recipe) {
		if (upgrade.isEnabled()) {
			RebornCraftingHelper.addShapedOreRecipe(upgrade.getItemStack(), recipe);
			Logger.info(upgrade + " recipe loaded.");
		}
	}
}
