/*
* (C) Copyright 2010-2016 Nexmo Inc. All Rights Reserved.
* These materials are unpublished, proprietary, confidential source code of
* Nexmo Inc and constitute a TRADE SECRET of Nexmo Inc.
* Nexmo Inc retains all titles to an intellectual property rights in these materials.
*/
package org.hibernate.test.cache.infinispan.functional.entities;

import java.io.Serializable;
import java.util.Date;

public class ReportPk implements Serializable {
	
	private static final long serialVersionUID = -5772915905866401835L;
	private Date date;
	private String reason;
	
	public ReportPk(){}
	
	public ReportPk(Date date, String reason) {
		super();
		this.date = date;
		this.reason = reason;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportPk other = (ReportPk) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReportPk [date=" + date + ", reason=" + reason + "]";
	}
	
	
	
	
	
}
