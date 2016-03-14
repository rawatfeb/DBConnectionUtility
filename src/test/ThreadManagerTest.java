package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import context.DBConnectionUtilityContext;
import dao.DBConnectionDao;
import dao.DBConnectionHibernateDao;
import db_connection_utlity.JvmInfoPopulatorThread;
import db_connection_utlity.ThreadManager;
import entity.JvmInfo;

public class ThreadManagerTest {
	static ThreadManager tm = new ThreadManager();

	private static DBConnectionDao dBConnectionDao;

	public static void main(String[] args) throws Exception {
		//		tm.getInitialJvmsInfoList();
		//		dumpJvmInfosToFile();
		
		start();
	}

	static private List<JvmInfo> jvmsInfoList;

	public static void start() throws Exception {

		JvmInfo jvminfo = new JvmInfo();
		jvminfo.setHostName("ns0895-12.westlan.com");
		jvminfo.setServiceClass("SHAREDCSLOCCLONE");
		jvminfo.setEngineIdentifier("NS0895-12.PROD.16");
		jvminfo.setJmxPort("21886");
		JvmInfo jvminfo2 = new JvmInfo();
		jvminfo2.setHostName("ns0895-12.westlan.com");
		jvminfo2.setServiceClass("SHAREDCSLOCCLONE");
		jvminfo2.setEngineIdentifier("NS0895-12.PROD.16");
		jvminfo2.setJmxPort("21876");
		List<JvmInfo> initialJvmsInfoList = new ArrayList<JvmInfo>();
		initialJvmsInfoList.add(jvminfo);
		initialJvmsInfoList.add(jvminfo2);

		long start = System.currentTimeMillis();
		ExecutorService executorService = null;
		try {
			System.setProperty("sun.rmi.transport.tcp.responseTimeout", "60000");
			executorService = Executors.newFixedThreadPool(DBConnectionUtilityContext.getThreadPoolSize());
			CompletionService jvminfo_resource_populator_compService = new ExecutorCompletionService(executorService);
			int jvmThreadCount = 0;
			for (JvmInfo jvmInfo : initialJvmsInfoList) {
				jvminfo_resource_populator_compService.submit(new JvmInfoPopulatorThread(jvmInfo));
				jvmThreadCount++;
			}

			executorService.shutdown();
			jvmsInfoList = new ArrayList<JvmInfo>();
			while (jvmThreadCount > 0) {
				jvmThreadCount--;
				try {
					Future future = jvminfo_resource_populator_compService.poll(60, TimeUnit.SECONDS);
					if (null != future) {
						JvmInfo JvmInfo = (JvmInfo) future.get();
						//						JvmInfo.getSuspectResourceList(6);
						jvmsInfoList.add(JvmInfo);
						if ((jvmThreadCount - 1) / 500 == 0) {
							persistToDB();
							jvmsInfoList.clear();
						}
					}
				} catch (Exception e) {
					continue;
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			executorService.shutdownNow();
		}
	}

	private static void persistToDB() {
		DBConnectionHibernateDao.saveJvmInfo(jvmsInfoList);
	}

	public void startTest() {
		try {
			tm.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
