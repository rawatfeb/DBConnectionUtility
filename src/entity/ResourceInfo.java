package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "RESOURCE_INFO", uniqueConstraints = { @UniqueConstraint(columnNames = { "FK_JVM_ID", "RESOURCE" }) })
public class ResourceInfo {

	/*
	 * @EmbeddedId private JvmResPk jvmResPk;
	 */

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk")
	private long resourceInfoPk;

	@Column(name = "RESOURCE")
	private String resource;

	@Column(name = "OPENED_CONNECTIONS")
	private Integer openedConnections;
	
	@ManyToOne
	@JoinColumn(name = "FK_JVM_ID", referencedColumnName = "JVM_ID", nullable = false)
	private JvmInfo parentRefInChild;

	public ResourceInfo() {

	}

	public ResourceInfo(String resource) {
		this.resource = resource;
	}

	public String toString() {
		return "{ \"Resource\": \"" + resource + "\", \"Connections\":" + openedConnections+" }";
	}

	public Integer getOpenedConnections() {
		return openedConnections;
	}

	public void setOpenedConnections(Integer openedConnections) {
		this.openedConnections = openedConnections;
	}
	
	public synchronized boolean isNull() {

		return (null == openedConnections || Integer.valueOf(0).equals(openedConnections));

		/* return (null==jvmResPk); */
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public JvmInfo getParentRefInChild() {
		return parentRefInChild;
	}

	public void setParentRefInChild(JvmInfo parentRefInChild) {
		this.parentRefInChild = parentRefInChild;
	}

	public long getResourceInfoPk() {
		return resourceInfoPk;
	}

	public void setResourceInfoPk(long resourceInfoPk) {
		this.resourceInfoPk = resourceInfoPk;
	}
}
