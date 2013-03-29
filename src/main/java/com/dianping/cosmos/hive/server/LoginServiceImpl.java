package com.dianping.cosmos.hive.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.security.Krb5Login;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.cosmos.hive.client.bo.LoginTokenBo;
import com.dianping.cosmos.hive.client.service.LoginService;
import com.dianping.cosmos.hive.server.queryengine.jdbc.HiveJdbcClient;
import com.dianping.cosmos.hive.server.store.domain.UserLogin;
import com.dianping.cosmos.hive.server.store.service.UserLoginService;
import com.dp.cosmos.hadoopKerberosLogin;
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

	private final static long ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000L;

	@Autowired
	private UserLoginService userLoginService;

	private static Cache<String, Date> tokenCache = CacheBuilder.newBuilder()
			.concurrencyLevel(4).maximumSize(100000)
			.expireAfterWrite(24, TimeUnit.HOURS)
			.removalListener(new RemovalListener<String, Date>() {

				@Override
				public void onRemoval(RemovalNotification<String, Date> rn) {
					logger.info(("tokenid:" + rn.getKey() + " addtime:"
							+ rn.getValue() + " was removed from tokenCache"));
				}

			}).build();

	public static Cache<String, Date> getTokenCache() {
		return tokenCache;
	}

	@Override
	public Boolean isAuthenticated(String tokenid) {
		if (tokenCache.getIfPresent(tokenid) != null) {
			Date addtime = tokenCache.getIfPresent(tokenid);
			Date currenttime = new Date();
			if (currenttime.getTime() - addtime.getTime() < ONE_DAY_IN_MILLISECONDS) {
				if (HiveJdbcClient.getUgiCache(tokenid) != null) {
					return true;
				}
			} else {
				logger.debug("tokenid time expired, tokenid:" + tokenid
						+ " addtime:" + addtime);
			}
		}
		return false;
	}

	@Override
	public LoginTokenBo authenticate(String username, String password) {
		LoginTokenBo tokenBo = null;
		try {
			hadoopKerberosLogin.loginFromPassword(username, password, "/tmp/"
					+ username + ".ticketcache");
		} catch (IOException e) {
			logger.error("create ticket cache failed:" + e);
			return null;
		}

		UserGroupInformation ugi = Krb5Login.getVerifiedUgi(username, password);
		if (ugi == null) {
			logger.error(String.format(
					"Get Verified UGI Failed, username:%s ,password:%s",
					username, password));
		} else {
			// Test if this ugi can work through hive server authentication
			Connection conn = HiveJdbcClient.getConnection(ugi);
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error(e);
				}
				String tokenid = UUID.randomUUID().toString();
				Date addtime = new Date();

				tokenBo = new LoginTokenBo();
				tokenBo.setTokenid(tokenid);
				tokenBo.setAddtime(addtime);

				tokenCache.put(tokenid, addtime);
				HiveJdbcClient.putUgiCache(tokenid, ugi);

				if (logger.isDebugEnabled()) {
					Map<String, Date> tokenCacheSnapshot = tokenCache.asMap();
					for (Map.Entry<String, Date> t : tokenCacheSnapshot
							.entrySet()) {
						logger.debug("tokenid:" + t.getKey() + " addtime:"
								+ t.getValue());
					}
				}
				UserLogin userLogin = new UserLogin();
				userLogin.setUsername(username);
				userLogin.setLogintime(addtime);

				userLoginService.insertUserLogin(userLogin);

				logger.info(String
						.format("Connection established successfully , username:%s , password:%s ,tokenid:%s ,tokenCacheSize:%s",
								username, password, tokenid, tokenCache.size()));
			}
		}
		return tokenBo;
	}

	@Override
	public Boolean logout(String tokenid) {
		tokenCache.invalidate(tokenid);
		HiveJdbcClient.removeUgiByTokenid(tokenid);
		return true;
	}
}
