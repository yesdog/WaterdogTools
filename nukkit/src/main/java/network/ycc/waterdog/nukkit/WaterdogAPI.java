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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WaterdogAPI extends PluginBase {
	private static MainLogger log = Server.getInstance().getLogger();

	private WaterdogToolsConfig conf = null;

	@Override
	public void onEnable() {
		this.getLogger().info("Starting plugin...");
		this.conf = new WaterdogToolsConfig(this, "config.yml");
		if (this.conf.isProxyFirewallEnabled()) {
			this.getLogger().info("[Firewall] Enabling Network-Firewall...");
			List<String> allowedIPs = this.conf.getProxyAddresses();
			Map<UUID, Player> online = this.getServer().getOnlinePlayers();
			if (online != null && !online.isEmpty()) {
				for (Player p : online.values()) {
					try {
						if (!allowedIPs.contains(p.getAddress())) {
							this.getLogger().info("[Firewall] Kicking non-proxy connected Player " + p.getName());
							p.kick("Please connect over the proxy.");
						}
					} catch (Exception e) {
						continue;
					}
				}
			}
			if (!this.conf.isPrimaryFirewallDisabled()) {
				boolean wasAlreadyregistered = false;
				for (SourceInterface sourceInterface : this.getServer().getNetwork().getInterfaces()) {
					if (sourceInterface instanceof RakNetInterface) {
						if (sourceInterface.getClass().getName().equals(ProtectedRakNetInterface.class.getName())) {
							try {
								sourceInterface.getClass().getMethod("reSetAddresses", List.class)
										.invoke(sourceInterface, allowedIPs);
								this.getLogger().warning(
										"[Firewall] Protected interface was already loaded. If this was due to a reload please consider restarting the server in the future as this might create unexpected behaviour!");
							} catch (Exception e) {
								this.getLogger().error(
										"[Firewall] Was unable to reload proxy addresses from the config. Please restart the server to reload them.");
							}
							wasAlreadyregistered = true;
						} else {
							sourceInterface.shutdown();
							this.getServer().getNetwork().unregisterInterface(sourceInterface);
						}
					}
				}
				this.getLogger().info("[Firewall] Shut down all non protected interfaces, OK");
				if (!wasAlreadyregistered) {
					this.getServer().getNetwork()
							.registerInterface(new ProtectedRakNetInterface(this.getServer(), allowedIPs));
					this.getLogger().info("[Firewall] Registered Protected RakNet Interface!");
				}
			} else {
				this.getLogger().info("[Firewall] Only using secondary protected interface as configured.");
			}
			this.getServer().getPluginManager().registerEvents(new SecondaryProtectedInterface(allowedIPs), this);
			this.getLogger().info("[Firewall] Registered secondary Protected Interface!");
		}
		this.getLogger().info("Done!");
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
