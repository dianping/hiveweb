package com.dianping.cosmos.hive.server;

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
	
	@Autowired
	private UserLoginService userLoginService;

	public static Cache<String, Date> tokenCache = CacheBuilder.newBuilder()
			.concurrencyLevel(4).maximumSize(100000)
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
		if (tokenCache.getIfPresent(tokenid) != null) {
			Date addtime = tokenCache.getIfPresent(tokenid);
			Date currenttime = new Date();
			if (currenttime.getTime() - addtime.getTime() < HALD_DAY_IN_MILLISECONDS) {
				if (HiveJdbcClient.getUgiCache(tokenid) != null) {
					logger.info(tokenid + " Authenticated");
					return true;
				}
			}else {
				logger.info("tokenid time expired, tokenid:" + tokenid + " addtime:" + addtime);
			}
		}
		return false;
	}

	@Override
	public LoginTokenBo authenticate(String username, String password) {
		LoginTokenBo tokenBo = null;

		UserGroupInformation ugi = Krb5Login.getVerifiedUgi(username, password);
		if (ugi == null) {
			logger.error(String
					.format("Get Verified UGI Failed, username:%s ,password:%s",
							username, password));
		} else {
			String tokenid = UUID.randomUUID().toString();
			Date addtime = new Date();

			tokenBo = new LoginTokenBo();
			tokenBo.setTokenid(tokenid);
			tokenBo.setAddtime(addtime);

			tokenCache.put(tokenid, addtime);
			HiveJdbcClient.putUgiCache(tokenid, ugi);
			
			if (logger.isDebugEnabled()){
				Map<String, Date> tokenCacheSnapshot = tokenCache.asMap();
				for (Map.Entry<String, Date> t : tokenCacheSnapshot.entrySet()) {
					logger.info("tokenid:" + t.getKey() + " addtime:"
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
		return tokenBo;
	}

	@Override
	public Boolean logout(String tokenid) {
		LoginServiceImpl.tokenCache.invalidate(tokenid);
		HiveJdbcClient.removeUgiByTokenid(tokenid);
		return true;
	}
}
