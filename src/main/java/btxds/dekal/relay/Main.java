package btxds.dekal.relay;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(value = Main.MODID, dist = Dist.CLIENT)
public class Main {

    public static final String MODID = "dekalrelay";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[Dekal Relay] Client authentication relay initialized.");
    }

}