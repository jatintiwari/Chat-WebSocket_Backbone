package com.websocket.service;

import java.util.Collection;

import com.websocket.model.WebFile;
import com.websocket.model.WebMessage;

public interface MessageService {

	Collection<WebMessage> getConversation(String currentUsername,String otherUser);
	Long saveMessage(WebMessage message);
	Long getUnReadMessageCount(String fromUser, String toUser);
	void markeMessagesAsRead(String fromUser, String toUser);
	Long getUnReadMessageCountForUser(String username);
	Long saveFile(WebFile file);
	Collection<WebFile> getFiles(String currentUser, String fromUser);
	WebFile getFile(String id);

}
