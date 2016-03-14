package db_connection_utlity;

import java.util.concurrent.Callable;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import entity.ResourceInfo;

public class OpenedConnectionsForResourceThread implements Callable<Object> {
	MBeanServerConnection mBeanServer = null;
	ObjectName mbean = null;
	String resource = null;

	public OpenedConnectionsForResourceThread(MBeanServerConnection mBeanServer, ObjectName mbean, String resource) {
		this.mBeanServer = mBeanServer;
		this.mbean = mbean;
		this.resource = resource;
	}

	@Override
	public Object call() throws Exception {
		ResourceInfo resourceInfo = new ResourceInfo(resource);
		try {
			resourceInfo.setOpenedConnections((Integer) mBeanServer.invoke(mbean, "findNumOpenConnections",
					new Object[] { resource }, new String[] { String.class.getName() }));
		} catch (Exception e) {
			//intentional
		}
		return resourceInfo;
	}

}
