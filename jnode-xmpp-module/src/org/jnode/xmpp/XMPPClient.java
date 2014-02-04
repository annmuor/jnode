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

package org.jnode.xmpp;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import jnode.logger.Logger;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.packet.Message;

public class XMPPClient {
	private static final String XMPP_SERVER = "xmpp.server";
	private static final String XMPP_UID = "xmpp.uid";
	private static final String XMPP_PWD = "xmpp.pwd";
	private static final String MASTERS = "masters";
	private static final String MASTER_PASSWORD = "master.password";
	private static final Logger logger = Logger.getLogger(XMPPClient.class);
	private String server;
	private String uid;
	private String pwd;
	private List<String> masters;
	private String password;
	private Connection connection;
	private HashMap<Chat, ChatAttributes> chatMap;

	public XMPPClient(Properties properties) {
		server = properties.getProperty(XMPP_SERVER);
		uid = properties.getProperty(XMPP_UID);
		pwd = properties.getProperty(XMPP_PWD);
		password = properties.getProperty(MASTER_PASSWORD);
		masters = Arrays.asList(properties.getProperty(MASTERS, "")
				.replaceAll("\\s", "").split(","));
		chatMap = new HashMap<>();
	}

	/**
	 * Проверка соединения
	 * 
	 * @return
	 */
	public boolean testConnection() {
		boolean test = false;
		Connection conn = new XMPPConnection(server);
		try {
			conn.connect();
			conn.login(uid, pwd);
			conn.disconnect();
			test = true;
		} catch (XMPPException e) {
			logger.l2("XMPP connection test failed", e);
		}
		return test;
	}

	/**
	 * Запуск
	 */
	void run() {
		connection = new XMPPConnection(server);
		try {
			connection.connect();
			connection.login(uid, pwd);
			connection.getRoster().setSubscriptionMode(
					SubscriptionMode.accept_all);
			connection.getChatManager().addChatListener(
					new JNodeChatManagerListener());
		} catch (XMPPException e) {
			onError(e);
		}
	}

	private void onError(Exception e) {
		synchronized (this) {
			if (connection != null) {
				connection.disconnect();
			}
			chatMap = new HashMap<>();
			logger.l2("XMPP connection failed", e);
			notify();
		}
	}

	/**
	 * Очищаем старые чаты
	 * 
	 * TODO: реализовать
	 * 
	 * @param delay
	 */
	void cleanUnused(long delay) {
		long now = new Date().getTime();
		for (Chat chat : chatMap.keySet()) {
			if (now - getAttributes(chat).lastMessage > delay) {
				chatMap.remove(chat);
			}
		}
	}

	/**
	 * Атрибуты чата
	 * 
	 * @param chat
	 * @return
	 */
	private ChatAttributes getAttributes(Chat chat) {
		ChatAttributes ret = chatMap.get(chat);
		if (ret == null) {
			ret = new ChatAttributes();
			chatMap.put(chat, ret);
		}
		return ret;
	}

	/**
	 * Обработка входящего чата
	 * 
	 * @author kreon
	 * 
	 */
	private class JNodeChatManagerListener implements ChatManagerListener {

		@Override
		public void chatCreated(Chat chat, boolean createdLocally) {
			if (!createdLocally) {
				String message = "You will not pass!";
				ChatAttributes attrs = new ChatAttributes();
				String username = chat.getParticipant().substring(0,
						chat.getParticipant().indexOf('/'));
				if (masters.contains(username)) {
					message = "Welcome, my master!";
					attrs.authorized = true;
				} else if (password != null) {
					message = "Speak, fiend, and enter";
				} else {
					return;
				}
				try {
					chat.sendMessage(message);
					chat.addMessageListener(new JNodeMessageListener());
					chatMap.put(chat, attrs);
				} catch (XMPPException ignore) {
					chat = null;
				}
			}
		}

	}

	/**
	 * Обработка входящего сообщения
	 * 
	 * @author kreon
	 * 
	 */
	private class JNodeMessageListener implements MessageListener {

		@Override
		public void processMessage(Chat chat, Message message) {
			String reply = "Unknown";
			if (!getAttributes(chat).authorized) {
				if (message.getBody().equals(password)) {
					getAttributes(chat).authorized = true;
					reply = "Authorized";
				} else {
					reply = "You will not pass!";
				}
			} else {
				reply = ControlTools.processCommand(message.getBody());
			}
			try {
				chat.sendMessage(reply);
			} catch (XMPPException e) {
				onError(e);
			}
			{
				long now = new Date().getTime();
				getAttributes(chat).lastMessage = now;
				if(now - getAttributes(chat).lastFloodRate > 60000) {
					getAttributes(chat).floodRate = 0;
					getAttributes(chat).lastFloodRate = now;
				}
				getAttributes(chat).floodRate++;
				if(getAttributes(chat).floodRate > 100) {
					// TODO: ban
				}
			}
		}

	}

	/**
	 * Аттрибуты чата
	 * 
	 * @author kreon
	 * 
	 */
	private class ChatAttributes {
		boolean authorized = false;
		long lastMessage = new Date().getTime();
		long floodRate;
		long lastFloodRate = new Date().getTime();
	}

}
