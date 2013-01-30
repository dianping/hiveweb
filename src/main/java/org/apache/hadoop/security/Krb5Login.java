package org.apache.hadoop.security;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.security.UserGroupInformation.AuthenticationMethod;

public class Krb5Login {
	private static final Log logger = LogFactory.getLog(Krb5Login.class);

	public static String HIVE_CONNECTION_URL;
	private static final org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();

	static {
		ResourceBundle bundle = ResourceBundle.getBundle("context");
		HIVE_CONNECTION_URL = bundle.getString("hive-web.jdbc.connection.url");
		
		conf.set("hadoop.security.authentication", "kerberos");
		try {
			Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static class Krb5Config extends AppConfigurationEntry {
		private Map<String, String> mOptions;

		private Krb5Config(String loginModuleName,
				LoginModuleControlFlag controlFlag, Map<String, ?> options) {
			super(loginModuleName, controlFlag, options);
		}

		public static final AppConfigurationEntry.LoginModuleControlFlag DEFAULT_CONTROL_FLAG = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;

		private static final String DEFAULT_LOGIN_MODULE_NAME = "com.sun.security.auth.module.Krb5LoginModule";

		public static Krb5Config getInstance() {
			HashMap<String, String> options = new HashMap<String, String>();
			Krb5Config kc = new Krb5Config(DEFAULT_LOGIN_MODULE_NAME,
					DEFAULT_CONTROL_FLAG, options);
			kc.mOptions = options;
			return kc;
		}

		public Krb5Config setDebug(boolean value) {
			mOptions.put("debug", value ? "true" : "false");
			return this;
		}

		public Krb5Config setDoNotPrompt(boolean value) {
			mOptions.put("doNotPrompt", value ? "true" : "false");
			return this;
		}

		public Krb5Config setKeyTab(String filename) {
			mOptions.put("keyTab", filename);
			setUseKeyTab(true);
			return this;
		}

		public Krb5Config setPrincipal(String principal) {
			mOptions.put("principal", principal);
			return this;
		}

		public Krb5Config setStoreKey(boolean value) {
			mOptions.put("storeKey", value ? "true" : "false");
			return this;
		}

		public Krb5Config setTicketCache(String filename) {
			mOptions.put("ticketCache", filename);
			setUseTicketCache(true);
			return this;
		}

		public Krb5Config setUseKeyTab(boolean value) {
			mOptions.put("useKeyTab", value ? "true" : "false");
			return this;
		}

		public Krb5Config setUseTicketCache(boolean value) {
			mOptions.put("useTicketCache", value ? "true" : "false");
			return this;
		}
	}

	static class DynamicConfiguration extends Configuration {
		private String mName;
		private AppConfigurationEntry[] mEntry;

		DynamicConfiguration(String name, AppConfigurationEntry[] entry) {
			mName = name;
			mEntry = entry;
		}

		/**
		 * Retrieve an array of AppConfigurationEntries which corresponds to the
		 * configuration of LoginModules for the given application.
		 */
		public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
			return name.equals(mName) ? mEntry : null;
		}

		public void refresh() {
		}
	}

	public static UserGroupInformation getVerifiedUgi(String username,
			final String password) {
		Subject subject = new Subject();
		LoginContext login = null;
		CallbackHandler handler = new CallbackHandler() {
			public void handle(Callback[] callbacks) throws IOException,
					UnsupportedCallbackException {
				for (Callback callback : callbacks) {
					if (callback instanceof PasswordCallback) {
						PasswordCallback pc = (PasswordCallback) callback;
						pc.setPassword(password.toCharArray());
					}
				}
			}
		};
		try {
			HadoopConf hadoopConf = new HadoopConf();
			hadoopConf.putUserKerberosOptions("principal", username
					+ "@DIANPING.COM");
			hadoopConf.putUserKerberosOptions("useTicketCache", "false");
			hadoopConf.putUserKerberosOptions("storeKey", "false");

			login = new LoginContext(HadoopConf.USER_KERBEROS_CONFIG_NAME,
					subject, handler, hadoopConf);
			login.login();
		} catch (LoginException e) {
			e.printStackTrace();
			return null;
		}
		UserGroupInformation loginUser = new UserGroupInformation(subject);
		// loginUser.setLogin(login);
		subject.getPrincipals(User.class).iterator().next().setLogin(login);
		loginUser.setAuthenticationMethod(AuthenticationMethod.KERBEROS);
		loginUser = new UserGroupInformation(login.getSubject());

		if (logger.isDebugEnabled()) {
			logger.debug("loginUser.getShortUserName():"
					+ loginUser.getShortUserName());
			logger.debug("loginUser.getUserName() " + loginUser.getUserName());
		}
		return loginUser;
	}

	public static Connection getVerifiedConnection(String username,
			final String password) {
		Subject subject = new Subject();
		LoginContext login = null;
		CallbackHandler handler = new CallbackHandler() {
			public void handle(Callback[] callbacks) throws IOException,
					UnsupportedCallbackException {
				for (Callback callback : callbacks) {
					if (callback instanceof PasswordCallback) {
						PasswordCallback pc = (PasswordCallback) callback;
						pc.setPassword(password.toCharArray());
					}
				}
			}
		};
		try {
			HadoopConf hadoopConf = new HadoopConf();
			hadoopConf.putUserKerberosOptions("principal", username
					+ "@DIANPING.COM");
			hadoopConf.putUserKerberosOptions("useTicketCache", "false");
			hadoopConf.putUserKerberosOptions("storeKey", "false");

			login = new LoginContext(HadoopConf.USER_KERBEROS_CONFIG_NAME,
					subject, handler, hadoopConf);
			login.login();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		UserGroupInformation loginUser = new UserGroupInformation(subject);
		// loginUser.setLogin(login);
		subject.getPrincipals(User.class).iterator().next().setLogin(login);
		loginUser.setAuthenticationMethod(AuthenticationMethod.KERBEROS);
		loginUser = new UserGroupInformation(login.getSubject());

		if (logger.isDebugEnabled()) {
			logger.debug("loginUser.getShortUserName():"
					+ loginUser.getShortUserName());
			logger.debug("loginUser.getUserName() " + loginUser.getUserName());
		}
		Connection conn = loginUser.doAs(new PrivilegedAction<Connection>() {
			@Override
			public Connection run() {
				Connection c = null;
				try {
					logger.debug("start get connection");
					c = DriverManager
							.getConnection(HIVE_CONNECTION_URL, "", "");
					logger.debug("through connection");
				} catch (Exception e) {
					logger.error("get connection failed");
					e.printStackTrace();
				}
				return c;
			}
		});
		return conn;
	}

	public static void main(String[] args) {
		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		System.out.println("-------------------------------------------------------");
		System.out.println(conf.get("hadoop.security.authentication"));
		System.out.println(conf.get("hadoop.security.authorization"));
		System.out
				.println("-------------------------------------------------------");
		conf.set("hadoop.security.authentication", "kerberos");
		// UserGroupInformation.setConfiguration(conf);
		try {
			Krb5Login.getVerifiedConnection("searchcron", "11");
			System.out.println("111111111111111");
			// Krb5Login.chech("yukang.chen@DIANPING.COM", "yukang.chen");
			// Krb5Login.check1();
			// Krb5Login.verifyPassword("yukang.chen@DIANPING.COM",
			// "yukang.chen");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
