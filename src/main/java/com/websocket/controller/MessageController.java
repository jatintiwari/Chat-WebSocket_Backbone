package com.websocket.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.Principal;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.websocket.log.Log;
import com.websocket.model.User;
import com.websocket.model.WebFile;
import com.websocket.model.WebMessage;
import com.websocket.service.MessageService;
import com.websocket.service.UserService;
import com.websocket.util.LoginUtil;
import com.websocket.util.TimeConversion;


@Controller
public class MessageController {


	@Autowired
	SimpMessagingTemplate template;

	@Autowired
	UserService userService;

	@Autowired
	MessageService messageService;
	
	@Value("${filePath}")
	String filePath;

	@MessageMapping("/message")
	public synchronized void greeting(Message<Object> message1,WebMessage message, Principal principal) throws Exception {
		try{
			if(principal.equals(null)){
				Log.info("User unknonwn!!");
				return;
			}
			String fromUser = principal.getName();
			Log.info("Message form "+fromUser+" to user "+message.getToUser()+" is "+message.getMessage());
			message.setFromUser(fromUser);
			message.setTime(TimeConversion.getTime());
			//prepare to send message
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("fromUser", fromUser);
			jsonObject.put("toUser", message.getToUser());
			jsonObject.put("message", message.getMessage());
			jsonObject.put("time", TimeConversion.getCurrentTime());
			jsonObject.put("date", TimeConversion.getCurrentDate());
			//saving message
			Long id = messageService.saveMessage(message);
			Log.info("Message saved with id  :: "+id);
			jsonObject.put("id", id);
			this.template.convertAndSendToUser(message.getToUser(), "/websocket/message", jsonObject.toString());
			Log.info("Message sent!");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@MessageMapping(value="/messages/read")
	public void markAsRead(WebMessage message,Principal principal){
		String currentUsername = principal.getName();
		User user = userService.getUserByUserName(currentUsername);
		if(!user.equals(null)){
			Log.info("from  :: "+message.getFromUser()+"  to ::"+message.getToUser());
			Long unReadMessages = messageService.getUnReadMessageCount(message.getFromUser(),message.getToUser());
			if(unReadMessages>0){
				try{
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("markRead", "TRUE");
					jsonObject.put("fromUser", message.getFromUser());
					jsonObject.put("toUser", message.getToUser());
					messageService.markeMessagesAsRead(message.getFromUser(),message.getToUser());
					this.template.convertAndSendToUser(message.getFromUser(), "/websocket/message", jsonObject.toString());
					Log.info("Messages marked as read");
				}catch(Exception e){
					e.printStackTrace();
					Log.info("Messages not marked as read");
				}
			}
		}
	}


	//sends the collection of messages :: the conversation between the two users.
	@RequestMapping(value="/messages", method=RequestMethod.GET)
	public @ResponseBody String getMessagesList(@RequestParam(value="otherUser",required=false)String otherUser) throws Exception{
		String currentUsername = LoginUtil.getCurrentUsername();
		User user = userService.getUserByUserName(currentUsername);
		if(user==null){
			Log.info("user is not logged in!!");
			return "{\"success\":\"false\", \"message\":\"Cannot access messages list.\"}";
		}else if(otherUser == "" || otherUser.equals(null)){
			Log.info("user is not logged in!!");
			return "{\"success\":\"false\", \"message\":\"Unknown user\"}";
		}

		JSONObject jsonObject;
		JSONArray jsonArray = new JSONArray();
		try{
			Collection<WebMessage> converstation = messageService.getConversation(currentUsername,otherUser);
			if(converstation.size()!=0){
				for(WebMessage message : converstation){
					jsonObject =  new JSONObject();
					jsonObject.put("id", message.getId());
					jsonObject.put("fromUser", message.getFromUser());
					jsonObject.put("toUser", message.getToUser());
					jsonObject.put("message", message.getMessage());
					jsonObject.put("time", TimeConversion.getSimpleTime(message.getTime()));
					jsonObject.put("date", TimeConversion.getSimpleDate(message.getTime()));
					jsonObject.put("messageRead", message.isMessageRead());
					jsonArray.put(jsonObject);
				}
				Log.info("Successfully retrieved the conversation between "+currentUsername+" and "+otherUser+" :: "+jsonArray.toString());
				return jsonArray.toString();
			}
			Log.info("No conversation found.");
			jsonObject =  new JSONObject();
			jsonObject.put("success","false");
			jsonObject.put("fromUser",otherUser);
			jsonObject.put("toUser",currentUsername );
			jsonObject.put("message","Start your conversation.");
			return jsonObject.toString();

		}catch(Exception e){
			e.printStackTrace();
			Log.error("Exception retrieving conversation",e);
			return "{\"success\":\"Error\", \"message\":\"Sorry!! We are looking into this problem.\"}";
		}
	}


	@MessageMapping(value="/file")
	public void file(Message<Object> message1,WebFile file,Principal principal){
		try{
			if(principal.equals(null)){
				Log.info("User unknonwn!!");
				return;
			}
			String fromUser = principal.getName();
			Log.info("Message form "+fromUser+" to user "+file.getToUser()+" is "+file.getName());
			file.setFromUser(fromUser);
			file.setTime(TimeConversion.getTime());
			//prepare to send message
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("fromUser", fromUser);
			jsonObject.put("toUser", file.getToUser());
			jsonObject.put("size", file.getSize());
			jsonObject.put("type", file.getType());
			jsonObject.put("time", TimeConversion.getCurrentTime());
			jsonObject.put("date", TimeConversion.getCurrentDate());
			// create an array of the base64 string
			String base64String = file.getFile().split(",")[1];
			byte[] fileArray = Base64.decodeBase64(base64String);
			File newFile;
			int i=0;
			do{
				newFile = new File(filePath+File.separator+file.getName());
				if(newFile.exists()){
					String[] fileExt = file.getName().split("\\.");
					file.setName(fileExt[0].split("-")[0]+"-"+(++i)+"."+fileExt[1]);
				}
			}while(newFile.exists());
			Log.info("File will be saved as "+file.getName());
			FileOutputStream fos = new FileOutputStream(newFile);
			//pass the deocoded stream of array bytes to fos
			fos.write(fileArray);
			fos.flush();
			fos.close();
			//saving fileInfo
			Long id = messageService.saveFile(file);
			Log.info("File saved with id  :: "+id);
			jsonObject.put("name", file.getName());
			jsonObject.put("id", id);
			this.template.convertAndSendToUser(file.getToUser(), "/websocket/file", jsonObject.toString());
			Log.info("File sent!");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@RequestMapping(value="/files", method=RequestMethod.GET)
	public @ResponseBody String files(@RequestParam(value="fromUser", required=false) String fromUser){
		if(fromUser=="" || fromUser==null){
			Log.info("from user is null");
			return "{\"success\":\"false\",\"message\":\"From user null\"}";
		}
		JSONObject jsonObject=null;
		String currentUser = LoginUtil.getCurrentUsername();
		try {
			JSONArray jsonArray =new JSONArray();
			Collection<WebFile> files = messageService.getFiles(currentUser,fromUser);
			if(files.size()>0){
				for(WebFile file : files){
					jsonObject = new JSONObject();
					jsonObject.put("id", file.getId());
					jsonObject.put("name", file.getName());
					jsonObject.put("size", file.getSize());
					jsonObject.put("type", file.getType());
					jsonObject.put("fromUser", file.getFromUser());
					jsonObject.put("toUser", file.getToUser());
					jsonArray.put(jsonObject);
				}
				Log.info("Files fromUser :: "+fromUser+" for :: "+currentUser);
				return jsonArray.toString();
			}else{
				Log.info("No files found!!");
				jsonObject =  new JSONObject();
				jsonObject.put("success","false");
				jsonObject.put("fromUser",fromUser);
				jsonObject.put("toUser",currentUser );
				jsonObject.put("message","No Files");
				return jsonObject.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"success\":\"Error\", \"message\":\"Sorry!! We are looking into this problem.\"}";
		}
		
	}

	@RequestMapping(value="/file", method=RequestMethod.GET)
	public void getAFile(@RequestParam("name") String name,HttpServletResponse response,
			HttpServletRequest request){
		try {
			WebFile webFile = messageService.getFile(name);
			if(!webFile.equals(null)){
				Log.info("File delivered in response");
				File file =  new File("/Users/Tiwari/git/Chat-WebSocket_Backbone/src/main/resources/"+webFile.getName());
				response.setHeader("Content-Disposition", "attachment;filename="+file.getName());
				response.setHeader("Cache-Control", "private");
				String mimeType = request.getServletContext().getMimeType(file.getAbsolutePath());
				response.setContentType(mimeType);
				response.setContentLength((int)file.length());
				Files.copy(file.toPath(), response.getOutputStream());
			}else{
				Log.info("File not found");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
