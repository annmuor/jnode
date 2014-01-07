package org.jnode.httpd.dao;

import org.jnode.httpd.dto.PointRequest;

import jnode.dao.GenericDAO;

public class PointRequestDAO extends GenericDAO<PointRequest> {
	private static PointRequestDAO self;

	public static PointRequestDAO getSelf() {
		if (self != null) {
			return self;
		}
		synchronized (PointRequestDAO.class) {
			try {
				self = new PointRequestDAO();
				return self;
			} catch (Exception e) {
			}
		}
		return null;
	}

	protected PointRequestDAO() throws Exception {
		super();
	}

	@Override
	protected Class<?> getType() {
		return PointRequest.class;
	}

}
