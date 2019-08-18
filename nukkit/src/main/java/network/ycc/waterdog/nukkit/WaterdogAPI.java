package network.ycc.waterdog.nukkit;

/* 
 * This file is part of the WaterdogTools distribution (https://github.com/yesdog/WaterdogTools).
 * Copyright (c) 2019 Yesdog OSS, a division of YCC Network, LLC
 * 
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.RakNetInterface;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.ScriptCustomEventPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.MainLogger;
import network.ycc.waterdog.nukkit.Components.WaterdogToolsConfig;
import network.ycc.waterdog.nukkit.Raknet.ProtectedRakNetInterface;
import network.ycc.waterdog.nukkit.Raknet.SecondaryProtectedInterface;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class WaterdogAPI extends PluginBase {
	private static MainLogger log = Server.getInstance().getLogger();

	private WaterdogToolsConfig conf = null;

	@Override
	public void onEnable() {
		this.getLogger().info("[WaterdogTools] Starting plugin...");
		this.conf = new WaterdogToolsConfig(this, "config.yml");
		if (this.conf.isProxyFirewallEnabled()) {
			this.getLogger().info("[WaterdogTools - Firewall] Enabling Network-Firewall...");
			if (!this.conf.isPrimaryFirewallDisabled()) {
				for (SourceInterface sourceInterface : this.getServer().getNetwork().getInterfaces()) {
					if (sourceInterface instanceof RakNetInterface) {
						sourceInterface.shutdown();
					}
				}
				this.getLogger().info("[WaterdogTools - Firewall] Shut down all non protected interfaces, OK");
				this.getServer().getNetwork().registerInterface(
						new ProtectedRakNetInterface(this.getServer(), this.conf.getProxyAddresses()));
				this.getLogger().info("[WaterdogTools - Firewall] Registered Protected RakNet Interface!");
			} else {
				this.getLogger()
						.info("[WaterdogTools - Firewall] Only using secondary protected interface as configured.");
			}
			this.getServer().getPluginManager()
					.registerEvents(new SecondaryProtectedInterface(this.conf.getProxyAddresses()), this);
			this.getLogger().info("[WaterdogTools - Firewall] Registered secondary Protected Interface!");
		}
		this.getLogger().info("[WaterdogTools] Done!");
	}

	public static void transferPlayer(Player p, String destination) {
		ScriptCustomEventPacket pk = new ScriptCustomEventPacket();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream a = new DataOutputStream(out);
		try {
			a.writeUTF("Connect");
			a.writeUTF(destination);
			pk.eventName = "bungeecord:main";
			pk.eventData = out.toByteArray();
			p.dataPacket(pk);
		} catch (Exception e) {
			log.warning("Error while transferring ( PLAYER: " + p.getName() + " | DEST: " + destination + " )");
			log.logException(e);
		}
	}

	public static void transferOther(String name, String destination) {
		Player p = getRandomPlayer();
		if (p != null) {
			ScriptCustomEventPacket pk = new ScriptCustomEventPacket();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream a = new DataOutputStream(out);
			try {
				a.writeUTF("ConnectOther");
				a.writeUTF(name);
				a.writeUTF(destination);
				pk.eventName = "bungeecord:main";
				pk.eventData = out.toByteArray();
				p.dataPacket(pk);
			} catch (Exception e) {
				log.warning("Error while transferring ( PLAYER: " + p.getName() + " | DEST: " + destination + " )");
				log.logException(e);
			}
		} else {
			log.info("Cannot execute transfer for player " + name + ": No player online!");
		}
	}

	public static Player getRandomPlayer() {
		if (!Server.getInstance().getOnlinePlayers().isEmpty()) {
			return Server.getInstance().getOnlinePlayers().values().iterator().next();
		} else {
			return null;
		}
	}

}

