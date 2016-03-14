package dao;

import hibernate_utils.HibernateRestrictionsBuilder;
import hibernate_utils.HibernateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import beans.RequestValueObject;
import context.DBConnectionUtilityContext;
import entity.JvmInfo;
import entity.ResourceInfo;

/**
 * @author U6025719
 *
 */
public class DBConnectionHibernateDao {

	static Logger log = LogManager.getRootLogger();

	public List<JvmInfo> getJvmInfosForHostFromLocalRestrictions(Session session, RequestValueObject requestValueObject) {
		Criteria criteria = session.createCriteria(JvmInfo.class);
		HibernateRestrictionsBuilder.buildRestrictions(requestValueObject, criteria);
		criteria.setFetchMode("resourceInfoList", FetchMode.JOIN);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<JvmInfo> hostJvmInfoList = criteria.list();
		if (hostJvmInfoList.isEmpty()) {
			throw new RuntimeException("Record not found in Local DB for given criteria="+requestValueObject);
		}
		return hostJvmInfoList;
	}

	public List<JvmInfo> getJvmInfosFromLocal(RequestValueObject requestValueObject) throws Exception {
		Session session = null;
		List<JvmInfo> jvmInfoList = null;
		try {
			session = HibernateUtil.getSession();
			jvmInfoList = getJvmInfosForHostFromLocalRestrictions(session, requestValueObject);
			log.debug("Done getJvmInfosFromLocal. Total Jvm fetched= " + jvmInfoList.size());
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			if (null != session)
				session.close();
		}
		return jvmInfoList;
	}

	public Set<String> getAllHosts() throws Exception {
		Session session = null;
		Set<String> hosts = null;
		try {
			session = HibernateUtil.getSession();
			SQLQuery sqlQuery = session.createSQLQuery("select DISTINCT HOST_NAME as host from JVM_INFO");
			sqlQuery.setFetchSize(500);
			List<String> list = sqlQuery.list();
			hosts = new HashSet<String>(list);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			if (null != session)
				session.close();
		}
		return hosts;
	}

	public Set<String> getAllServiceClasses() throws Exception {
		Session session = null;
		Set<String> serviceClass = null;
		try {
			session = HibernateUtil.getSession();
			SQLQuery sqlQuery = session.createSQLQuery("select DISTINCT SERVICE_CLASS as service from JVM_INFO");
			sqlQuery.setFetchSize(200);
			List<String> list = sqlQuery.list();
			serviceClass = new TreeSet<String>(list);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			if (null != session)
				session.close();
		}
		return serviceClass;
	}

	public Set<String> getAllresources() throws Exception {
		Session session = null;
		Set<String> resourceList = null;
		try {
			session = HibernateUtil.getSession();
			SQLQuery sqlQuery = session.createSQLQuery("select DISTINCT RESOURCE as resource from RESOURCE_INFO");
			sqlQuery.setFetchSize(400);
			List<String> list = sqlQuery.list();
			resourceList=new TreeSet<String>(list);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			if (null != session)
				session.close();
		}
		return resourceList;
	}

	public static void saveJvmInfo(List<JvmInfo> JvmInfoList) {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			for (JvmInfo jvmInfo : JvmInfoList) {
				session.save(jvmInfo);
			}
			tx.commit();
			log.debug(JvmInfoList.size() + " jvmsInfos persisted to DB");
		} catch (Exception e) {
			log.debug(e.getMessage());
			try {
				tx.rollback();
			} catch (Exception e1) {
				log.debug("Exception . not able to rollback transaction in saveJvmInfo");
			}
		} finally {
			if (null != session)
				session.close();
		}

	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 *             check if refresh already is running if refresh is not running
	 *             set it to running in properties. save the latest server
	 *             information from CIr and CCI get all the Hosts from local DB
	 *             for each host get the List of JvmInfo and save it to Local
	 *             DB. set the the Last refresh time finally unset the refresh
	 *             running flag
	 */

