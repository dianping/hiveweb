package com.dianping.cosmos.hive.server.store.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dianping.cosmos.hive.server.store.domain.QueryHistory;
import com.dianping.cosmos.hive.server.store.persistence.QueryHistoryMapper;

@Service
public class QueryHistoryService {
	
	@Autowired
	private QueryHistoryMapper queryHistoryMapper;
	
	@Transactional
	public List<QueryHistory> selectQueryHistoryByUsername(String username){
		List<QueryHistory> queryHistoryList = queryHistoryMapper.selectQueryHistoryByUsername(username);
		for (QueryHistory queryHistory : queryHistoryList) {
			System.out.println(queryHistory);
		}
		return queryHistoryList;
	};
    
	@Transactional
    public void insertQueryHistory(QueryHistory qh){
		queryHistoryMapper.insertQueryHistory(qh);
	};
	
	@Transactional
	public List<String> selectLastNQuery(){
		List<String> hqls = queryHistoryMapper.selectLastNQuery();
		return hqls;
	}
}
