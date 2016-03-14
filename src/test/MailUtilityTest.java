package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import entity.JvmInfo;
import entity.ResourceInfo;

public class MailUtilityTest {
	public static void main(String[] args) {
		String mailBody = "<html><head><title>DB Connection Utility Tool Alert Report </title></head><body><table border=\"1\"><tr><th>Engine</th><th colspan=\"100%\">Resource and Opened connections </th></tr> table-content </table> </body></html>";
		StringBuilder mailBodyAlertRow = new StringBuilder();
		List<JvmInfo> suspectJvmInfo = new ArrayList<JvmInfo>();
		JvmInfo vmInfo = new JvmInfo();
		vmInfo.setHostName("hello");
		vmInfo.setServiceClass("jvm name");
		vmInfo.setEngineIdentifier("26");

		List<ResourceInfo> resourceInfoList = new ArrayList<ResourceInfo>();
		ResourceInfo resourceInfo1 = new ResourceInfo();
		resourceInfo1.setResource("cci");
		resourceInfo1.setOpenedConnections(20);
		ResourceInfo resourceInfo2 = new ResourceInfo();
		resourceInfo2.setResource("doc");
		resourceInfo2.setOpenedConnections(12);
		ResourceInfo resourceInfo3 = new ResourceInfo();
		resourceInfo3.setResource("doc");
		resourceInfo3.setOpenedConnections(12);
		ResourceInfo resourceInfo4 = new ResourceInfo();
		resourceInfo4.setResource("doc");
		resourceInfo4.setOpenedConnections(12);
		resourceInfoList.add(resourceInfo1);
		resourceInfoList.add(resourceInfo2);
		resourceInfoList.add(resourceInfo3);
		resourceInfoList.add(resourceInfo4);
		

		Map<String, List<ResourceInfo>> suspectJvms = new HashMap<String, List<ResourceInfo>>();
		suspectJvms.put("testVm", resourceInfoList);

		mailBodyAlertRow.append("<tr>");
		for (Entry<String, List<ResourceInfo>> jvm : suspectJvms.entrySet()) {
			mailBodyAlertRow.append("<tr>");
			mailBodyAlertRow.append("<td>");
			mailBodyAlertRow.append(jvm.getKey());
			mailBodyAlertRow.append("</td>");
			List<ResourceInfo> suspectResourceList = jvm.getValue();
			for (ResourceInfo resourceInfo : suspectResourceList) {
				mailBodyAlertRow.append("<td>");
				mailBodyAlertRow.append(resourceInfo.getResource() + " " + resourceInfo.getOpenedConnections());
				mailBodyAlertRow.append("</td>");
			}
			mailBodyAlertRow.append("</tr>");
		}
		mailBodyAlertRow.append("</tr>");
		mailBody = mailBody.replace("table-content", mailBodyAlertRow.toString());
		System.out.println(mailBody);
	}
}
