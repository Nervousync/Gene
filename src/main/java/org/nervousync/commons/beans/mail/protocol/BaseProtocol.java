/*
 * Licensed to the Nervousync Studio (NSYC) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nervousync.commons.beans.mail.protocol;

import java.io.Serializable;
import java.security.Security;
import java.util.*;

import org.nervousync.commons.beans.mail.config.ServerConfig;
import org.nervousync.commons.core.Globals;
import org.nervousync.utils.StringUtils;

/**
 * JavaMail base protocol
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: Jul 31, 2012 7:07:08 PM $
 */
public abstract class BaseProtocol implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6441571927997267674L;

	private static final String SSL_FACTORY_CLASS = "javax.net.ssl.SSLSocketFactory";

	/**
	 * Connection timeout parameter name
	 */
	protected String connectionTimeoutParam;
	/**
	 * Host parameter name
	 */
	protected String hostParam;
	/**
	 * Port parameter name
	 */
	protected String portParam;
	/**
	 * Timeout parameter name
	 */
	protected String timeoutParam;

	/**
	 * Constructor for define protocol type
	 */
	protected BaseProtocol() {
	}

	/**
	 * Read configuration for JavaMail using
	 *
	 * @param serverConfig      the server config
	 * @param connectionTimeout the connection timeout
	 * @param processTimeout    the process timeout
	 * @param userName          the user name
	 * @return java.util.Properties for JavaMail using
	 */
	public final Properties readConfig(ServerConfig serverConfig, int connectionTimeout, int processTimeout, String userName) {
		Properties properties = new Properties();

		properties.setProperty(this.hostParam, serverConfig.getHostName());
		int port = serverConfig.getHostPort();
		if (port != Globals.DEFAULT_VALUE_INT) {
			properties.setProperty(portParam, Integer.toString(port));
		}

		if (connectionTimeout > 0) {
			properties.setProperty(connectionTimeoutParam, Integer.toString(connectionTimeout * 1000));
		}
		if (processTimeout > 0) {
			properties.setProperty(timeoutParam, Integer.toString(processTimeout * 1000));
		}

		if (serverConfig.isSsl()) {
			Security.addProvider(Security.getProvider("SunJSSE"));
		}

		switch (serverConfig.getProtocolOption().toUpperCase()) {
			case "IMAP":
				properties.setProperty("mail.store.protocol", "imap");

				if (serverConfig.isAuthLogin()) {
					properties.setProperty("mail.imap.auth.plain.disable", "true");
					properties.setProperty("mail.imap.auth.login.disable", "true");
				}

				if (serverConfig.isSsl()) {
					properties.setProperty("mail.store.protocol", "imaps");
					properties.setProperty("mail.imap.socketFactory.class", SSL_FACTORY_CLASS);
					if (port != 0) {
						properties.setProperty("mail.imap.socketFactory.port", Integer.toString(port));
					}
					properties.setProperty("mail.imap.starttls.Enable", "true");
				}
				break;
			case "SMTP":
				properties.setProperty("mail.store.protocol", "smtp");
				properties.setProperty("mail.transport.protocol", "smtp");

				if (serverConfig.isAuthLogin()) {
					properties.setProperty("mail.smtp.auth", "true");
					if (StringUtils.notBlank(userName)) {
						properties.setProperty("mail.smtp.from", userName);
					}
				}

				if (serverConfig.isSsl()) {
					properties.setProperty("mail.store.protocol", "smtps");
					properties.setProperty("mail.smtp.ssl.enable", "true");
					properties.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY_CLASS);
					properties.setProperty("mail.smtp.socketFactory.fallback", "false");
					if (port != 0) {
						properties.setProperty("mail.smtp.socketFactory.port", Integer.toString(port));
					}
					properties.setProperty("mail.smtp.starttls.Enable", "true");
				}
				break;
			case "POP3":
				properties.setProperty("mail.store.protocol", "pop3");
				properties.setProperty("mail.transport.protocol", "pop3");

				if (serverConfig.isSsl()) {
					properties.setProperty("mail.store.protocol", "pop3s");
					properties.setProperty("mail.transport.protocol", "pop3s");
					properties.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY_CLASS);
					if (port != 0) {
						properties.setProperty("mail.pop3.socketFactory.port", Integer.toString(port));
					}
					properties.setProperty("mail.pop3.disabletop", "true");
					properties.setProperty("mail.pop3.ssl.enable", "true");
					properties.setProperty("mail.pop3.useStartTLS", "true");
				}
				break;
			default:
				return new Properties();
		}

		return properties;
	}
}
