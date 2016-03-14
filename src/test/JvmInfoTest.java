package test;

import java.util.ArrayList;
import java.util.List;

import entity.JvmInfo;
import entity.ResourceInfo;

public class JvmInfoTest {

	public static void main(String[] args) {
		JvmInfo jvmInfo = new JvmInfo();
		List<ResourceInfo> resourceInfoL = new ArrayList<ResourceInfo>();
		jvmInfo.setResourceInfoList(resourceInfoL);
		ResourceInfo res1 = new ResourceInfo();
		res1.setResource("cci");
		res1.setOpenedConnections(5);

		ResourceInfo res2 = new ResourceInfo();
		res2.setResource("cci");
		res2.setOpenedConnections(5);

		ResourceInfo res3 = new ResourceInfo();
		res3.setResource("cci");
		res3.setOpenedConnections(5);

		resourceInfoL.add(res1);
		resourceInfoL.add(res2);
		resourceInfoL.add(res3);

		String expectedString = "{\"JVM_Id\":0, \"Host_Name\":\"null\", \"JVM_Name\":\"null\", \"JMX_Port\":\"null\", \"ResourceInfoList\":[{ \"Resource\": \"cci\", \"Connections\":5 }, { \"Resource\": \"cci\", \"Connections\":5 }, { \"Resource\": \"cci\", \"Connections\":5 }]}";

		if (expectedString.equals(jvmInfo.toString())) {
			System.out.println("JVM info JSON test Passed Successfully");
		} else {
			System.out.println("JVM info JSON test fialed");
		}

		System.out.println(jvmInfo);
	}

}
