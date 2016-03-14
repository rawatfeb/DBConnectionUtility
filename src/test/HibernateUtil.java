package test;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
	private static SessionFactory sessionFactory = null;

	private HibernateUtil() {
		super();
	}

	private synchronized static void buildSessionFactory() {
		try {
			System.out.println("-----------> Creating session factory <------------");
			System.out.println("Will load cfg file="
					+ HibernateUtil.class.getClassLoader().getResource("hibernate.cfg.xml"));
			Configuration cfg = new Configuration();
			cfg.configure(HibernateUtil.class.getClassLoader().getResource("hibernate.cfg.xml"));
			System.out.println("Hibernate Configuration loaded successfuly");
			StandardServiceRegistryBuilder sb = new StandardServiceRegistryBuilder();
			sb.applySettings(cfg.getProperties());
			StandardServiceRegistry standardServiceRegistry = sb.build();
			sessionFactory = cfg.buildSessionFactory(standardServiceRegistry);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static synchronized Session getSession() throws HibernateException {
		if (null == sessionFactory) {
			buildSessionFactory();
		}
		return sessionFactory.openSession();
	}

	public static void closeSessionFactory() throws HibernateException {
		if (null != sessionFactory)
			sessionFactory.close();
	}
}