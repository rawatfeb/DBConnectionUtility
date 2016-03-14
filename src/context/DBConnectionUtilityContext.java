package context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBConnectionUtilityContext {
	static Logger log = LogManager.getRootLogger();
	static Properties properties = new Properties();
	static File file = null;
	public static final String CIR_USER = "cirr";
	public static final String PASSWORD = "n0vu5r";
	public static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";
	public static final String CCI_USER = "ccir";
	public static final String DOMAIN_QUERY_LOCAL = "select distinct(DOMAIN) from SERVER_DOMAIN";
	public static final String POOL_QUERY_LOCAL = "select distinct(POOL) from SERVER_POOL";
	public static final String DOMAIN_QUERY_CCI = "select distinct DOMAIN_NAME from CCI.domain_data where domain_type='LOAD'";
	public static final String SERVER_DOMAIN_QUERY_LOCAL = "select distinct(SERVER) from SERVER_DOMAIN";
	public static final String SERVER_POOL_QUERY_LOCAL = "select distinct(SERVER) from SERVER_POOL";
	public static final String SERVER_POOL_QUERY_CIR = "SELECT S.SRVR_NAME,SERVICE.POOL_NAME FROM DEPLOY.SERVER S, DEPLOY.SRVR_SERVICE SERVICE where S.SRVR_STATUS = 'A' AND S.SRVR_NAME=SERVICE.SRVR_NAME AND lower(SERVICE.POOL_NAME) LIKE '%prod%'";
	public static final String SERVER_DOMAIN_QUERY_CIR = "SELECT S.SRVR_NAME,SN.DOMAIN_NAME FROM DEPLOY.SERVER S,(SELECT SDP.SRVR_NAME,D.DOMAIN_NAME FROM DEPLOY.DOMAIN D, DEPLOY.SRVR_DOMAIN_POOL SDP WHERE D.DOMAIN_ID = SDP.DOMAIN_ID) SN WHERE S.SRVR_NAME = SN.SRVR_NAME AND S.SRVR_STATUS= 'A' AND upper(SN.DOMAIN_NAME) IN(?)";
	public static final String CACHE_PROPERTY_FILE = "DBConnectionUtiltyCache.properties";
	public static final String PROD_DOMAIN = ".westlan.com";
	public static final String DOMAIN_MODE = "domainMode";
	public static final String POOL_MODE = "poolMode";
	public static final String STATUS_USER = "novusbbr";
	public static final String JMX_PORT_QUERY_STATUS = "select CLIENT_NAME, JMX_PORT from STATUS.CLIENT_STATUS where HOST_NAME LIKE ? OR host_name = ?";
	public static final String CCI_JDBC_URL = "jdbc:oracle:thin:@cottonfield.westlan.com/nvp14b.westlan.com";
	public static final String CIR_JDBC_URL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(LOAD_BALANCE=NO)(ADDRESS=(PROTOCOL=TCP)(HOST=dansfield.int.thomsonreuters.com)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=nvp32a.int.thomsonreuters.com)))";
	public static final String STATUS_MASTER_JDBC_URL = "jdbc:oracle:thin:@//bacliff.westlan.com:1521/nvp07a.westlan.com";
	public static final String STATUS_SLAVE_JDBC_URL = "jdbc:oracle:thin:@//branford.westlan.com:1521/nvp07z.westlan.com";

	static {
		readProperties();
	}

	public static void readProperties() {
		file = new File(DBConnectionUtilityContext.class.getClassLoader().getResource(CACHE_PROPERTY_FILE).getPath());
		InputStream inStream;
		try {
			inStream = new FileInputStream(file);
			properties.load(inStream);
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			log.debug("Exception while reading properties file", e);
		}
	}

	enum availableConfigurableProperties {
		IS_REFRESH_RUNNING, TURN_ON_SEND_TO_ELASTIC_SEARCH, THREAD_POOL_SIZE, SERVICE_ALERT_ON_CONNECTION, EMAIL_TO, HOUR_AT_REFRESH
	}

	public static void setRefreshRunning() {
		setRefreshRunningValue(true);
	}

	public static void unsetRefreshRunning() {
		setRefreshRunningValue(false);
	}

	private static void setRefreshRunningValue(Boolean flag) {
		properties.setProperty("IS_REFRESH_RUNNING", flag.toString());

	}

	public static boolean isRefreshRunning() {
		String isRefreshRunningProperty = properties.getProperty("IS_REFRESH_RUNNING", "false");
		try {
			if (Boolean.parseBoolean(isRefreshRunningProperty)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void setLastRefreshTime() {
		properties.setProperty("LAST_REFRESH_TIME", new Date().toString());
	}

	public static void flushProperties() {
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			properties.store(out, null);
			out.close();
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
	}

	public static void setCache(String propertyName, Set<String> cache) {
		properties.setProperty(propertyName, cache.toString());
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			properties.store(out, null);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String propertyName, String defaultValue) {
		return properties.getProperty(propertyName, defaultValue);
	}

	public static boolean isSendToElastciSearchTurnedOn() {
		return Boolean.valueOf(getProperty("TURN_ON_SEND_TO_ELASTIC_SEARCH", "true"));
	}

	public static int getThreadPoolSize() {
		return Integer.valueOf(getProperty("THREAD_POOL_SIZE", "4"));
	}

	public static int getServiceAlertableConnection() {
		return Integer.valueOf(getProperty("SERVICE_ALERT_ON_CONNECTION", "6"));
	}

	public static String getMailTO() {
		return getProperty("EMAIL_TO", "gaurav.rawat@thomsonreuters.com");
	}

	public static int getHourAtRefresh() {
		return Integer.valueOf(getProperty("HOUR_AT_REFRESH", "4"));
	}

	public static String getLastRefreshTime() {
		return getProperty("LAST_REFRESH_TIME", "NA");
	}

	public static String getAllServicesCache(String hint) {
		return filterByHint(getProperty("ALL_SERVICES", "[]"), hint);
	}

	public static String getAllResourceCache(String hint) {
		return filterByHint(getProperty("ALL_RESOURCES", "[]"), hint);
	}

	public static String getAllHostCache(String hint) {
		return filterByHint(getProperty("ALL_HOSTS", "[]"), hint);
	}

	public static void main(String[] args) {
		System.out.println(getAllServicesCache(null));
	}

	private static String filterByHint(String value, String hint) {
		if (null == hint) {
			hint = "";
		}
		hint=hint.toLowerCase();
		StringBuilder sb = new StringBuilder("[");
		String[] val = value.split(", ");
	
		for (int i = 0; i < val.length; i++) {
			if (val[i].toLowerCase().startsWith(hint)) {
				sb.append("\"" + val[i] + "\",");
			}
		}
		sb.setLength(sb.length() - 1);
		sb.append("]");
		return sb.toString();
	}

	public static void setAllServicesCache(Set<String> value) {
		properties.setProperty("ALL_SERVICES", value.toString().replace("[", "").replace("]", ""));
	}

	public static void setAllResourceCache(Set<String> value) {
		properties.setProperty("ALL_RESOURCES", value.toString().replace("[", "").replace("]", ""));
	}

	public static void setAllHostCache(Set<String> value) {
		properties.setProperty("ALL_HOSTS", value.toString().replace("[", "").replace("]", ""));
	}

	public static Properties getConfigurableProperties() {
		Properties configurableProperties = (Properties) properties.clone();
		validateConfiguration(configurableProperties);
		return configurableProperties;
	}

	public static Properties updateConfiguration(Properties configuration) {
		validateConfiguration(configuration);
		properties.putAll(configuration);
		flushProperties();
		return getConfigurableProperties();
	}

	private static void validateConfiguration(Properties configuration) {

		Enumeration<Object> keysEnumeration = configuration.keys();
		while (keysEnumeration.hasMoreElements()) {
			Object key = keysEnumeration.nextElement();
			try {
				availableConfigurableProperties k = availableConfigurableProperties.valueOf((String) key);
				switch (k.toString()) {
				case "SERVICE_ALERT_ON_CONNECTION":
					try {
						Integer.parseInt((String) configuration.get("SERVICE_ALERT_ON_CONNECTION"));
					} catch (Exception e) {
						configuration.remove(key);
					}
					break;
				case "THREAD_POOL_SIZE":
					try {
						Integer.parseInt((String) configuration.get("THREAD_POOL_SIZE"));
					} catch (Exception e) {
						configuration.remove(key);
					}
					break;
				case "TURN_ON_SEND_TO_ELASTIC_SEARCH":
					String value = (String) configuration.get("TURN_ON_SEND_TO_ELASTIC_SEARCH");
					if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
						//go ahead
					} else {
						configuration.remove(key);
					}
					break;
				case "HOUR_AT_REFRESH":
					try {
						Integer.parseInt((String) configuration.get("HOUR_AT_REFRESH"));
					} catch (Exception e) {
						configuration.remove(key);
					}
					break;
				case "IS_REFRESH_RUNNING":
					value = (String) configuration.get("IS_REFRESH_RUNNING");
					if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
						//go ahead
					} else {
						configuration.remove(key);
					}
					break;
				}

			} catch (IllegalArgumentException ex) {
				configuration.remove(key);
				// do not update properties which are not allowed	(not in availableConfigurableProperties	) 
			}
		}
	}
}
