package engineers.workshop.client.renderer;

import engineers.workshop.common.table.TileTable;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

/**
 * Created by EwyBoy
 */
//TODO get rid of this, and either replace it with a static render, or use block models.
public class PowerTESR extends TileEntitySpecialRenderer<TileTable> {

	@Override
	public void render(TileTable te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			TextureRenderer.translateAgainstPlayer(te.getPos(), false);
			TextureRenderer.renderTexture(te, te.getPos(),
				0.05d, 0.12d, 0.05d,
				0.00d, 0.00d, 0.00d,
				0.90d, 0.45d, 0.90d);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}

}
