package utility;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import beans.RequestValueObject;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import context.DBConnectionUtilityContext;
import entity.JvmInfo;
import entity.ResourceInfo;

public class Utility {

	static Logger log = LogManager.getRootLogger();

	public static String resolvedHost(String server) {
		try {
			return InetAddress.getByName(server).getCanonicalHostName();
		} catch (UnknownHostException e) {
		}
		return server + DBConnectionUtilityContext.PROD_DOMAIN;
	}

	public static List<JvmInfo> filterJvmInfoList(List<JvmInfo> jvmInfoList, RequestValueObject requestValueObject) {
		String res = requestValueObject.getResource();
		int minCon = 0;
		Iterator<JvmInfo> jvmItr = jvmInfoList.iterator();
		if (!requestValueObject.isResourceNull() && !requestValueObject.isMinConNull()) {
			try {
				minCon = Integer.parseInt(requestValueObject.getMinCon());
			} catch (Exception e) {
				log.debug(e.getMessage());
			}
			while (jvmItr.hasNext()) {
				JvmInfo jvmInfo = jvmItr.next();
				List<ResourceInfo> resourceInfoList = jvmInfo.getResourceInfoList();
				Iterator<ResourceInfo> resourceInfoItr = resourceInfoList.iterator();
				while (resourceInfoItr.hasNext()) {
					ResourceInfo resourceInfo = resourceInfoItr.next();
					if (!resourceInfo.getResource().startsWith(res) || resourceInfo.getOpenedConnections() < minCon) {
						resourceInfoItr.remove();
					}
				}
				if (jvmInfo.isResourceInfoListEmpty()) {
					jvmItr.remove();
				}
			}
		} else if (!requestValueObject.isResourceNull()) {
			while (jvmItr.hasNext()) {
				JvmInfo jvmInfo = jvmItr.next();
				List<ResourceInfo> resourceInfoList = jvmInfo.getResourceInfoList();
				Iterator<ResourceInfo> resourceInfoItr = resourceInfoList.iterator();
				while (resourceInfoItr.hasNext()) {
					ResourceInfo resourceInfo = resourceInfoItr.next();
					if (!resourceInfo.getResource().startsWith(res)) {
						resourceInfoItr.remove();
					}
				}
				if (jvmInfo.isResourceInfoListEmpty()) {
					jvmItr.remove();
				}
			}
		} else if (!requestValueObject.isMinConNull()) {
			try {
				minCon = Integer.parseInt(requestValueObject.getMinCon());
				while (jvmItr.hasNext()) {
					JvmInfo jvmInfo = jvmItr.next();
					List<ResourceInfo> resourceInfoList = jvmInfo.getResourceInfoList();
					Iterator<ResourceInfo> resourceInfoItr = resourceInfoList.iterator();
					while (resourceInfoItr.hasNext()) {
						ResourceInfo resourceInfo = resourceInfoItr.next();
						if (resourceInfo.getOpenedConnections() < minCon) {
							resourceInfoItr.remove();
						}
					}
					if (jvmInfo.isResourceInfoListEmpty()) {
						jvmItr.remove();
					}
				}
			} catch (Exception e) {
				log.debug(e.getMessage());
			}
		}
		Collections.sort(jvmInfoList);
		return jvmInfoList;
	}

	public static void saveToXmlFile(Map<String, Integer> bag) throws Exception {
		try {
			XStream xStream = new XStream(new StaxDriver());
			xStream.alias("Resource", java.lang.String.class);
			String xml = xStream.toXML(bag);
			FileOutputStream fileOut = null;
			if (!bag.isEmpty()) {
				fileOut = new FileOutputStream("dailyResourceConnectionsReport.xml");
			}
			fileOut.write(xml.getBytes());
			fileOut.close();
			log.debug("saveToXmlFile done. data is saved in dailyResourceConnectionsReport.xml");
		} catch (IOException e) {
			log.error("Exception:", e);
			throw e;
		}
	}

	public static Map<String, Integer> readFromXmlFile() throws IOException {
		Map<String, Integer> bag = new TreeMap<String, Integer>();
		try {
			XStream xStream = new XStream(new StaxDriver());
			xStream.alias("Resource", java.lang.String.class);
			FileReader fileReader = new FileReader("dailyResourceConnectionsReport.xml");
			BufferedReader br = new BufferedReader(fileReader);
			bag = (Map<String, Integer>) xStream.fromXML(br.readLine());
			br.close();
			fileReader.close();
		} catch (IOException e) {
			throw e;
		}
		return bag;
	}

