package test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxConnectionTest{

public static void main(String[] args)throws Exception{
System.setProperty("sun.rmi.transport.tcp.responseTimeout", "60000");
System.setProperty("com.sun.management.jmxremote.authenticate","false");

JMXConnector connector = null;

//JMXServiceURL target = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://ns0895-12.westlan.com:21886/jmxrmi");
JMXServiceURL target = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://ns1085-07.westlan.com:21900/jmxrmi");
try {
	connector = JMXConnectorFactory.connect(target);
	MBeanServerConnection mBeanServer = connector.getMBeanServerConnection();
	ObjectName mbean = new ObjectName("com.westgroup.novus.conncop:type=ConnCopStats");
	String [] resourceNames = (String[]) mBeanServer.getAttribute(mbean, "ResourceNames");
	
	System.out.println("Connected and got the resource names "+resourceNames.length);
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
			if (null != connector) {
				connector.close();
			}
		}
	
}}
