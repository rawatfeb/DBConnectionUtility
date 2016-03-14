package entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class JvmResPk implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6075049467781322141L;
	
	@Column(name = "JVM_ID",nullable=false)
	private Integer jvmId;
	@Column(name = "RESOURCE",nullable=false)
	private String resource;

	@Override
	public String toString() {
		return "[jvmId=" + jvmId+ " resource=" + resource + "]";
	}

	@Override
	public boolean equals(Object obj){
		if(this==obj)return true;
		if(!(obj instanceof JvmResPk))return false;
		else{
			JvmResPk t = (JvmResPk)obj;
			return t.getJvmId().equals(this.getJvmId()) && t.getResource().equals(this.getResource());
		}
	}
	
	@Override
	public int hashCode() {
		int i = 31 * ((jvmId.toString() + resource).hashCode()) + 17;
		return i;
	}

	public Integer getJvmId() {
		return jvmId;
	}

	public void setJvmId(Integer jvmId) {
		this.jvmId = jvmId;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

}
