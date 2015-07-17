/*
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.model.jobs;

import java.io.Serializable;

import javax.inject.Named;
import javax.persistence.Embeddable;

import org.apache.commons.lang.StringUtils;

@Named
@Embeddable
public class TimerInfo implements Serializable {

	private static final long serialVersionUID = 5572229725635504448L;
	private boolean active = false;
	private String jobName;
	private String parametres;
	private Long userId;
	private Long followingTimerId;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getParametres() {
		return parametres;
	}

	public void setParametres(String parametres) {
		this.parametres = parametres;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Long getFollowingTimerId() {
		return followingTimerId;
	}

	public void setFollowingTimerId(Long followingTimerId) {
		this.followingTimerId = followingTimerId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof TimerInfo) {
			if (this == other) {
				return true;
			}

			TimerInfo info = (TimerInfo) other;
			if (StringUtils.equals(parametres, info.getParametres())
					&& userId == info.getUserId()
					&& followingTimerId == info.getFollowingTimerId()) {
				return true;
			}
		}
		
		return false;
	}
}
