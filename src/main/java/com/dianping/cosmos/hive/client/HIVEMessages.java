package com.dianping.cosmos.hive.client;

public interface HIVEMessages extends com.google.gwt.i18n.client.Messages {

	@DefaultMessage("最近十条查询")
	@Key("homepage.stats")
	String homepage_stats();

	@DefaultMessage("hivequery.html")
	@Key("homepage.HiveQueryURL")
	String homepage_HiveQueryURL();

	@DefaultMessage("执行Hive语句")
	@Key("homepage.HiveQueryLabel")
	String homepage_HiveQueryLabel();

	@DefaultMessage("tableschema.html")
	@Key("homepage.TableSchemaURL")
	String homepage_TableSchemaURL();

	@DefaultMessage("查看数据库表结构")
	@Key("homepage.TableSchemaLabel")
	String homepage_TableSchemaLabel();

	@DefaultMessage("queryhistory.html")
	@Key("homepage.QueryHistoryURL")
	String homepage_QueryHistoryURL();

	@DefaultMessage("查看查询历史")
	@Key("homepage.QueryHistoryLabel")
	String homepage_QueryHistoryLabel();

	@DefaultMessage("获取执行计划信息,执行Hive语句返回查询结果,结果文件可下载到本地")
	@Key("homepage.HiveQueryDesc")
	String homepage_HiveQueryDesc();
	
	@DefaultMessage("查看Hive数据库和表结构详细信息")
	@Key("homepage.TableSchemaDesc")
	String homepage_TableSchemaDesc();
	
	@DefaultMessage("查看用户提交过的查询历史和下载结果文件")
	@Key("homepage.QueryHistoryDesc")
	String homepage_QueryHistoryDesc();

	@DefaultMessage("This web site is for users to execute ad-hoc hive querys and download query result files directly and may change in response to your feedback.")
	@Key("homepage.info")
	String homepage_info();

	@DefaultMessage("<a href='mailto:yukang.chen@dianping.com'> Contact us </a>")
	@Key("homepage.contact")
	String homepage_contact();
}
