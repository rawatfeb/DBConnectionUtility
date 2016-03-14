package beans;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import context.DBConnectionUtilityContext;

public class RequestValueObject {

	private String resource = null;
	private String serviceHint = null;
	private String hosts = null;
	private String minCon = null;
	private String command = null;
	private boolean realTimeFetch = false;

	@Override
	public String toString() {
		return " hosts=" + hosts + " serviceHint=" + serviceHint + " minCon=" + minCon + " realTimeFetch="
				+ realTimeFetch;
	}

	public boolean isEmptyHosts() {
		if (null == hosts || hosts.trim().isEmpty()) {
			return true;
		}
		return false;
	}

	public String getServiceHint() {
		return serviceHint;
	}

	public void setServiceHint(String serviceHint) {
		this.serviceHint = serviceHint;
	}

	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public Set<String> getHostSet() {
		Set<String> hostSet = new HashSet<String>();
		if (null != hosts) {
			String[] hostsArray = hosts.split(",");
			for (String host : hostsArray) {
				if (!host.endsWith(".com")) {
					String h;
					try {
						h = InetAddress.getByName(host).getCanonicalHostName();
						hostSet.add(h);
					} catch (UnknownHostException e) {
						hostSet.add(host + DBConnectionUtilityContext.PROD_DOMAIN);
					}
				} else {
					hostSet.add(host);
				}
			}
		}
		return hostSet;
	}

	public String[] getHostsArray() {
		String[] hostsArray = null;
		if (null != hosts) {
			hostsArray = hosts.split(",");
			for (int i = 0; i < hostsArray.length; i++) {
				if (!hostsArray[i].endsWith(".com")) {
					try {
						hostsArray[i] = InetAddress.getByName(hostsArray[i]).getCanonicalHostName();
					} catch (UnknownHostException e) {
						hostsArray[i] = hostsArray[i] + DBConnectionUtilityContext.PROD_DOMAIN;
					}
				}
			}
		}
		return hostsArray;
	}

	public String getMinCon() {
		return minCon;
	}

	public int getMinConInt() {
		try {
			return Integer.parseInt(minCon);
		} catch (Exception e) {
			return 0;
		}
	}

	public void setMinCon(String minCon) {

		this.minCon = minCon;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		if (null != resource) {
			this.resource = resource.trim().toUpperCase();
		} else {
			this.resource = resource;
		}
	}

	public boolean isServiceHintNull() {
		if (null != serviceHint && !serviceHint.trim().isEmpty()) {
			return false;
		}
		return true;
	}

	public boolean isResourceNull() {
		if (null != resource && !resource.trim().isEmpty()) {
			return false;
		}
		return true;
	}

	public boolean isMinConNull() {
		if (null != minCon && !minCon.trim().isEmpty()) {
			return false;
		}
		return true;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public boolean isRealTimeFetch() {
		return realTimeFetch;
	}

	public void setRealTimeFetch(boolean realTimeFetch) {
		this.realTimeFetch = realTimeFetch;
	}

}
