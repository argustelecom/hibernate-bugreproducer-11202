/*
* (C) Copyright 2010-2016 Nexmo Inc. All Rights Reserved.
* These materials are unpublished, proprietary, confidential source code of
* Nexmo Inc and constitute a TRADE SECRET of Nexmo Inc.
* Nexmo Inc retains all titles to an intellectual property rights in these materials.
*/
package org.hibernate.test.cache.infinispan.functional.entities;

import java.util.Date;

public class Report {

	private ReportPk pk;

	private int total;

	public Report() {
	}

	public Report(ReportPk pk, int total) {
		this.pk = pk;
		this.total = total;
	}
	
	public Report(String reason, int total) {
		this.pk = new ReportPk(new Date(), reason);
		this.total = total;
	}

	public ReportPk getPk() {
		return pk;
	}

	public void setPk(ReportPk pk) {
		this.pk = pk;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
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
		Report other = (Report) obj;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Report [pk=" + pk + ", total=" + total + "]";
	}

	
}
