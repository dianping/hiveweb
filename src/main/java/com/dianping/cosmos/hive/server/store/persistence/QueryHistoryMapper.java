package com.dianping.cosmos.hive.server.store.persistence;

import java.util.List;

import com.dianping.cosmos.hive.server.store.domain.QueryHistory;

public interface QueryHistoryMapper {
	
    List<QueryHistory> selectQueryHistoryByUsername(String username);
    
    void insertQueryHistory(QueryHistory qh);
    
    List<String> selectLastNQuery();

}
