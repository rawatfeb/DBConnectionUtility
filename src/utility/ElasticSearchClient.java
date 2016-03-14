package utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import entity.JvmInfo;

public class ElasticSearchClient {
	static Logger log = LogManager.getRootLogger();
	static Client client;
	static String index = "dbconnectionutility"; //dbconnectionutility  //megacorp

	public static void main(String[] args) throws Exception, ExecutionException {
		// on startup

		/*
		 * String json =
		 * "{ \"Date\":\"2015-08-236T10:03:44.044Z\", \"JVM_Id\":0, \"Host_Name\":\"null\", \"JVM_Name\":\"null\", \"JMX_Port\":\"null\", \"ResourceInfoList\":[{ \"Resource\": \"cci\", \"Connections\":5 }, { \"Resource\": \"cci\", \"Connections\":5 }, { \"Resource\": \"cci\", \"Connections\":5 }]}"
		 * ; for (int i = 0; i < 100; i++) { IndexResponse response =
		 * client.prepareIndex(index, "hostName", "" +
		 * i).setSource(json).execute() .actionGet();
		 * System.out.println(response.isCreated()); }
		 * 
		 * System.out.println("done"); // on shutdown
		 */

		/*
		 * Client client = new TransportClient().addTransportAddress(new
		 * InetSocketTransportAddress( "c081nzt.int.thomsonreuters.com", 9300));
		 * ActionFuture<GetIndexResponse> future =
		 * client.admin().indices().getIndex(new GetIndexRequest());
		 * GetIndexResponse response = future.get(); String[] res =
		 * response.getIndices();
		 * 
		 * for (String string : res) { System.out.println(res.toString()); }
		 */

		//		client.close();
	}

	private static boolean sendToElasticSearch(String indexWithDate, String type, String json) {
		IndexResponse response = client.prepareIndex(indexWithDate, type).setSource(json).execute().actionGet();
		return response.isCreated();
	}

	public static void setClient(String host, int port, String cluster) {
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", cluster).build(); //rwt_elasic_cluster
		client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(host, port));
	}

	public synchronized static void setClient() {
		if (null == client) {
			Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build(); //rwt_elasic_cluster
			client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(
					"swat-a.westlan.com", 9300)); //u6025719-tpl-a.ten.thomsonreuters.com
		}
	}

	public static void closeClient() {
		if (null != client) {
			client.close();
			client = null;
			log.debug("Elastic Search Client is closed");
		}

	}

	public static Client getClient() {
		return client;
	}

	public static void setClient(Client client) {
		ElasticSearchClient.client = client;
	}

	public String getIndex() {
		return index;
	}

	public static void setIndex(String index) {
		ElasticSearchClient.index = index;
	}

	public static void sendToElasticSearch(List<JvmInfo> jvmsInfoList) {
		if (null != client) {
			String indexWithDate = index + "-" + new SimpleDateFormat("YYYY.MM.dd").format(new Date());
			for (JvmInfo jvmInfo : jvmsInfoList) {
				sendToElasticSearch(indexWithDate, jvmInfo.getServiceClass(), jvmInfo.toString());
			}
		}
	}
}
