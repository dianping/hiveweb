package com.dianping.cosmos.hive.server.store.persistence;

import java.util.List;

import com.dianping.cosmos.hive.server.store.domain.UserLogin;

public interface UserLoginMapper {
	
	List<UserLogin> selectUserLoginByUsername(String username);
    
    void insertUserLogin(UserLogin userLogin);
}
