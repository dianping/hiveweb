package com.dianping.cosmos.hive.server.store.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dianping.cosmos.hive.server.store.domain.UserLogin;
import com.dianping.cosmos.hive.server.store.persistence.UserLoginMapper;

@Service
public class UserLoginService {
	private static final Log logger = LogFactory.getLog(UserLoginService.class);

	@Autowired
	private UserLoginMapper userLoginMapper;

	@Transactional
	public List<UserLogin> selectUserLoginByUsername(String username) {
		List<UserLogin> userLoginList = userLoginMapper
				.selectUserLoginByUsername(username);

		if (logger.isDebugEnabled()) {
			logger.debug("username:" + username
					+ " total query history list size is:"
					+ userLoginList.size());

			for (UserLogin u : userLoginList) {
				logger.debug(u);
			}
		}
		return userLoginList;
	};

	@Transactional
	public void insertUserLogin(UserLogin userLogin) {
		if (logger.isDebugEnabled()) {
			logger.debug(userLogin);
		}
		userLoginMapper.insertUserLogin(userLogin);
	};
}
