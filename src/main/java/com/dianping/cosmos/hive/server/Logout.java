package com.dianping.cosmos.hive.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cosmos.hive.server.queryengine.jdbc.HiveJdbcClient;

public class Logout extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory.getLog(Logout.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		if (session != null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					Cookie c = cookies[i];
					if ("tokenid".equals(c.getName())) {
						String tokenid = c.getValue();
						if (logger.isDebugEnabled()) {
							logger.debug("start to remove tokenid from tokenCache and ugi cache. tokenid:"
									+ tokenid);
						}
						
						LoginServiceImpl.tokenCache.invalidate(tokenid);
						HiveJdbcClient.removeUgiByTokenid(tokenid);
					}
					c.setMaxAge(0);
					response.addCookie(c);
				}
			}
		}
		response.sendRedirect("index.html");
	}
}
