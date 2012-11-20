package com.dianping.cosmos.hive.shared.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

public class UserLogonUtils {
	private static Logger logger = Logger.getLogger(UserLogonUtils.class);
	
	private static final String URL = "ldap://192.168.50.11:389/";
	private static final String BASEDN = "OU=Technolog Department,OU=shoffice,DC=dianpingoa,DC=com";
	private static final String FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

	public static Boolean logon(String username, String password) {
		LdapContext ctx = null;
		Control[] connCtls = null;
		
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, FACTORY);
		env.put(Context.PROVIDER_URL, URL + "DC=dianpingoa,DC=com");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, "cn=" + username + ","
				+ BASEDN);
		env.put(Context.SECURITY_CREDENTIALS, password);
		try {
			ctx = new InitialLdapContext(env, connCtls);
		} catch (javax.naming.AuthenticationException e) {
			logger.info("Authentication faild: " + e.toString());
		} catch (Exception e) {
			logger.info("Something wrong while authenticating: "
					+ e.toString());
		}
		
		if (ctx == null){
			return false;
		}
		return true;
	}

}
