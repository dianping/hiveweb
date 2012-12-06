package org.apache.hadoop.security;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.apache.hadoop.security.UserGroupInformation.HadoopLoginModule;
import org.apache.hadoop.security.authentication.util.KerberosUtil;

public class HadoopConf extends javax.security.auth.login.Configuration {

	private static String OS_LOGIN_MODULE_NAME;
	public static final String SIMPLE_CONFIG_NAME = "hadoop-simple";
	public static final String USER_KERBEROS_CONFIG_NAME = "hadoop-user-kerberos";
	public static final String KEYTAB_KERBEROS_CONFIG_NAME = "hadoop-keytab-kerberos";

	private static final boolean windows = System.getProperty("os.name")
			.startsWith("Windows");

	static {
		OS_LOGIN_MODULE_NAME = getOSLoginModuleName();
	}

	private static final AppConfigurationEntry OS_SPECIFIC_LOGIN = new AppConfigurationEntry(
			OS_LOGIN_MODULE_NAME, LoginModuleControlFlag.REQUIRED,
			new HashMap<String, String>());

	private static final AppConfigurationEntry HADOOP_LOGIN = new AppConfigurationEntry(
			HadoopLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED,
			new HashMap<String, String>());
	
	private final Map<String, String> USER_KERBEROS_OPTIONS = new HashMap<String, String>();
//	static {
//		USER_KERBEROS_OPTIONS.put("principal", "yukang.chen@DIANPING.COM");
//		USER_KERBEROS_OPTIONS.put("useTicketCache", "false");
//		USER_KERBEROS_OPTIONS.put("storeKey", "false");
//	}
	private final AppConfigurationEntry USER_KERBEROS_LOGIN = new AppConfigurationEntry(
			KerberosUtil.getKrb5LoginModuleName(),
			LoginModuleControlFlag.OPTIONAL, USER_KERBEROS_OPTIONS);
	
	private final AppConfigurationEntry[] USER_KERBEROS_CONF = new AppConfigurationEntry[] {
		OS_SPECIFIC_LOGIN, USER_KERBEROS_LOGIN, HADOOP_LOGIN };
	

	@Override
	public AppConfigurationEntry[] getAppConfigurationEntry(String appName) {
		if (USER_KERBEROS_CONFIG_NAME.equals(appName)) {
			return USER_KERBEROS_CONF;
		}
		return null;
	}
	
	public void putUserKerberosOptions(String key, String value){
		USER_KERBEROS_OPTIONS.put(key, value);
	}

	private static String getOSLoginModuleName() {
		if (System.getProperty("java.vendor").contains("IBM")) {
			return windows ? "com.ibm.security.auth.module.NTLoginModule"
					: "com.ibm.security.auth.module.LinuxLoginModule";
		} else {
			return windows ? "com.sun.security.auth.module.NTLoginModule"
					: "com.sun.security.auth.module.UnixLoginModule";
		}
	}

}