	public static String generateHtmlTableEngineCon(Map<String, Integer> map) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body style=\"text-align: center;\"><a href=\"dailyReport\">Home</a>");
		sb.append("<table style=\"margin: 0px auto;\"><tr><td>");
		sb.append("<table  style=\" background-color: Moccasin; margin: 0 auto;\" border=\"1\" >");

		Set<Entry<String, Integer>> es = map.entrySet();
		Iterator<Entry<String, Integer>> itr = es.iterator();
		int tableSize = map.size() / 2;
		if (tableSize > 1) {
			sb.append("<table  style=\" background-color: Moccasin; margin: 0 auto;\" border=\"1\" >");
			for (int i = 0; i < tableSize; i++) {
				Entry<String, Integer> entry = itr.next();
				sb.append("<tr><td>");
				sb.append(entry.getKey());
				sb.append("</td><td>");
				sb.append(entry.getValue());
				sb.append("</td></tr>");
			}
			sb.append("</table>");
			sb.append("</td><td>");
		}
		sb.append("<table  style=\" background-color: Moccasin; margin: 0 auto;\" border=\"1\" >");
		while (itr.hasNext()) {
			Entry<String, Integer> entry = itr.next();
			sb.append("<tr><td>");
			sb.append(entry.getKey());
			sb.append("</td><td>");
			sb.append(entry.getValue());
			sb.append("</td></tr>");
		}
		sb.append("</table>");
		sb.append("</td></tr></table>");
		sb.append("</body></html>");
		return sb.toString();
	}

	public static String generateHtmlTableServiceCon(Map<String, Integer> map, String resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body style=\"text-align: center;\">");
		sb.append("<table style=\"margin: 0px auto;\"><tr><th colspan=2><a href=\"dailyReport\">Home</a></th><tr><td>");
		int tableSize = map.size() / 2;
		Set<Entry<String, Integer>> es = map.entrySet();
		Iterator<Entry<String, Integer>> itr = es.iterator();
		if (tableSize > 1) {
			sb.append("<table  style=\" background-color: Moccasin; margin: 0 auto;\" border=\"1\" >");
			for (int i = 0; i < tableSize; i++) {
				Entry<String, Integer> entry = itr.next();
				sb.append("<tr><td>");
				sb.append("<a href=\" navigator?command=getbyserviceclass&resource=" + resource + "&serviceclass="
						+ entry.getKey() + "\">");
				sb.append(entry.getKey());
				sb.append("</a>");
				sb.append("</td><td>");
				sb.append(entry.getValue());
				sb.append("</td></tr>");
			}
			sb.append("</table>");
			sb.append("</td><td>");
		}
		sb.append("<table  style=\" background-color: Moccasin; margin: 0 auto;\" border=\"1\" >");
		while (itr.hasNext()) {
			Entry<String, Integer> entry = itr.next();
			sb.append("<tr><td>");
			sb.append("<a href=\" navigator?command=getbyserviceclass&resource=" + resource + "&serviceclass="
					+ entry.getKey() + "\">");
			sb.append(entry.getKey());
			sb.append("</a>");
			sb.append("</td><td>");
			sb.append(entry.getValue());
			sb.append("</td></tr>");
		}
		sb.append("</table>");
		
		sb.append("</td></tr></table>");
		sb.append("</body></html>");
		return sb.toString();
	}

	public static String generateHtmlResCon(Map<String, Integer> gloabalResourceConMap) {
		StringBuilder summaryOuterHtml = new StringBuilder("<div id='dailyReport'><div id='summaryresrow' class='row'>");
		for (Entry<String, Integer> rcEntry : gloabalResourceConMap.entrySet()) {
			String resource = rcEntry.getKey();
			summaryOuterHtml.append(" <a href=\" navigator?command=getbyresource&resource=" + resource
					+ "\" ><div class='summaryColumn' class='row' align='left'>" + resource + "</div></a>");
			summaryOuterHtml.append("<div class='summaryColumn' class='row'  align='left' >"
					+ rcEntry.getValue().toString() + "</div>");
		}
		summaryOuterHtml.append("</div></div>");
		return summaryOuterHtml.toString();
	}

	public static String stack2string(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		} catch (Exception e2) {
			return e.getMessage();
		}
	}

	public static String getDomainExt(String env) {
		String domainExt = ".com";
		switch (env.toUpperCase()) {
		case "PROD":
			domainExt = ".westlan.com";
		case "CLIENT":
			domainExt = ".int.westgroup.com";
		default:
			domainExt = ".com";
		}
		return domainExt;
	}
}