package network.ycc.waterdog.nukkit.Raknet;

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

import java.util.List;

import cn.nukkit.Server;
import cn.nukkit.network.RakNetInterface;
import cn.nukkit.raknet.protocol.EncapsulatedPacket;

public class ProtectedRakNetInterface extends RakNetInterface {

	private List<String> allowedIPs = null;

	public ProtectedRakNetInterface(Server server, List<String> allowedIPs) {
		super(server);
		this.allowedIPs = allowedIPs;
	}

	@Override
	public void handleEncapsulated(String identifier, EncapsulatedPacket packet, int flags) {
		String address = address(identifier);
		if (allowedIPs.contains(identifier)) {
			super.handleEncapsulated(identifier, packet, flags);
			return;
		}
		this.blockAddress(address);

	}

	@Override
	public void openSession(String identifier, String address, int port, long clientID) {
		if (allowedIPs.contains(identifier)) {
			super.openSession(identifier, address, port, clientID);
			return;
		}
		this.blockAddress(address, 100000000);
	}

	private String address(String identifier) {
		String[] sp = identifier.split(":");
		if (sp.length == 2) {
			return sp[0];
		} else {
			String ret = sp[0];
			for (int i = 1; i < sp.length - 1; i++) {
				ret += sp[i];
			}
			return ret;
		}
	}

}

