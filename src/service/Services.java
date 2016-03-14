package service;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utility.MailUtility;
import utility.Utility;
import beans.RequestValueObject;
import context.DBConnectionUtilityContext;
import dao.DBConnectionHibernateDao;
import db_connection_utlity.ThreadManager;
import entity.JvmInfo;
import entity.ResourceInfo;

public class Services {
	static Logger log = LogManager.getRootLogger();
	DBConnectionHibernateDao dBConnectionHibernateDao = new DBConnectionHibernateDao();

	public boolean refreshLocalDB() throws Exception {
		log.debug("Started refreshLocalDB....");
		long startTime = System.currentTimeMillis();
		try {
			if (!DBConnectionUtilityContext.isRefreshRunning()) {
				DBConnectionUtilityContext.setRefreshRunning();
				dBConnectionHibernateDao.truncateDataTable();
				new ThreadManager().start();
				log.debug("Refreshed Local DB Successfully Total Elapsed Time="
						+ (System.currentTimeMillis() - startTime) / (1000 * 60) + " minutes. ");
				dialyReportPersister();
			} else {
				log.debug("Do not mind looks like Refresh is already running or check properties file for more control");
			}
		} catch (Exception e) {
			log.error(" ", e);
			throw e;
		} finally {
			DBConnectionUtilityContext.unsetRefreshRunning();
			DBConnectionUtilityContext.setLastRefreshTime();
			DBConnectionUtilityContext.flushProperties();
		}
		return true;
	}

	public List<JvmInfo> getJvmInfosFromLocal(RequestValueObject requestValueObject) throws Exception {
		List<JvmInfo> jvmInfoList = dBConnectionHibernateDao.getJvmInfosFromLocal(requestValueObject);
		jvmInfoList = Utility.filterJvmInfoList(jvmInfoList, requestValueObject);
		if (jvmInfoList.isEmpty()) {
			throw new RuntimeException("No Engines matched the query. Please try minimal query criteria.");
		}
		return jvmInfoList;
	}

	public void dialyReportPersister() {
		try {
			DBConnectionUtilityContext.setAllResourceCache(dBConnectionHibernateDao.getAllresources());
			DBConnectionUtilityContext.setAllHostCache(dBConnectionHibernateDao.getAllHosts());
			DBConnectionUtilityContext.setAllServicesCache(dBConnectionHibernateDao.getAllServiceClasses());
			Map<String, Integer> resCon = dBConnectionHibernateDao.prepareDailyReport();
			Utility.saveToXmlFile(resCon);
			Map<String, List<ResourceInfo>> suspectsJvmsList = dBConnectionHibernateDao.getSuspectJvmInfo();
			if (!suspectsJvmsList.isEmpty()) {
				MailUtility.mail(suspectsJvmsList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
