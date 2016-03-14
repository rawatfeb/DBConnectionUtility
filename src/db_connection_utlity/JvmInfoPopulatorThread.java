package db_connection_utlity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import entity.JvmInfo;
import entity.ResourceInfo;

public class JvmInfoPopulatorThread implements Callable<Object> {
	static Logger log = LogManager.getRootLogger();
	private JvmInfo jvmInfo;

	public JvmInfoPopulatorThread(JvmInfo jvmInfo) {
		this.jvmInfo = jvmInfo;
	}

	// will return a new JvmInfo (host, pid, name, JMXPort) for a given jvm
	@Override
	public Object call() throws Exception {
		setResourceConnection();
		return jvmInfo;
	}

	private void setResourceConnection() throws Exception {
		JMXConnector connector = null;
		JMXServiceURL target = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + jvmInfo.getHostName() + ":"
				+ jvmInfo.getJmxPort() + "/jmxrmi");

		try {
			connector = JMXConnectorFactory.connect(target);
		} catch (Exception e) {
			throw new RuntimeException("Exception: could not connect to: " + target);
		}
		try {
			MBeanServerConnection mBeanServer = connector.getMBeanServerConnection();

			/**
			 * this is the part where you MUST know which MBean to get
			 * com.digitalscripter.search.statistics:name=
			 * requestStatistics,type=RequestStatistics YOURS WILL VARY!
			 */
			ObjectName mbean = new ObjectName("com.westgroup.novus.conncop:type=ConnCopStats");
			jvmInfo.setResourceInfoList(getResourcesFromMbeanThreaded(mBeanServer, mbean));
			log.debug("engine="+jvmInfo.getJvmName()+" resCon size="+jvmInfo.getResourceInfoList().size());
		} catch (Exception e) {
			throw new RuntimeException("Exception: " + e.getMessage() + " " + target);
		} finally {
			if (null != connector) {
				connector.close();
			}
		}
	}

	private List<ResourceInfo> getResourcesFromMbeanThreaded(MBeanServerConnection mBeanServer, ObjectName mbean)
			throws Exception {
		List<ResourceInfo> resourceConnectionList = new ArrayList<ResourceInfo>();
		String[] resourceNames = null;
		try {
			resourceNames = (String[]) mBeanServer.getAttribute(mbean, "ResourceNames");
		} catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException | ReflectionException
				| IOException e) {
		}
		ExecutorService executorService = null;
		try {
			executorService = Executors.newFixedThreadPool(5);
			CompletionService openedConnectionsForResource_compService = new ExecutorCompletionService(executorService);
			int resourceCount = 0;
			for (String rn : resourceNames) {
				openedConnectionsForResource_compService.submit(new OpenedConnectionsForResourceThread(mBeanServer,
						mbean, rn));
				resourceCount++;
			}
			executorService.shutdown();
			while (resourceCount > 0) {
				resourceCount--;
				try {
					Future<ResourceInfo> future = openedConnectionsForResource_compService.poll(3, TimeUnit.SECONDS);
					if (null != future) {
						ResourceInfo resourceInfo = (ResourceInfo) future.get();
						if (!resourceInfo.isNull()) {
							resourceInfo.setParentRefInChild(jvmInfo);
							resourceConnectionList.add(resourceInfo);
						}
					}

				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception in getResourcesFromMbeanThreaded", e);
		} finally {
			executorService.shutdownNow();
		}
		return resourceConnectionList;
	}

}
