package entity;

import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "JVM_INFO", uniqueConstraints = { @UniqueConstraint(columnNames = { "HOST_NAME", "JMX_PORT" }) })
public class JvmInfo implements Comparable<JvmInfo>, Comparator<JvmInfo> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "JVM_ID", nullable = false)
	private long jvmId;

	@Column(name = "HOST_NAME")
	private String hostName;
	@Column(name = "SERVICE_CLASS")
	private String serviceClass;
	@Column(name = "ENGINE_IDENTIFIER")
	private String engineIdentifier;
	@Column(name = "JMX_PORT")
	private String jmxPort;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parentRefInChild", fetch = FetchType.EAGER)
	@BatchSize(size = 40)
	private List<ResourceInfo> resourceInfoList;

	public JvmInfo() {

	}

	public JvmInfo(String hostName, String serviceClass, String engineIdentifier, String jmxPort) {
		this.hostName = hostName;
		this.serviceClass = serviceClass;
		this.engineIdentifier = engineIdentifier;
		this.jmxPort = jmxPort;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getJmxPort() {
		return jmxPort;
	}

	public void setJmxPort(String jmxPort) {
		this.jmxPort = jmxPort;
	}

	public List<ResourceInfo> getResourceInfoList() {
		return resourceInfoList;
	}

	public void setResourceInfoList(List<ResourceInfo> resourceInfoList) {
		this.resourceInfoList = resourceInfoList;
	}

	public boolean isResourceInfoListEmpty() {
		return resourceInfoList.isEmpty();
	}

	public long getJvmId() {
		return jvmId;
	}

	public void setJvmId(long jvmId) {
		this.jvmId = jvmId;
	}

	private int getResourceInfoListSize() {
		if (null != resourceInfoList && !resourceInfoList.isEmpty()) {
			return resourceInfoList.size();
		} else {
			return 0;
		}
	}

	public Integer getConnectionsToResource(String resource) {
		if (null != resourceInfoList && !resourceInfoList.isEmpty()) {
			for (ResourceInfo resourceInfo : resourceInfoList) {
				if(resourceInfo.getResource().equalsIgnoreCase(resource)){
					return resourceInfo.getOpenedConnections();
				}
			}
		}
		return 0;
	}

	@Override
	public int compare(JvmInfo JvmInfo1, JvmInfo JvmInfo2) {
		return JvmInfo1.getResourceInfoListSize() - JvmInfo2.getResourceInfoListSize();
	}

	@Override
	public int compareTo(JvmInfo jvmInfo) {
		return jvmInfo.getResourceInfoListSize() - this.getResourceInfoListSize();
	}

	@Override
	public String toString() {
		return "{\"JVM_Id\":" + jvmId + ", \"Host_Name\":\"" + hostName + "\", \"JVM_Name\":\"" + serviceClass+"."
				+ engineIdentifier + "\", \"JMX_Port\":\"" + jmxPort + "\", \"ResourceInfoList\":" + resourceInfoList
				+ "}";
	}

	public String getServiceClass() {
		return serviceClass;
	}

	public void setServiceClass(String serviceClass) {
		this.serviceClass = serviceClass;
	}

	public String getJvmName() {
		return serviceClass +"."+ engineIdentifier;
	}

	public String getEngineIdentifier() {
		return engineIdentifier;
	}

	public void setEngineIdentifier(String engineIdentifier) {
		this.engineIdentifier = engineIdentifier;
	}
}
