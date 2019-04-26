package network.ycc.waterdog;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import network.ycc.raknet.RakNet;
import network.ycc.waterdog.api.event.UserChannelTapEvent;

public class MetricsPlugin extends Plugin implements Listener {
    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void onUserTap(UserChannelTapEvent event) {
        event.getChannel().config().setOption(RakNet.METRICS, PERakNetMetrics.INSTANCE);
    }
}
