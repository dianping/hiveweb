package com.dianping.cosmos.hive.server.store.persistence;

import java.util.List;

import com.dianping.cosmos.hive.server.store.domain.QueryFavorite;

public interface QueryFavoriteMapper {
	List<QueryFavorite> selectQueryFavoriteByUsername(String username);

	void insertQueryFavorite(QueryFavorite qf);
}
