package com.dianping.cosmos.hive.client.widget;

import java.util.HashSet;

public class HiveKeyword {
	private static HashSet<String> keywords;

	static {
		keywords = new HashSet<String>();

		//Reserved Keywords
		keywords.add("TRUE");
		keywords.add("FALSE");
		keywords.add("ALL");
		keywords.add("AND");
		keywords.add("OR");
		keywords.add("NOT");
		keywords.add("LIKE");

		keywords.add("ASC");
		keywords.add("DESC");
		keywords.add("ORDER");
		keywords.add("BY");
		keywords.add("GROUP");
		keywords.add("WHERE");
		keywords.add("FROM");
		keywords.add("AS");
		keywords.add("SELECT");
		keywords.add("DISTINCT");
		keywords.add("INSERT");
		keywords.add("OVERWRITE");
		keywords.add("OUTER");
		keywords.add("JOIN");
		keywords.add("LEFT");
		keywords.add("RIGHT");
		keywords.add("FULL");
		keywords.add("ON");
		keywords.add("PARTITION");
		keywords.add("PARTITIONS");
		keywords.add("TABLE");
		keywords.add("TABLES");
		keywords.add("SHOW");
		keywords.add("MSCK");
		keywords.add("DIRECTORY");
		keywords.add("LOCAL");
		keywords.add("TRANSFORM");
		keywords.add("USING");
		keywords.add("CLUSTER");
		keywords.add("DISTRIBUTE");
		keywords.add("SORT");
		keywords.add("UNION");
		keywords.add("LOAD");
		keywords.add("DATA");
		keywords.add("INPATH");
		keywords.add("NULL");
		keywords.add("CREATE");
		keywords.add("EXTERNAL");
		keywords.add("ALTER");
		keywords.add("DESCRIBE");
		keywords.add("DROP");
		keywords.add("REANME");
		keywords.add("COMMENT");
		keywords.add("BOOLEAN");
		keywords.add("TINYINT");
		keywords.add("SMALLINT");
		keywords.add("INT");
		keywords.add("BIGINT");
		keywords.add("FLOAT");
		keywords.add("DOUBLE");
		keywords.add("DATE");
		keywords.add("DATETIME");
		keywords.add("TIMESTAMP");
		keywords.add("STRING");
		keywords.add("BINARY");
		keywords.add("ARRAY");
		keywords.add("MAP");
		keywords.add("REDUCE");
		keywords.add("PARTITIONED");
		keywords.add("CLUSTERED");
		keywords.add("SORTED");
		keywords.add("INTO");
		keywords.add("BUCKETS");
		keywords.add("ROW");
		keywords.add("FORMAT");
		keywords.add("DELIMITED");
		keywords.add("FIELDS");
		keywords.add("TERMINATED");
		keywords.add("COLLECTION");
		keywords.add("ITEMS");
		keywords.add("KEYS");
		keywords.add("LINES");
		keywords.add("STORED");
		keywords.add("SEQUENCEFILE");
		keywords.add("TEXTFILE");
		keywords.add("INPUTFORMAT");
		keywords.add("OUTPUTFORMAT");
		keywords.add("LOCATION");
		keywords.add("TABLESAMPLE");
		keywords.add("BUCKET");
		keywords.add("OUT");
		keywords.add("OF");
		keywords.add("CAST");
		keywords.add("ADD");
		keywords.add("REPLACE");
		keywords.add("COLUMNS");
		keywords.add("RLIKE");
		keywords.add("REGEXP");
		keywords.add("TEMPORARY");
		keywords.add("FUNCTION");
		keywords.add("EXPLAIN");
		keywords.add("EXTENDED");
		keywords.add("SERDE");
		keywords.add("WITH");
		keywords.add("SERDEPROPERTIES");
		keywords.add("LIMIT");
		keywords.add("SET");
		keywords.add("TBLPROPERTIES");
		
		//table name
		keywords.add("HIPPOLOG");
		keywords.add("NGINXLOG");
		keywords.add("HIPPOLOGCURRENT");
		
		//database name
		keywords.add("DEFAULT");
		keywords.add("ARCH");
		keywords.add("ALGO");
		keywords.add("BI");
		keywords.add("BA");
		keywords.add("DPDP");
		keywords.add("SEARCH");
		keywords.add("WWW");
		keywords.add("DATAALGO");
		keywords.add("MOBILE");
		keywords.add("TUANGOU");
		
		//function name
		keywords.add("CONCAT");
		keywords.add("LPAD");
		keywords.add("RPAD");
		keywords.add("TRIM");
		keywords.add("LTRIM");
		keywords.add("RTRIM");
		keywords.add("LIKE");
		keywords.add("RLIKE");
		keywords.add("REGEXP");
		keywords.add("SUM");
		keywords.add("COUNT");
		keywords.add("FROM_UNIXTIME");
		keywords.add("UNIX_TIMESTAMP");
		keywords.add("TO_DATE");
		keywords.add("MAX");
		keywords.add("MIN");
		keywords.add("AVG");
		keywords.add("CASE");
		keywords.add("WHEN");
	}
	
	public static String[] getKeywordsArray() {
		return keywords.toArray(new String[keywords.size()]);
	}
}
