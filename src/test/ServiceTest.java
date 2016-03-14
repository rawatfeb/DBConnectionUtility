package test;

import beans.RequestValueObject;
import service.Services;

public class ServiceTest {
	static Services service = new Services();

	public static void main(String[] args) throws Exception {
		
		dialyReportPersisterTest();
		
	}

	public static void dialyReportPersisterTest(){
		
		service.dialyReportPersister();
		
	}
	
	
	public static void getJvmInfosFromLocalTest(){
		try {
			RequestValueObject requestValueObject = new RequestValueObject();
			requestValueObject.setHosts("ns0895-12.westlan.com");
			requestValueObject.setServiceHint("shared");
			requestValueObject.setMinCon("2");
			requestValueObject.setResource("dlc");

			System.out.println(service.getJvmInfosFromLocal(requestValueObject));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
