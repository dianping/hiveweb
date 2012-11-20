package org.apache.hadoop.security;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.apache.hadoop.security.UserGroupInformation.HadoopLoginModule;
import org.apache.hadoop.security.authentication.util.KerberosUtil;

public class HadoopConfiguration extends
		javax.security.auth.login.Configuration {
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
	private static final Map<String, String> USER_KERBEROS_OPTIONS = new HashMap<String, String>();
	static {
//		USER_KERBEROS_OPTIONS.put("doNotPrompt", "true");
//		USER_KERBEROS_OPTIONS.put("useTicketCache", "true");
//		USER_KERBEROS_OPTIONS.put("renewTGT", "true");
//		String ticketCache = System.getenv("KRB5CCNAME");
//		if (ticketCache != null) {
//			USER_KERBEROS_OPTIONS.put("ticketCache", ticketCache);
//		}
		USER_KERBEROS_OPTIONS.put("principal", "yukang.chen@DIANPING.COM");
		USER_KERBEROS_OPTIONS.put("useTicketCache", "false");
		USER_KERBEROS_OPTIONS.put("storeKey", "false");
	}
	private static final AppConfigurationEntry USER_KERBEROS_LOGIN = new AppConfigurationEntry(
			KerberosUtil.getKrb5LoginModuleName(),
			LoginModuleControlFlag.OPTIONAL, USER_KERBEROS_OPTIONS);
	private static final Map<String, String> KEYTAB_KERBEROS_OPTIONS = new HashMap<String, String>();
	static {
		KEYTAB_KERBEROS_OPTIONS.put("doNotPrompt", "true");
		KEYTAB_KERBEROS_OPTIONS.put("useKeyTab", "true");
		KEYTAB_KERBEROS_OPTIONS.put("storeKey", "true");
	}
	private static final AppConfigurationEntry KEYTAB_KERBEROS_LOGIN = new AppConfigurationEntry(
			KerberosUtil.getKrb5LoginModuleName(),
			LoginModuleControlFlag.REQUIRED, KEYTAB_KERBEROS_OPTIONS);

	private static final AppConfigurationEntry[] SIMPLE_CONF = new AppConfigurationEntry[] {
			OS_SPECIFIC_LOGIN, HADOOP_LOGIN };

	private static final AppConfigurationEntry[] USER_KERBEROS_CONF = new AppConfigurationEntry[] {
			OS_SPECIFIC_LOGIN, USER_KERBEROS_LOGIN, HADOOP_LOGIN };

	private static final AppConfigurationEntry[] KEYTAB_KERBEROS_CONF = new AppConfigurationEntry[] {
			KEYTAB_KERBEROS_LOGIN, HADOOP_LOGIN };

	@Override
	public AppConfigurationEntry[] getAppConfigurationEntry(String appName) {
		if (SIMPLE_CONFIG_NAME.equals(appName)) {
			return SIMPLE_CONF;
		} else if (USER_KERBEROS_CONFIG_NAME.equals(appName)) {
			return USER_KERBEROS_CONF;
		} else if (KEYTAB_KERBEROS_CONFIG_NAME.equals(appName)) {
			// KEYTAB_KERBEROS_OPTIONS.put("keyTab", keytabFile);
			// KEYTAB_KERBEROS_OPTIONS.put("principal", keytabPrincipal);
			return KEYTAB_KERBEROS_CONF;
		}
		return null;
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
