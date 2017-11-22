package engineers.workshop;

import engineers.workshop.common.loaders.BlockLoader;
import engineers.workshop.common.loaders.ItemLoader;
import engineers.workshop.common.network.DataPacket;
import engineers.workshop.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import reborncore.common.network.RegisterPacketEvent;

import static engineers.workshop.common.util.Reference.Info.MODID;
import static engineers.workshop.common.util.Reference.Info.NAME;
import static engineers.workshop.common.util.Reference.Paths.CLIENT_PROXY;
import static engineers.workshop.common.util.Reference.Paths.COMMON_PROXY;

@Mod(modid = MODID, name = NAME, dependencies = "required-after:reborncore", certificateFingerprint = "8727a3141c8ec7f173b87aa78b9b9807867c4e6b")
public class EngineersWorkshop {

	@SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
	public static CommonProxy proxy;

	@Instance(MODID)
	public static EngineersWorkshop instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ItemLoader.registerItems();
		BlockLoader.registerBlocks();
		MinecraftForge.EVENT_BUS.register(this);
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	@SubscribeEvent
	public void packetEvent(RegisterPacketEvent event) {
		event.registerPacket(DataPacket.class, Side.SERVER);
		event.registerPacket(DataPacket.class, Side.CLIENT);
	}
}
