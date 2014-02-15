/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jnode.mail.service;

import java.io.IOException;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

public class EMailService {
	private String host;
	private String username;
	private String password;
	private String fromAddr;
	private String port;

	public void sendEMail(String emailTo, String subject, String text)
			throws Exception {
		String[] emailToA = { emailTo };
		sendEMail(emailToA, subject, text, null);
	}

	public void sendEMail(String[] emailTo, String subject, String text,
			String[] attachments) throws Exception {

		Session mailSession = getSession();
		String sendWarnings = new String();
		try {
			Transport transport = mailSession.getTransport();
			MimeMessage message = new MimeMessage(mailSession);
			message.setSubject(subject);
			message.setFrom(new InternetAddress(fromAddr));
			for (int i = 0; i < emailTo.length; i++) {
				message.addRecipient(Message.RecipientType.TO,
						new InternetAddress(emailTo[i]));
			}
			MimeMultipart multiMessage = new MimeMultipart("related");
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(text, "text/plain; charset=utf-8");
			multiMessage.addBodyPart(textPart);
			if (attachments != null) {
				for (int i = 0; i < attachments.length; i++) {
					MimeBodyPart filePart = new MimeBodyPart();
					try {
						filePart.attachFile(attachments[i]);
						multiMessage.addBodyPart(filePart);
					} catch (IOException e) {
						sendWarnings += "Can not attach file " + attachments[i]
								+ "\n";
					}
				}
			}
			message.setContent(multiMessage);
			transport.connect();
			transport.sendMessage(message,
					message.getRecipients(Message.RecipientType.TO));
			if (sendWarnings.length() > 0) {
				throw new Exception(sendWarnings);
			}
		} catch (NoSuchProviderException e) {
			throw new Exception(e);
		} catch (MessagingException e) {
			throw new Exception(e);
		}

	}

	public void sendEMail(String emailTo, String subject, String text,
			String... attachments) throws Exception {
		String[] emailToA = { emailTo };
		sendEMail(emailToA, subject, text, attachments);

	}

	public void sendEMailMulti(String[] emailTo, String subject, String text,
			String... attachments) throws Exception {
		sendEMail(emailTo, subject, text, attachments);

	}

	private Session getSession() {
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", host);
		props.setProperty("mail.port", port);
		if ("465".equals(port)) {
			props.setProperty("mail.smtp.ssl.enable", "true");
		}
		if (username != null && password != null) {
			Authenticator authenticator = new Authenticator();
			props.setProperty("mail.smtp.auth", "true");
			props.setProperty("mail.smtp.sasl.enable", "true");
			props.setProperty("mail.smtp.submitter", authenticator
					.getPasswordAuthentication().getUserName());
			return Session.getInstance(props, authenticator);

		}
		return Session.getInstance(props);
	}

	private class Authenticator extends javax.mail.Authenticator {
		private PasswordAuthentication authentication;

		public Authenticator() {
			authentication = new PasswordAuthentication(username, password);
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
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

	public String getFromAddr() {
		return fromAddr;
	}

	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

}