	public void truncateDataTable() {
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			Transaction tx = session.beginTransaction();
			session.createSQLQuery("alter table RESOURCE_INFO drop constraint FK_1aqx9h19tjimi85jp43exn6aw")
					.executeUpdate();
			session.createSQLQuery("TRUNCATE TABLE RESOURCE_INFO").executeUpdate();
			session.createSQLQuery("TRUNCATE TABLE JVM_INFO").executeUpdate();
			session.createSQLQuery(
					"alter table RESOURCE_INFO add constraint FK_1aqx9h19tjimi85jp43exn6aw  foreign key (FK_JVM_ID) references JVM_INFO")
					.executeUpdate();
			try {
				tx.commit();
			} catch (Exception e) {
				log.error("Exception while committing transaction", e);
				try {
					tx.rollback();
				} catch (Exception e1) {
					log.error("Exception . not able to rollbach transaction ");
				}
			}
			log.debug("RESOURCE_INFO and JVM_INFO tables truncated successfully");
		} catch (Exception e) {
			log.debug("Exception in truncateDataTable() method of DBConnectionHibernateDao", e);
		} finally {
			session.close();
		}
	}

	public Map<String, Integer> prepareDailyReport() {
		Map<String, Integer> resCon = new TreeMap<String, Integer>();
		Session session = null;
		List<?> resourceList = null;
		try {
			session = HibernateUtil.getSession();
			Criteria criteria = session.createCriteria(ResourceInfo.class);
			ProjectionList projectionList = Projections.projectionList();
			projectionList.add(Projections.groupProperty("resource"));
			projectionList.add(Projections.sum("openedConnections"));
			criteria.setProjection(projectionList);
			criteria.setFetchSize(400);
			resourceList = criteria.list();
			for (Object object : resourceList) {
				Object[] ar = (Object[]) object;
				resCon.put((String) ar[0], ((Long) ar[1]).intValue());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			if (null != session)
				session.close();
		}
		return resCon;
	}
	
	public Map<String, Integer> getByResource(String resource) {
		Map<String, Integer> resCon = new LinkedHashMap<String, Integer>();
		Session session = null;
		List<?> resourceList = null;
		try {
			session = HibernateUtil.getSession();
			Criteria criteria = session.createCriteria(JvmInfo.class);
			criteria.createAlias("resourceInfoList", "r");
			criteria.add(Restrictions.ilike("r.resource", resource));
			ProjectionList projectionList = Projections.projectionList();
			projectionList.add(Projections.groupProperty("serviceClass"));
			projectionList.add(Projections.sum("r.openedConnections").as("oc"));
			criteria.setProjection(projectionList);
			criteria.addOrder(Order.desc("oc"));
			criteria.setFetchSize(400);
			resourceList = criteria.list();
			for (Object object : resourceList) {
				Object[] ar = (Object[]) object;
				resCon.put((String) ar[0], ((Long) ar[1]).intValue());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			if (null != session)
				session.close();
		}
		return resCon;
	}
	
	public Map<String, Integer> getByServiceClass(String serviceClass,String resource) {
		Map<String, Integer> engineResCon = new LinkedHashMap<String, Integer>();
		Session session = null;
		List<JvmInfo> jvmInfoList = null;
		try {
			session = HibernateUtil.getSession();
			Criteria criteria = session.createCriteria(JvmInfo.class);
			criteria.createAlias("resourceInfoList", "r");
			criteria.add(Restrictions.and(Restrictions.ilike("serviceClass", serviceClass),Restrictions.ilike("r.resource", resource)));
			criteria.addOrder(Order.desc("r.openedConnections"));
			criteria.setFetchSize(400);
			jvmInfoList = criteria.list();
			for (JvmInfo jvmInfo : jvmInfoList) {
				engineResCon.put(jvmInfo.getJvmName(), jvmInfo.getConnectionsToResource(resource));
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			if (null != session)
				session.close();
		}
		return engineResCon;
	}

	public Map<String, List<ResourceInfo>> getSuspectJvmInfo() { 
		Map<String, List<ResourceInfo>> suspects = new HashMap<String, List<ResourceInfo>>();
		Session session = null;
		try {
			session = HibernateUtil.getSession();
			Criteria criteria = session.createCriteria(ResourceInfo.class);
			criteria.add(Restrictions.ge("openedConnections",
					DBConnectionUtilityContext.getServiceAlertableConnection()));
			criteria.setFetchSize(30);
			List<ResourceInfo> resourceList = criteria.list();

			for (ResourceInfo resourceInfo : resourceList) {
				String jvm = resourceInfo.getParentRefInChild().getJvmName();
				List<ResourceInfo> resList = suspects.get(jvm);
				if (null != resList) {
					resList.add(resourceInfo);
				} else {
					resList = new ArrayList<ResourceInfo>();
					resList.add(resourceInfo);
					suspects.put(jvm, resList);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			if (null != session)
				session.close();
		}
		return suspects;
	}
}
