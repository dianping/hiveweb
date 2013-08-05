package com.dianping.cosmos.hive.server.dao.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dianping.cosmos.hive.server.store.domain.QueryFavorite;
import com.dianping.cosmos.hive.server.store.service.QueryFavoriteService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class QueryFavoriteServiceTest {

	@Autowired
	private QueryFavoriteService service;
	
	@Test
	public void testInsertQueryFavorite() throws Exception {
		QueryFavorite qf = new QueryFavorite();
		qf.setUsername("searchcron");
		qf.setHql("select * from hippolog limit 20000");
		qf.setAddtime(new Date());
		qf.setQueryName("query1");
		service.insertQueryFavorite(qf);
	}
	
	@Test
	public void testSelectQueryFavoriteByUsername() throws Exception {
		List<QueryFavorite> qfs = service.selectQueryFavoriteByUsername("searchcron");
		for (QueryFavorite q : qfs) {
			System.out.println(q);
		}
	}
}
