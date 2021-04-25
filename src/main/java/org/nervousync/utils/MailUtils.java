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
package org.nervousync.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.pop3.POP3Folder;
import org.nervousync.commons.beans.mail.MailObject;
import org.nervousync.commons.beans.mail.MailServerConfig;
import org.nervousync.commons.beans.mail.authenticator.DefaultAuthenticator;
import org.nervousync.commons.core.Globals;
import org.nervousync.enumerations.mail.ProtocolOption;

/**
 * @author Steven Wee     <a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: Jul 31, 2012 8:54:04 PM $
 */
public final class MailUtils {

	private static transient final Logger LOGGER = LoggerFactory.getLogger(MailUtils.class);
	
	private MailUtils() {
	}

	/**
	 * Send mail
	 * @param mailServerConfig      mail server config
	 * @param mailObject            mail info
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @return                      send result
	 * @throws MessagingException   send failed
	 */
	public static boolean sendMessage(MailServerConfig mailServerConfig, MailObject mailObject,
	                                  String userName, String passWord) throws MessagingException {
		MimeMessage message =
				new MimeMessage(Session.getDefaultInstance(mailServerConfig.getSendConfigInfo(userName),
						new DefaultAuthenticator(userName, passWord)));
		
		message.setSubject(mailObject.getSubject(), mailObject.getCharset());
		
		MimeMultipart mimeMultipart = new MimeMultipart();
		
		if (mailObject.getAttachFiles() != null) {
			for (String attachment : mailObject.getAttachFiles()) {
				MimeBodyPart mimeBodyPart = new MimeBodyPart();
				
				File file;
				
				try {
					file = FileUtils.getFile(attachment);
				} catch (FileNotFoundException e) {
					return Globals.DEFAULT_VALUE_BOOLEAN;
				}
				
				DataSource dataSource = new FileDataSource(file);
				
				mimeBodyPart.setFileName(StringUtils.getFilename(attachment));
				mimeBodyPart.setDataHandler(new DataHandler(dataSource));
				
				mimeMultipart.addBodyPart(mimeBodyPart, mimeMultipart.getCount());
			}
		}
		
		if (mailObject.getIncludeFiles() != null) {
			List<String> includeFiles = mailObject.getIncludeFiles();
			for (String filePath : includeFiles) {
				File file;
				MimeBodyPart mimeBodyPart;
				
				try {
					file = FileUtils.getFile(filePath);
					String fileName = StringUtils.getFilename(filePath);
					mimeBodyPart = new MimeBodyPart();
					DataHandler dataHandler = 
							new DataHandler(new ByteArrayDataSource(file.toURI().toURL().openStream(),
									"application/octet-stream"));
					mimeBodyPart.setDataHandler(dataHandler);
					
					mimeBodyPart.setFileName(fileName);
					mimeBodyPart.setHeader("Content-ID", fileName);
				} catch (Exception e) {
					return Globals.DEFAULT_VALUE_BOOLEAN;
				}
				
				mimeMultipart.addBodyPart(mimeBodyPart, mimeMultipart.getCount());
			}
		}
		
		if (mailObject.getContent() != null) {
			String content = mailObject.getContent();
			
			if (mailObject.getContentMap() != null) {
				Map<String, String> argsMap = mailObject.getContentMap();

				for (Map.Entry<String, String> entry : argsMap.entrySet()) {
					content = StringUtils.replace(content, "###" + entry.getKey() + "###", entry.getValue());
				}
			}
			
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(content, mailObject.getContentType() + "; charset=" + mailObject.getCharset());
			mimeMultipart.addBodyPart(mimeBodyPart, mimeMultipart.getCount());
		}
		
		message.setContent(mimeMultipart);
		if (mailObject.getSendAddress() != null) {
			message.setFrom(new InternetAddress(mailObject.getSendAddress()));
		} else {
			message.setFrom(new InternetAddress(userName));
		}
		
		StringBuilder receiveAddress = new StringBuilder();
		
		for (String address : mailObject.getReceiveAddress()) {
			receiveAddress.append(",").append(address);
		}
		
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiveAddress.substring(1)));

		if (mailObject.getCcAddress() != null) {
			StringBuilder ccAddress = new StringBuilder();
			
			for (String address : mailObject.getCcAddress()) {
				ccAddress.append(",").append(address);
			}
			
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddress.substring(1)));
		}

		if (mailObject.getBccAddress() != null) {
			StringBuilder bccAddress = new StringBuilder();
			
			for (String address : mailObject.getBccAddress()) {
				bccAddress.append(",").append(address);
			}
			
			message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bccAddress.substring(1)));
		}

		if (mailObject.getReplyAddress() != null) {
			StringBuilder replyAddress = new StringBuilder();
			
			for (String address : mailObject.getReplyAddress()) {
				replyAddress.append(",").append(address);
			}
			
			message.setReplyTo(InternetAddress.parse(replyAddress.substring(1)));
		} else {
			if (mailObject.getSendAddress() != null) {
				message.setReplyTo(InternetAddress.parse(mailObject.getSendAddress()));
			} else {
				message.setReplyTo(InternetAddress.parse(userName));
			}
		}
		
		message.setSentDate(mailObject.getSendDate());
		
		Transport.send(message);
		
		return true;
	}

	/**
	 * Read mail info
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uid                   mail uid
	 * @param saveAttachPath        attach file save path
	 * @return                      read mail object instance
	 */
	public static MailObject getMailInfo(MailServerConfig mailServerConfig, String userName, 
			String passWord, String uid, String saveAttachPath) {

		try (Store store = connect(mailServerConfig, userName, passWord); Folder folder = openReadOnlyFolder(store)) {

			if (!folder.exists() || !folder.isOpen()) {
				return null;
			}

			Message message = null;
			if (mailServerConfig.getReceiveServerConfig().getProtocolOption().equals(ProtocolOption.POP3)) {
				Message[] messages = folder.getMessages();

				for (Message msg : messages) {
					if (((POP3Folder) folder).getUID(msg).equals(uid)) {
						message = msg;
						break;
					}
				}
			} else if (mailServerConfig.getReceiveServerConfig().getProtocolOption().equals(ProtocolOption.IMAP)) {
				message = ((IMAPFolder) folder).getMessageByUID(Long.parseLong(uid));
			}

			if (message != null) {
				return receiveMessage((MimeMessage) message, userName, true, saveAttachPath);
			}
		} catch (Exception e) {
			if (MailUtils.LOGGER.isDebugEnabled()) {
				MailUtils.LOGGER.debug("Receive Message Error! ", e);
			}
		}
		
		return null;
	}

	/**
	 * Connect to mail server
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @return                      Store instance
	 * @throws MessagingException   connect failed
	 */
	private static Store connect(MailServerConfig mailServerConfig, String userName, String passWord) throws MessagingException {
		Properties properties = mailServerConfig.getReceiveConfigInfo(userName);
		Session session = Session.getDefaultInstance(properties, new DefaultAuthenticator(userName, passWord));

		Store store = session.getStore(properties.getProperty("mail.store.protocol"));

		if (mailServerConfig.getReceiveServerConfig().getHostPort() == 0) {
			store.connect(mailServerConfig.getReceiveServerConfig().getHostName(), userName, passWord);
		} else {
			store.connect(mailServerConfig.getReceiveServerConfig().getHostName(),
					mailServerConfig.getReceiveServerConfig().getHostPort(), userName, passWord);
		}
		return store;
	}

	/**
	 * Read mail list
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uidList               mail uid list
	 * @param saveAttachPath        attach file save path
	 * @return                      read mail object instance list
	 */
	public static List<MailObject> getMailInfo(MailServerConfig mailServerConfig, String userName, 
			String passWord, List<String> uidList, String saveAttachPath) {
		List<MailObject> mailList = new ArrayList<>();

		try (Store store = connect(mailServerConfig, userName, passWord); Folder folder = openReadOnlyFolder(store)) {
			if (!folder.exists() || !folder.isOpen()) {
				return mailList;
			}

			List<Message> messageList = convertMailArraysToList(mailServerConfig, uidList, folder);

			for (Message message : messageList) {
				MailObject mailObject = receiveMessage((MimeMessage) message, userName, true, saveAttachPath);
				if (mailObject != null) {
					mailList.add(mailObject);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Receive Message Error! ");
			if (MailUtils.LOGGER.isDebugEnabled()) {
				MailUtils.LOGGER.debug("Stack message: ", e);
			}
		}
		
		return mailList;
	}

	/**
	 * Convert array message to list
	 * @param mailServerConfig      mail server config
	 * @param uidList               mail uid list
	 * @param folder                mail folder
	 * @return                      mail message list
	 * @throws MessagingException   read mail failed
	 */
	private static List<Message> convertMailArraysToList(MailServerConfig mailServerConfig,
	                                                     List<String> uidList, Folder folder) throws MessagingException {
		List<Message> messageList = new ArrayList<>();
		if (mailServerConfig.getReceiveServerConfig().getProtocolOption().equals(ProtocolOption.POP3)) {
			Message[] messages = folder.getMessages();

			for (Message message : messages) {
				if (uidList.contains(((POP3Folder)folder).getUID(message))) {
					messageList.add(message);
				}
			}
		} else if (mailServerConfig.getReceiveServerConfig().getProtocolOption().equals(ProtocolOption.IMAP)) {
			long[] uidArrays = new long[uidList.size()];

			for (int i = 0 ; i < uidList.size() ; i++) {
				uidArrays[i] = Long.parseLong(uidList.get(i));
			}
			Message[] messages = ((IMAPFolder)folder).getMessagesByUID(uidArrays);
			Collections.addAll(messageList, messages);
		}
		return messageList;
	}

	/**
	 * Read all mail list
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param saveAttachPath        attach file save path
	 * @return                      read mail object instance list
	 */
	public static List<MailObject> getMailList(MailServerConfig mailServerConfig, String userName, 
			String passWord, String saveAttachPath) {
		return getMailList(mailServerConfig, userName, passWord, null, saveAttachPath);
	}

	/**
	 * Read mail list
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param date                  receive date
	 * @param saveAttachPath        attach file save path
	 * @return                      read mail object instance list
	 */
	public static List<MailObject> getMailList(MailServerConfig mailServerConfig, String userName, 
			String passWord, Date date, String saveAttachPath) {
		List<MailObject> mailList = new ArrayList<>();

		try (Store store = connect(mailServerConfig, userName, passWord); Folder folder = openReadOnlyFolder(store)) {

			if (!folder.exists() || !folder.isOpen()) {
				return mailList;
			}

			Message[] messages = folder.getMessages();

			for (Message message : messages) {
				if (date == null || message.getReceivedDate().after(date)) {
					MailObject mailObject = receiveMessage((MimeMessage) message, userName, false, saveAttachPath);
					if (mailObject != null) {
						mailList.add(mailObject);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Receive Message Error! ");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Stack message: ", e);
			}
		}
		
		return mailList;
	}

	/**
	 * Remove mail by uid
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uid                   mail uid
	 */
	public static void removeMail(MailServerConfig mailServerConfig, String userName, 
			String passWord, String uid) {
		List<String> uidList = new ArrayList<>();
		uidList.add(uid);
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.DELETED, true);
	}

	/**
	 * Remove mails by uid list
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uidList               uid list
	 */
	public static void removeMails(MailServerConfig mailServerConfig, String userName, 
			String passWord, List<String> uidList) {
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.DELETED, true);
	}

	/**
	 * Recover mail by uid
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uid                   mail uid
	 */
	public static void recoverMail(MailServerConfig mailServerConfig, String userName, 
			String passWord, String uid) {
		List<String> uidList = new ArrayList<>();
		uidList.add(uid);
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.DELETED, false);
	}

	/**
	 * Recover mails by uid list
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uidList               uid list
	 */
	public static void recoverMails(MailServerConfig mailServerConfig, String userName, 
			String passWord, List<String> uidList) {
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.DELETED, false);
	}

	/**
	 * Set mail status as read by uid
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uid                   mail uid
	 */
	public static void readMail(MailServerConfig mailServerConfig, String userName, 
			String passWord, String uid) {
		List<String> uidList = new ArrayList<>();
		uidList.add(uid);
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.SEEN, true);
	}

	/**
	 * Set mails status as read by uid list
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uidList               uid list
	 */
	public static void readMails(MailServerConfig mailServerConfig, String userName, 
			String passWord, List<String> uidList) {
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.SEEN, true);
	}

	/**
	 * Set mail status as unread by uid
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uid                   mail uid
	 */
	public static void unreadMail(MailServerConfig mailServerConfig, String userName, 
			String passWord, String uid) {
		List<String> uidList = new ArrayList<>();
		uidList.add(uid);
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.SEEN, false);
	}

	/**
	 * Set mails status as unread by uid list
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uidList               uid list
	 */
	public static void unreadMails(MailServerConfig mailServerConfig, String userName, 
			String passWord, List<String> uidList) {
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.SEEN, false);
	}

	/**
	 * Set mail status as answered by uid
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uid                   mail uid
	 */
	public static void answerMail(MailServerConfig mailServerConfig, String userName, 
			String passWord, String uid) {
		List<String> uidList = new ArrayList<>();
		uidList.add(uid);
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.ANSWERED, true);
	}

	/**
	 * Set mails status as answered by uid list
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uidList               uid list
	 */
	public static void answerMails(MailServerConfig mailServerConfig, String userName, 
			String passWord, List<String> uidList) {
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.ANSWERED, true);
	}

	/**
	 * Set mail status as flagged by uid
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uid                   mail uid
	 */
	public static void flagMail(MailServerConfig mailServerConfig, String userName, 
			String passWord, String uid) {
		List<String> uidList = new ArrayList<>();
		uidList.add(uid);
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.FLAGGED, true);
	}

	/**
	 * Set mails status as flagged by uid list
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uidList               uid list
	 */
	public static void flagMails(MailServerConfig mailServerConfig, String userName, 
			String passWord, List<String> uidList) {
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.FLAGGED, true);
	}

	/**
	 * Set mail status as not flagged by uid
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uid                   mail uid
	 */
	public static void unflagMail(MailServerConfig mailServerConfig, String userName,
			String passWord, String uid) {
		List<String> uidList = new ArrayList<>();
		uidList.add(uid);
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.FLAGGED, false);
	}

	/**
	 * Set mails status as not flagged by uid list
	 * @param mailServerConfig      mail server config
	 * @param userName              login user name
	 * @param passWord              login pass word
	 * @param uidList               uid list
	 */
	public static void unflagMails(MailServerConfig mailServerConfig, String userName, 
			String passWord, List<String> uidList) {
		setMessageStatus(mailServerConfig, userName, passWord, uidList, Flags.Flag.FLAGGED, false);
	}

	/**
	 * Read mail info
	 * @param mimeMessage           MIME message instance
	 * @param receiveAddress        receive address
	 * @param detail                read detail
	 * @param saveAttachPath        attach file save path
	 * @return                      Mail object instance
	 * @throws MessagingException   read mail info error
	 * @throws IOException          save attach file error
	 */
	private static MailObject receiveMessage(MimeMessage mimeMessage, String receiveAddress, 
			boolean detail, String saveAttachPath) throws MessagingException, IOException {
		MailObject mailObject = new MailObject();

		InternetAddress[] internetAddresses = (InternetAddress[]) mimeMessage.getRecipients(IMAPMessage.RecipientType.TO);

		List<String> receiveList = new ArrayList<>();
		Arrays.asList(mimeMessage.getRecipients(IMAPMessage.RecipientType.TO)).forEach(address -> {
			if (address instanceof InternetAddress) {
				receiveList.add(((InternetAddress)address).getAddress());
			}
		});
		
		if (!receiveList.contains(receiveAddress)) {
			return null;
		}
		
		mailObject.setReceiveAddress(receiveList);
		
		Folder folder = mimeMessage.getFolder();
		
		if (folder instanceof POP3Folder) {
			mailObject.setUid(((POP3Folder)folder).getUID(mimeMessage));
		} else if (folder instanceof IMAPFolder) {
			mailObject.setUid(Long.valueOf(((IMAPFolder)folder).getUID(mimeMessage)).toString());
		}
		String subject = mimeMessage.getSubject();
		
		if (subject != null) {
			mailObject.setSubject(MimeUtility.decodeText(mimeMessage.getSubject()));
		} else {
			mailObject.setSubject("");
		}
		mailObject.setSendDate(mimeMessage.getSentDate());
		mailObject.setSendAddress(MimeUtility.decodeText(InternetAddress.toString(mimeMessage.getFrom())));
		
		if (detail) {
			//	Read mail cc address
			InternetAddress[] ccAddress = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);
			
			if (ccAddress != null) {
				List<String> ccList = new ArrayList<>();
		
				for (InternetAddress address : ccAddress) {
					ccList.add(address.getAddress());
				}
		
				mailObject.setCcAddress(ccList);
			}
	
			//	Read mail bcc address
			InternetAddress[] bccAddress = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);
	
			if (bccAddress != null) {
				List<String> bccList = new ArrayList<>();
				
				for (InternetAddress address : bccAddress) {
					bccList.add(address.getAddress());
				}
		
				mailObject.setBccAddress(bccList);
			}

			//	Read mail content message
			StringBuilder contentBuffer = new StringBuilder();
			getMailContent(mimeMessage, contentBuffer);
			mailObject.setContent(contentBuffer.toString());
			
			List<String> attachFiles = new ArrayList<>();
			getMailAttachment(mimeMessage, saveAttachPath, attachFiles);
			mailObject.setAttachFiles(attachFiles);
		}
		
		return mailObject;
	}
	
	private static void setMessageStatus(MailServerConfig mailServerConfig, String userName,
	                                     String passWord, List<String> uidList, Flags.Flag flag, boolean status) {
		try (Store store = connect(mailServerConfig, userName, passWord); Folder folder = openFolder(store, false)) {

			if (!folder.exists() || !folder.isOpen()) {
				return;
			}

			List<Message> messageList = convertMailArraysToList(mailServerConfig, uidList, folder);

			for (Message message : messageList) {
				message.setFlag(flag, status);
			}
		} catch (Exception e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Set message status error! ", e);
			}
		}
	}
	
	private static void getMailContent(Part part, StringBuilder contentBuffer) throws MessagingException, IOException {
		String contentType = part.getContentType();
		int nameIndex = contentType.indexOf("name");

		if (contentBuffer == null) {
			throw new IOException();
		}

		if (part.isMimeType(Globals.DEFAULT_EMAIL_CONTENT_TYPE_TEXT) && (nameIndex == -1)) {
			contentBuffer.append(part.getContent().toString());
		} else {
			if (part.isMimeType(Globals.DEFAULT_EMAIL_CONTENT_TYPE_HTML) && (nameIndex == -1)) {
				contentBuffer.append(part.getContent().toString());
			} else {
				if (part.isMimeType(Globals.DEFAULT_EMAIL_CONTENT_TYPE_MULTIPART)) {
					Multipart multipart = (Multipart) part.getContent();
					int count = multipart.getCount();
					for (int i = 0; i < count; i++) {
						getMailContent(multipart.getBodyPart(i), contentBuffer);
					}
				} else {
					if (part.isMimeType(Globals.DEFAULT_EMAIL_CONTENT_TYPE_MESSAGE_RFC822)) {
						getMailContent((Part) part.getContent(), contentBuffer);
					}
				}
			}
		}
	}
	
	private static void getMailAttachment(Part part, String saveAttachPath, List<String> saveFiles) throws MessagingException, IOException {
		if (saveFiles == null) {
			saveFiles = new ArrayList<>();
		}
		if (part.isMimeType(Globals.DEFAULT_EMAIL_CONTENT_TYPE_MULTIPART)) {
			Multipart multipart = (Multipart) part.getContent();
			int count = multipart.getCount();
			for (int i = 0; i < count; i++) {
				Part bodyPart = multipart.getBodyPart(i);
				if (bodyPart.getFileName() != null) {
					String disposition = bodyPart.getDisposition();
					if (disposition != null && (disposition.equals(Part.ATTACHMENT) || disposition.equals(Part.INLINE))) {
						boolean saveFile = FileUtils.saveFile(bodyPart.getInputStream(), 
								saveAttachPath + Globals.DEFAULT_PAGE_SEPARATOR + MimeUtility.decodeText(bodyPart.getFileName()));
						if (saveFile) {
							saveFiles.add(saveAttachPath + Globals.DEFAULT_PAGE_SEPARATOR + MimeUtility.decodeText(bodyPart.getFileName()));
						}
					} else if (bodyPart.isMimeType(Globals.DEFAULT_EMAIL_CONTENT_TYPE_MULTIPART)) {
						getMailAttachment(bodyPart, saveAttachPath, saveFiles);
					}
				}
			}
		}
	}

	private static Folder openReadOnlyFolder(Store store) 
			throws MessagingException {
		return openFolder(store, true);
	}
	
	private static Folder openFolder(Store store, boolean readOnly) 
			throws MessagingException {
		Folder folder = store.getFolder(Globals.DEFAULT_EMAIL_FOLDER_INBOX);
		
		if (readOnly) {
			folder.open(Folder.READ_ONLY);
		} else {
			folder.open(Folder.READ_WRITE);
		}
		
		return folder;
	}
}