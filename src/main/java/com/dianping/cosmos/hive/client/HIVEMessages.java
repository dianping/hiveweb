package com.dianping.cosmos.hive.client;

public interface HIVEMessages extends com.google.gwt.i18n.client.Messages {

	@DefaultMessage("Latest Ten Hive Query")
	@Key("homepage.stats")
	String homepage_stats();

	@DefaultMessage("hivequery.html")
	@Key("homepage.HiveQueryURL")
	String homepage_HiveQueryURL();

	@DefaultMessage("Execute Hive Query")
	@Key("homepage.HiveQueryLabel")
	String homepage_HiveQueryLabel();

	@DefaultMessage("tableschema.html")
	@Key("homepage.TableSchemaURL")
	String homepage_TableSchemaURL();

	@DefaultMessage("View Table Schema")
	@Key("homepage.TableSchemaLabel")
	String homepage_TableSchemaLabel();

	@DefaultMessage("queryhistory.html")
	@Key("homepage.QueryHistoryURL")
	String homepage_QueryHistoryURL();

	@DefaultMessage("View Query History")
	@Key("homepage.QueryHistoryLabel")
	String homepage_QueryHistoryLabel();

	@DefaultMessage("Write your hive query language and retrieve the output, also get the query plan info of the specified query")
	@Key("homepage.HiveQueryDesc")
	String homepage_HiveQueryDesc();
	
	@DefaultMessage("Check the database and table schema infomation")
	@Key("homepage.TableSchemaDesc")
	String homepage_TableSchemaDesc();

	@DefaultMessage("This hive web site is for users to execute on-demand hive query and download query result file directly and may change in response to your feedback.")
	@Key("homepage.info")
	String homepage_info();

	@DefaultMessage("<a href='mailto:yukang.chen@dianping.com'> Contact us </a>")
	@Key("homepage.contact")
	String homepage_contact();
}
