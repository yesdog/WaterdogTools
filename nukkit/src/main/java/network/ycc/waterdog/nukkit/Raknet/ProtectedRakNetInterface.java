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

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.nukkit.Server;
import cn.nukkit.network.RakNetInterface;
import cn.nukkit.raknet.protocol.EncapsulatedPacket;

public class ProtectedRakNetInterface extends RakNetInterface {

	private List<String> allowedIPs = null;
	private Set<String> blockedAddresses = new HashSet<String>();

	public ProtectedRakNetInterface(Server server, List<String> allowedIPs) {
		super(server);
		this.allowedIPs = allowedIPs;
	}

	synchronized public void reSetAddresses(List<String> allowedIPs) {
		this.allowedIPs = allowedIPs;
		this.clearBans();
	}

	@Override
	public void handleEncapsulated(String identifier, EncapsulatedPacket packet, int flags) {
		String address = address(identifier);
		try {
			if (allowedIPs.contains(address)) {
				super.handleEncapsulated(identifier, packet, flags);
				return;
			}
		} catch (ConcurrentModificationException e) {
			super.handleEncapsulated(identifier, packet, flags);
			return;
		}
		this.addBan(address, true);

	}

	@Override
	public void openSession(String identifier, String address, int port, long clientID) {
		try {
			if (allowedIPs.contains(address)) {
				super.openSession(identifier, address, port, clientID);
				return;
			}
		} catch (ConcurrentModificationException e) {
			super.openSession(identifier, address, port, clientID);
			return;
		}
		this.addBan(address, false);
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
	
	synchronized private void addBan(String address, boolean secondary) {
		int timeout = secondary ? 300 : 100000000;
		this.blockedAddresses.add(address);
		this.blockAddress(address, timeout);
	}
	
	synchronized private void clearBans() {
		for(String s : this.blockedAddresses) {
			if(this.allowedIPs.contains(s))
				this.unblockAddress(s);
		}
	}

	@Override
	public void shutdown() {
		super.shutdown();
	}

	@Override
	public void emergencyShutdown() {
		super.emergencyShutdown();
	}

}
