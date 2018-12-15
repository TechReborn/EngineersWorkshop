package engineers.workshop.common.util.helpers;

import net.minecraft.client.MinecraftClient;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by EwyBoy
 **/
public class FormattingHelper {

	public static String formatNumber(int number) {
		return NumberFormat.getIntegerInstance(Locale.forLanguageTag(String.valueOf(MinecraftClient.getInstance().getLanguageManager().getLanguage()))).format(number);
	}
}
