package hibernate_utils;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import beans.RequestValueObject;

public class HibernateRestrictionsBuilder {

	public static void buildRestrictions(RequestValueObject requestValueObject, Criteria criteria) {
		boolean isAliasCreated = false;

		if (!requestValueObject.isServiceHintNull()) {
			String[] likeServices = requestValueObject.getServiceHint().split(",");
			for (String likeService : likeServices) {
				String[] tokens = likeService.split("\\.", 2);
				if (tokens.length > 1) {
					criteria.add(Restrictions.and(Restrictions.ilike("serviceClass", tokens[0], MatchMode.START),
							Restrictions.ilike("engineIdentifier", tokens[1], MatchMode.START)));
				} else {
					criteria.add(Restrictions.ilike("serviceClass", likeService, MatchMode.START));
				}
			}
		}
		if (!requestValueObject.isEmptyHosts()) {
			criteria.add(Restrictions.and(Restrictions.in("hostName", requestValueObject.getHostsArray())));
		}

		if (!requestValueObject.isResourceNull()) {
			if (!isAliasCreated) {
				criteria.createAlias("resourceInfoList", "r");
				isAliasCreated = true;
			}
			criteria.add(Restrictions.and(Restrictions.ilike("r.resource", requestValueObject.getResource() + "%")));
		}
		if (!requestValueObject.isMinConNull()) {
			if (!isAliasCreated) {
				criteria.createAlias("resourceInfoList", "r");
				isAliasCreated = true;
			}
			criteria.add(Restrictions.and(Restrictions.ge("r.openedConnections", requestValueObject.getMinConInt())));
		}

		// in a single line 
		//criteria.createAlias("resourceInfoList", "resourceInfoList", Criteria.LEFT_JOIN, Restrictions.like("resourceInfoList.resource", "CCI",MatchMode.ANYWHERE) ).setFetchMode("resourceInfoList", FetchMode.JOIN);

	}

}
