// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.clients;

import javax.mail.*;
import javax.mail.internet.*;
import com.sun.mail.smtp.SMTPSSLTransport;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.CEnv;


		/**
		 * <p>SMTP client used to push content to a list of email recipients.</p>
		 * @author Patrick Nicolas
		 * @date 12/05/2010
		 */
public final class CMailClient extends AClient {
	private Session 			_emailSession = null;
	private List<String>		_toList = null;
	private SMTPSSLTransport 	_sslProtocol = null;
	private String				_subject = null;
	
	
			/**
			 * <p>Create a SMTP client to push email to a specific SMTP server, on 
			 * the behalf of a sender, to a list of recipients.</p>
			 * @param servername Name of the SMTP server
			 * @param sender email address of the sender
			 * @param password
			 * @param toList List of recipients
			 */
	public CMailClient(CEnv env) {
		load(env.getConfiguration("user", "email"));
	}
	

			/**
			 * <p>Return the string of 'to list" recipient</p>
			 * @return concatenate list of email recipients..
			 */
	public final List<String> getToList() {
		return _toList;
	}
	
	public void setSubject(final String subject) {
		_subject = subject;
	}
	
			/**
			 * <p>Generate and send an email with a predefined subject and content. This client
			 * is connected and logged in if necessary.</p>
			 * @param subject Email subject
			 * @param content content or body of the email
			 */
	@Override
	public void update(final String content) throws IOException {
		if(_toList.size() == 0) {
			throw new NullPointerException("No recipient has been defined.");
		}
		
		if( connect() ) {
			Message msg = new MimeMessage(_emailSession);
		
			try {
				msg.setFrom(new InternetAddress(_userName));
				InternetAddress[] addresses = new InternetAddress[_toList.size()];
				
				int index = 0;
				for( String recipient : _toList) {
					addresses[index++] = new InternetAddress(recipient);
				}
				
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(_toList.get(0)));
				msg.setSubject(_subject);
				msg.setSentDate(new Date());
				msg.setText(content);
				msg.saveChanges();
				_sslProtocol.sendMessage(msg, addresses);
			}
			catch(AddressException e) {
				CLogger.error(e.getMessage());
			}
			catch(MessagingException e) {
				CLogger.error(e.getMessage());
			}
			catch(IllegalStateException e) {
				CLogger.error(e.getMessage());
			}
		}
	}
	
	
	
	public final String getToListString() {
		StringBuilder buf = new StringBuilder();
		for(String recipient : _toList) {
			buf.append(recipient);
			buf.append(";");
		}
		
		return buf.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("mail:");
		buf.append(_serverName);
		buf.append("from ");
		buf.append(_userName);
		buf.append("to: ");
		for( String recipient : _toList) {
			buf.append(recipient);
		}
		
		return buf.toString();
	}
	 
	
	
	/**
	 * <p>Initialize the properties for this user with the login name, password and
	 * content destination server.</p>
	 * @param properties key,value pairs of user information.
	 */
	@Override
	protected void load(final Map<String, String> properties) {
		super.load(properties);
		
		String toListStr = properties.get("target");
		
		String[] recipients = toListStr.split(";");
		if( recipients == null) {
			throw new NullPointerException("No email recipients found");
		}
		_toList = new ArrayList<String>();
		for( int j = 0; j < recipients.length; j++) {
			_toList.add(recipients[j]);
		}
	}
	
	


	
	private boolean connect() throws IOException  {
		boolean connectOK = true;
		
		if( !_isLoggedIn ) {
			Properties properties = new Properties();
			properties.put("mail.smtp.host", _serverName);
			properties.put("mail.debug", true);
			properties.put("mail.smtp.port", "465");
			properties.put("mail.smtp.auth", true);
			properties.put("mail.smtp.socketFactory.port", "465");
			properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.put("mail.smtp.socketFactory.fallback", "false");
			properties.put("mail.smtp.socketFactory.port", "465");
			properties.put("mail.smtp.starttls.enable", "true");
			
			Authenticator auth  = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(_userName, _password);
				}
			};
			_emailSession = Session.getInstance(properties, auth);
			
			try {
				_sslProtocol = (SMTPSSLTransport)_emailSession.getTransport("smtps");
				_sslProtocol.connect(_serverName, _userName, _password);
				_isLoggedIn = true;
			}
			catch(AddressException e) {
				CLogger.error(e.getMessage());
			}
			catch(MessagingException e) {
				CLogger.error(e.getMessage());
			}
			catch(IllegalStateException e) {
				CLogger.error(e.getMessage());
			}
		}
		
		return connectOK;
	}
}

// ------------------------------  EOF --------------------------------------------------