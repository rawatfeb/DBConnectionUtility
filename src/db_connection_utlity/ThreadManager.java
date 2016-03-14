package db_connection_utlity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utility.ElasticSearchClient;
import context.DBConnectionUtilityContext;
import dao.DBConnectionDao;
import dao.DBConnectionHibernateDao;
import entity.JvmInfo;

public class ThreadManager {
	Logger log = LogManager.getRootLogger();
	private List<JvmInfo> jvmsInfoList;

	public ThreadManager() {
	}

	public void start() throws Exception {
		try {
			if (DBConnectionUtilityContext.isSendToElastciSearchTurnedOn()) {
				ElasticSearchClient.setClient();
			}
		} catch (Exception e) {
			log.debug("ElasticSearchClient Exception ", e);
		}
		List<JvmInfo> initialJvmsInfoList = getInitialJvmsInfoList();

		ExecutorService executorService = null;
		try {
			System.setProperty("sun.rmi.transport.tcp.responseTimeout", "60000");
			System.setProperty("com.sun.management.jmxremote.authenticate", "false");
			executorService = Executors.newFixedThreadPool(DBConnectionUtilityContext.getThreadPoolSize());
			CompletionService jvminfo_resource_populator_compService = new ExecutorCompletionService(executorService);
			int jvmThreadCount = 0;
			for (JvmInfo jvmInfo : initialJvmsInfoList) {
				jvminfo_resource_populator_compService.submit(new JvmInfoPopulatorThread(jvmInfo));
				jvmThreadCount++;
			}
			log.debug(jvmThreadCount + " Threads been submitted to jvminfo_resource_populator_compService");
			executorService.shutdown();
			jvmsInfoList = new ArrayList<JvmInfo>();
			while (jvmThreadCount > 0) {
				jvmThreadCount--;
				try {
					Future<JvmInfo> future = jvminfo_resource_populator_compService.poll(60, TimeUnit.SECONDS);
					if (null != future) {
						JvmInfo JvmInfo = (JvmInfo) future.get();
						jvmsInfoList.add(JvmInfo);
					}
					if ((jvmThreadCount % 200) == 0) {
						batchPersist();
						jvmsInfoList.clear();
					}
				} catch (Exception e) {
					log.warn("Exception while getting jvmsInfoList from the jvminfo_resource_populator_compService..",
							e);
					continue;
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			executorService.shutdownNow();
		}
		releaseResources();
	}

	private void batchPersist() {
		persistToDB();
		if (DBConnectionUtilityContext.isSendToElastciSearchTurnedOn()) {
			sendToElasticSearch();
		}

	}

	private void sendToElasticSearch() {
		try {
			ElasticSearchClient.sendToElasticSearch(jvmsInfoList);
		} catch (Exception e) {
			log.debug(
					"Exception in sendToElasticSearch() method of HostThread jvmsInfoList size=" + jvmsInfoList.size(),
					e);
		}

	}

	public List<JvmInfo> getInitialJvmsInfoList() {
		List<JvmInfo> initialJvmsInfoList = null;
		DBConnectionDao dBConnectionDao = null;
		try {
			dBConnectionDao = new DBConnectionDao();
			initialJvmsInfoList = dBConnectionDao.getJmxInfoFromStatus();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Not able to get initial jvm info(host, name, jmx port) from the status DB", e);
		} 
		return initialJvmsInfoList;
	}

	private void persistToDB() {
		DBConnectionHibernateDao.saveJvmInfo(jvmsInfoList);
	}

	private void releaseResources() {
		ElasticSearchClient.closeClient();
	}
}
