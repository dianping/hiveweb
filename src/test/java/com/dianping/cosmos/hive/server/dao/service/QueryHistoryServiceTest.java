package com.dianping.cosmos.hive.server.dao.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dianping.cosmos.hive.server.store.domain.QueryHistory;
import com.dianping.cosmos.hive.server.store.service.QueryHistoryService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class QueryHistoryServiceTest {
	@Autowired
	private QueryHistoryService queryHistoryService;

	@Test
	public void testInsertQueryHistory() throws Exception {
		QueryHistory qh = new QueryHistory();
		qh.setUsername("yukang.chen");
		qh.setHql("select * from hippolog limit 3");
		qh.setAddtime(new Date());
		qh.setFilename("file_" + UUID.randomUUID());
		queryHistoryService.insertQueryHistory(qh);
	}

	@Test
	public void testSelectQueryHistoryByUsername() throws Exception {
		List<QueryHistory> qhs = queryHistoryService.selectQueryHistoryByUsername("yukang.chen");
		for (QueryHistory q : qhs) {
			System.out.println(q);
		}
	}
	
	@Test
	public void testSelectLastNQuery() throws Exception {
		List<String> hqls = queryHistoryService.selectLastNQuery();
		for (String h : hqls) {
			System.out.println(h);
		}
	}
}
