package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utility.Utility;
import context.DBConnectionUtilityContext;
import entity.JvmInfo;

public class DBConnectionDao {

	public DBConnectionDao() {
		System.out.println("Object of DBConnectionDao created");
	}

	private static Logger log = LogManager.getRootLogger();
	Connection statusMasterConn = null;
	Connection statusSlaveConn = null;

	static {
		try {
			Class.forName(DBConnectionUtilityContext.ORACLE_DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			log.debug("can not find oracle driver make sure oracle driver jar exist in your classpath");
			throw new RuntimeException("can not find oracle driver make sure oracle driver jar exist in your classpath");
		}
	}

	private void connectStatusMasterDB() throws SQLException {
		if (null == statusMasterConn || statusMasterConn.isClosed()) {
			try {
				statusMasterConn = DriverManager.getConnection(DBConnectionUtilityContext.STATUS_MASTER_JDBC_URL,
						DBConnectionUtilityContext.STATUS_USER, DBConnectionUtilityContext.PASSWORD);
			} catch (SQLException e) {
				e.printStackTrace();

				log.error("Not able get connection plase check JDBC URL "
						+ DBConnectionUtilityContext.STATUS_MASTER_JDBC_URL + " or Credential(Username and Password)"
						+ DBConnectionUtilityContext.STATUS_USER);
				throw new RuntimeException("Not able get connection plase check JDBC URL "
						+ DBConnectionUtilityContext.STATUS_MASTER_JDBC_URL + " or Credential(Username and Password)"
						+ DBConnectionUtilityContext.STATUS_USER);
			}
		}
	}

	private void connectStatusSlaveDB() throws SQLException {
		if (null == statusSlaveConn || statusSlaveConn.isClosed()) {
			try {
				statusSlaveConn = DriverManager.getConnection(DBConnectionUtilityContext.STATUS_SLAVE_JDBC_URL,
						DBConnectionUtilityContext.STATUS_USER, DBConnectionUtilityContext.PASSWORD);
			} catch (SQLException e) {
				e.printStackTrace();

				log.error("Not able get connection plase check JDBC URL "
						+ DBConnectionUtilityContext.STATUS_SLAVE_JDBC_URL + " or Credential(Username and Password)"
						+ DBConnectionUtilityContext.STATUS_USER);
				throw new RuntimeException("Not able get connection plase check JDBC URL "
						+ DBConnectionUtilityContext.STATUS_SLAVE_JDBC_URL + " or Credential(Username and Password)"
						+ DBConnectionUtilityContext.STATUS_USER);
			}
		}
	}

	/*
	 * in status table host name can be simple host name or ip address of the
	 * host i see status master and slave contains different data i think they
	 * do not follow primary to slave complete replication concept we need fully
	 * qualified name of host for further rmi call because of performance impact
	 * doing here only (status routed)
	 */
	public List<JvmInfo> getJmxInfoFromStatus() throws SQLException {
		List<JvmInfo> partialInfoJvmsList = new ArrayList<JvmInfo>();
		connectStatusMasterDB();
		getJmxInfoFromStatus(statusMasterConn, partialInfoJvmsList);
		closeMasterConnections();
		connectStatusSlaveDB();
		getJmxInfoFromStatus(statusSlaveConn, partialInfoJvmsList);
		closeSlaveConnections();
		return partialInfoJvmsList;
	}

	private void getJmxInfoFromStatus(Connection StatusConnnection, List<JvmInfo> partialInfoJvmsList)
			throws SQLException {
		Map<String, String> fqHostNameCache = new HashMap<String, String>();
		try {
			PreparedStatement pstmt = StatusConnnection
					.prepareStatement("select HOST_NAME,CLIENT_NAME,JMX_PORT from STATUS.CLIENT_STATUS order by HOST_NAME");
			pstmt.setFetchSize(40000);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String host = rs.getString("HOST_NAME");
				String fqHostName = fqHostNameCache.get(host);
				if (null == fqHostName) {
					fqHostName=Utility.resolvedHost(host);//expensive call
					fqHostNameCache.clear();
					fqHostNameCache.put(host, fqHostName); 
				}
				String engine = rs.getString("CLIENT_NAME");
				String[] engineTokens = engine.split("\\.",2);
				partialInfoJvmsList.add(new JvmInfo(fqHostName, engineTokens[0], engineTokens[1], rs
						.getString("JMX_PORT")));
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			throw e;
		}
	}

	private void closeSlaveConnections() {
		if (null != statusSlaveConn) {
			try {
				statusSlaveConn.close();
				statusSlaveConn = null;
				log.debug("Connection to statusSlaveConn closed");
			} catch (Exception e) {
				log.debug("Exception while closing status connection ", e);
			}
		}
	}

	private void closeMasterConnections() {
		if (null != statusSlaveConn) {
			try {
				statusSlaveConn.close();
				statusSlaveConn = null;
				log.debug("Connection to statusSlaveConn closed");
			} catch (Exception e) {
				log.debug("Exception while closing status connection ", e);
			}
		}
	}
}