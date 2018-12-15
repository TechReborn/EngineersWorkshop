package engineers.workshop;

import engineers.workshop.common.items.Upgrade;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.model.ModelLoader;

import static engineers.workshop.common.util.Reference.Info.MODID;

public class EngineersWorkshopClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
//		for (int i = 0; i < Upgrade.values().length; ++i) {
//			Upgrade[] upgrades = Upgrade.values().clone();
//			ModelLoader.setCustomModelResourceLocation(EngineersWorkshop.itemUpgrade, i, new ModelResourceLocation(MODID + ":upgrades/" + upgrades[i].getName()));
//		}
	}
}
