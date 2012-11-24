package com.dianping.cosmos.hive.server;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.security.Krb5Login;
import org.springframework.stereotype.Service;

import com.dianping.cosmos.hive.client.bo.LoginTokenBo;
import com.dianping.cosmos.hive.client.service.LoginService;
import com.dianping.cosmos.hive.server.queryengine.jdbc.HiveJdbcClient;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
@Service("Login")
public class LoginServiceImpl extends RemoteServiceServlet implements
		LoginService {
	private static final Log logger = LogFactory
			.getLog(HiveQueryServiceImpl.class);

	private final static long HALD_DAY_IN_MILLISECONDS = 12 * 60 * 60 * 1000L;

	private static Cache<String, Date> tokenCache = CacheBuilder.newBuilder()
			.concurrencyLevel(4).maximumSize(10000)
			.expireAfterWrite(12, TimeUnit.HOURS)
			.removalListener(new RemovalListener<String, Date>() {

				@Override
				public void onRemoval(RemovalNotification<String, Date> rn) {
					logger.info(("tokenid:" + rn.getKey() + " addtime:"
							+ rn.getValue() + " was removed from tokenCache"));
				}

			}).build();

	@Override
	public Boolean isAuthenticated(String tokenid) {
		logger.info("tokenid:" + tokenid);
		logger.info("tokenCache:" + tokenCache.size());

		Map<String, Date> tt = tokenCache.asMap();
		for (Map.Entry<String, Date> t : tt.entrySet()) {
			logger.info("----------------------------" + t.getKey() + ":"
					+ t.getValue());
		}

		if (tokenCache.getIfPresent(tokenid) != null) {
			Date addtime = tokenCache.getIfPresent(tokenid);
			Date currenttime = new Date();
			if (currenttime.getTime() - addtime.getTime() < HALD_DAY_IN_MILLISECONDS) {
				if (HiveJdbcClient.getTokenConn(tokenid) != null) {
					
					logger.info("Authenticated");
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public LoginTokenBo authenticate(String username, String password) {
		LoginTokenBo tokenBo = null;

		Connection conn = Krb5Login.getVerifiedConnection(username, password);
		if (conn == null) {
			logger.error(String
					.format("Get Verified Hive Connection Failed, username:%s ,password:%s",
							username, password));
		} else {
			String tokenid = UUID.randomUUID().toString();
			Date addtime = new Date();

			tokenBo = new LoginTokenBo();
			tokenBo.setTokenid(tokenid);
			tokenBo.setAddtime(addtime);

			tokenCache.put(tokenid, addtime);
			HiveJdbcClient.putTokenConn(tokenid, conn);
			
			Map<String, Date> tt = tokenCache.asMap();
			for (Map.Entry<String, Date> t : tt.entrySet()) {
				logger.info("#######################" + t.getKey() + ":"
						+ t.getValue());
			}

			logger.info(String
					.format("Connection established successfully , username:%s ,password:%s ,tokenid:%s ,tokenCacheSize:%s",
							username, password, tokenid, tokenCache.size()));
		}
		return tokenBo;
	}

	@Override
	public Boolean logout(String tokenid) {
		tokenCache.invalidate(tokenid);
		HiveJdbcClient.removeConnectionByTokenid(tokenid);
		return true;
	}
}
