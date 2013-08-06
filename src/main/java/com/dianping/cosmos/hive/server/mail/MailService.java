package com.dianping.cosmos.hive.server.mail;

import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;

public class MailService {
	private static final Log LOG = LogFactory.getLog(MailService.class);
	
	private String fromAddress;
	private String subject;
	private String recipient;
	private String host;
	private String username;
	private String password;

	public boolean sendMail(String body) {
		final Email email = new Email();
		email.setFromAddress(fromAddress, fromAddress);
		email.setSubject(subject);
		String[] recipients = recipient.split(",");
		for (String r : recipients) {
			email.addRecipient(r, r, RecipientType.TO);
		}
		email.setText(body);
		try {
			new Mailer(host, 25, username, password).sendMail(email);
		} catch (MailException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
