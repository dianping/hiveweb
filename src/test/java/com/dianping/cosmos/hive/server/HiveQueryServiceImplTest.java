package com.dianping.cosmos.hive.server;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dianping.cosmos.hive.server.queryengine.IQueryEngine;
import com.dianping.cosmos.hive.server.store.service.QueryHistoryService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class HiveQueryServiceImplTest {

	@Autowired
	private QueryHistoryService queryHistoryService;
	
	@Autowired
	@Qualifier("HiveCmdLineQueryEngine")
	private IQueryEngine queryEngine;

	@Test
	public void testGetDatabases() {
		assertNotNull(queryHistoryService);
		assertNotNull(queryEngine);
		
		Cache<String, Date> graphs = CacheBuilder.newBuilder().concurrencyLevel(4).weakKeys().maximumSize(10000)
		.expireAfterWrite(3, TimeUnit.SECONDS).build();
		
		graphs.put("1", new Date());
		System.out.println(graphs.size());
		System.out.println(graphs.getIfPresent("1"));
		try {
			Thread.sleep(4000L);
			System.out.println(graphs.size());
			System.out.println(graphs.getIfPresent("1"));
			System.out.println(graphs.size());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
