package network.ycc.waterdog.nukkit.Components;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

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

public class WaterdogToolsConfig {

	private Config conf = null;

	@SuppressWarnings("serial")
	public WaterdogToolsConfig(PluginBase plugin, String name) {
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdirs();
		}
		this.conf = new Config(new File(plugin.getDataFolder(), name), Config.YAML);
		this.conf.setDefault(new LinkedHashMap<String, Object>() {
			{
				put("enable-proxy-firewall", false);
				put("only-use-fallback-firewall", false);
				put("proxy-addresses", Arrays.asList(new String[] { "127.0.0.1", "10.0.0.1", "172.0.0.1" }));
			}
		});
		this.conf.save();

	}

	public boolean isProxyFirewallEnabled() {
		try {
			return this.conf.getBoolean("enable-proxy-firewall");
		} catch (Exception e) {
			Server.getInstance().getLogger().critical(
					"[WaterdogTools - Firewall] Unable to read enable-proxy-firewall from the config. Assuming 'true' to prevent possible damage. Please fix this ASAP!");
			return true;
		}

	}

	public boolean isPrimaryFirewallDisabled() {
		try {
			return this.conf.getBoolean("only-use-fallback-firewall");
		} catch (Exception e) {
			Server.getInstance().getLogger().critical(
					"[WaterdogTools - Firewall] Unable to read only-use-fallback-firewall from the config. Assuming 'false' to prevent possible damage. Please fix this ASAP!");
			return false;
		}

	}

	public List<String> getProxyAddresses() {
		List<String> ret = null;
		try {
			ret = this.conf.getStringList("proxy-addresses");
			if (ret == null) {
				Server.getInstance().getLogger().critical(
						"[WaterdogTools - Config] Unable to read proxy firewall addresses! Blocking all connections until this is resolved!");
				return new ArrayList<String>();
			}
		} catch (Exception e) {
			Server.getInstance().getLogger().critical(
					"[WaterdogTools - Config] An error was encountered trying to read the proxy firewall addresses! Blocking all connections until this is resolved! Error details: "
							+ e.getMessage());
			return new ArrayList<String>();
		}
		return ret;
	}

}

