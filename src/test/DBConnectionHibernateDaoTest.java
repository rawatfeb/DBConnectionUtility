package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import context.DBConnectionUtilityContext;
import dao.DBConnectionHibernateDao;
import entity.JvmInfo;
import entity.ResourceInfo;

public class DBConnectionHibernateDaoTest {
	static DBConnectionHibernateDao dbConnectionHibernateDao = new DBConnectionHibernateDao();

	public static void main(String[] args) throws Exception {

		getByServiceClassTest();
		HibernateUtil.closeSessionFactory();
		System.exit(0);
	}

	public static void getByServiceClassTest() {
		System.out.println(dbConnectionHibernateDao.getByServiceClass("UTILITYCLONEPOOLA", "DLC29"));
	}

	public static void getByResourceTest() {
		System.out.println(dbConnectionHibernateDao.getByResource("cci"));
	}

	public static void getSuspectJvmInfoTest() {
		System.out.println(dbConnectionHibernateDao.getSuspectJvmInfo());

	}

	public static void prepareDailyReportTest() {
		System.out.println(dbConnectionHibernateDao.prepareDailyReport());

		//		Utility.saveToXmlFile(dbConnectionHibernateDao.prepareDailyReport());
		//		System.out.println(Utility.readFromXmlFile());
	}

	public static void getAllresourcesTest() throws Exception {
		System.out.println(dbConnectionHibernateDao.getAllresources());

	}

	public static void insertTest() {
		List<JvmInfo> jvmInfoList = new ArrayList<JvmInfo>();
		JvmInfo jvmInfo1 = new JvmInfo();
		jvmInfo1.setServiceClass("jvm");
		jvmInfo1.setEngineIdentifier("1");
		jvmInfo1.setHostName("host1");
		List<ResourceInfo> resourceInfoList = new ArrayList<ResourceInfo>();
		ResourceInfo resourceInfo1 = new ResourceInfo();
		resourceInfo1.setResource("cci");
		resourceInfo1.setOpenedConnections(15);
		ResourceInfo resourceInfo2 = new ResourceInfo();
		resourceInfo2.setResource("doc");
		resourceInfo2.setOpenedConnections(13);
		resourceInfoList.add(resourceInfo1);
		resourceInfoList.add(resourceInfo2);
		jvmInfo1.setResourceInfoList(resourceInfoList);

		JvmInfo jvmInfo2 = new JvmInfo();
		jvmInfo1.setServiceClass("jvm");
		jvmInfo1.setEngineIdentifier("12");
		jvmInfo2.setHostName("host1");
		List<ResourceInfo> resourceInfoList2 = new ArrayList<ResourceInfo>();
		ResourceInfo resourceInfo3 = new ResourceInfo();
		resourceInfo3.setResource("cci");
		resourceInfo3.setOpenedConnections(4);
		ResourceInfo resourceInfo4 = new ResourceInfo();
		resourceInfo4.setResource("doc");
		resourceInfo4.setOpenedConnections(6);
		resourceInfoList2.add(resourceInfo3);
		resourceInfoList2.add(resourceInfo4);
		jvmInfo2.setResourceInfoList(resourceInfoList2);

		jvmInfoList.add(jvmInfo1);
		jvmInfoList.add(jvmInfo2);

		System.out.println(mergeResourceConnections(jvmInfoList));

		System.out.println(suspectJvmInfo);
	}

	private static List<JvmInfo> suspectJvmInfo = new ArrayList<JvmInfo>();

	public static Map<String, Integer> mergeResourceConnections(List<JvmInfo> jvmInfoList) {
		Map<String, Integer> resConMap = new TreeMap<String, Integer>();
		try {
			boolean isSuspectedJvm = false;
			for (JvmInfo jvmInfo : jvmInfoList) {
				List<ResourceInfo> resInfo = jvmInfo.getResourceInfoList();
				for (ResourceInfo resourceInfo : resInfo) {
					String resourse = resourceInfo.getResource();
					Integer totalcon = resConMap.get(resourse);
					Integer oc = resourceInfo.getOpenedConnections();
					if (oc >= DBConnectionUtilityContext.getServiceAlertableConnection()) {
						isSuspectedJvm = true;
					}
					resConMap.put(resourse, new Integer(((null != totalcon) ? totalcon : 0) + oc));
				}
				//populate suspect map here to mail
				if (isSuspectedJvm) {
					suspectJvmInfo.add(jvmInfo);
					isSuspectedJvm = false;
				}
			}
			return resConMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("merge succeed. size=" + resConMap.size() + " suspectJvmInfo size=" + suspectJvmInfo.size());
		return resConMap;
	}
}
