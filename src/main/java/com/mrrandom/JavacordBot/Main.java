package com.mrrandom.JavacordBot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.javacord.api.AccountType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class Main {
	public static int mode = 0;
	private static HashMap<String, String>  qna;
	public static void main(String[] args) throws FileNotFoundException {
		qna = new HashMap<String, String>();
		populateMap(new File("src/main/resources/trivia/"));
		
		//logging in takes a while
		DiscordApi api = new DiscordApiBuilder()
			    .setAccountType(AccountType.CLIENT)
			    .setToken("Njk0Njc5Nzk3OTU5NDkxNzM1.XoPJvw._R_fD-8ocpeLOwAj-8ts_Q5zhd0") //token of "Buster" the trivia buster. 
			    .login().join();
		//Buster's email: ted27@rgdoubtdhq.com
        //Buster's email token: 100618816a175a1913c01ce59b5b3491
		//Buster's Password: Leon2003!
		
        //This listener does the busting
		List<BusterChannelListener> busting = new ArrayList<BusterChannelListener>();
		api.addMessageCreateListener(new MessageCreateListener() {
					@Override
					public void onMessageCreate(MessageCreateEvent event) {
						if(!api.getYourself().equals(event.getMessage().getAuthor().asUser().get()))
								return;
			        	if (event.getMessageContent().equalsIgnoreCase("/ping")) {
			                event.getChannel().sendMessage("Pong!");
			            	event.getMessage().delete();
			            }
			        	if (event.getMessageContent().toLowerCase().startsWith("/possible")) {
			        		mode++; if(mode >= 3) mode = 0; 
			        		event.getChannel().sendMessage("Winning against me is now "+currentMode());
			            	event.getMessage().delete();
			        	}
			        	if (event.getMessageContent().toLowerCase().startsWith("/trigger")) {
			                event.getChannel().sendMessage("Anyone up for some trivia?");
			            	event.getMessage().delete();
			            	
			            	String[] aa = event.getMessage().getContent().toLowerCase().split(" ");
			            	System.out.println("content: "+event.getMessage().getContent().toLowerCase());
			            	System.out.println("args: "+Arrays.toString(aa));
			            	
			            	if(aa.length <= 1) {
			            		newBuster(busting, event.getChannel(), event.getMessage());
			            	}
			            	else try {
			            		TextChannel tc = (TextChannel) api.getChannelById(aa[1]).get(); //this works because try catch
			            		Message ms = tc.getMessages(1).get().first();
			            		newBuster(busting, tc, ms);
			            	}
			            	catch(Exception e) {
			            		e.printStackTrace();
			            		event.getChannel().sendMessage("I'm not...");
			            	}
			            }
					}
				});
        System.out.println("Logged in!");
        //api.updateStatus();
    }
	public static String currentMode() {
		switch(mode) {
		case 0: return "***impossible***";
		case 1: return "possible :)";
		case 2: return "probable [i am human now]";
		}
		return "[error]";
	}
	public static void newBuster(List<BusterChannelListener> busting, TextChannel tc, Message m) {
		//construct new buster
    	BusterChannelListener bcl = new BusterChannelListener(qna,tc,m);
    	for(int i = busting.size()-1; i >= 0; i--) {
    		if(busting.get(i).equals(bcl)) {
    			System.out.println("Popping old listener");
    			busting.get(i).close();
    			busting.remove(i);
    		}
    		else if(!busting.get(i).isActive())
    			busting.remove(i);
    	}
    	bcl.start();
    	busting.add(bcl);
    	System.out.println("New buster started!");
    	tc.sendMessage("!trivia list");
	}
	public static void populateMap(final File folder) throws FileNotFoundException {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        	populateMap(fileEntry);
	        } else {
	            System.out.println(fileEntry.getName());
	            
	            //process the file (scanners are slow, but *eh*)
	            Scanner fileScan = new Scanner(fileEntry);
	            while(fileScan.hasNextLine()) {
	            	//get raw line and split it
	            	String[] split = fileScan.nextLine().split("`");
	            	if(split.length<2) //ignore if array is too small
	            		continue;
            		String q = split[0];
            		String a = ""; int i = 1;
            		while(a.isBlank() && i < split.length) {
            			//if it doesn't start with + or -, it is an answer
            			if(!(split[i].startsWith("+") || split[i].startsWith("-")) && !split[i].isBlank())
            				a = split[i];
            			i++;
            		}
            		//put in map
            		qna.put(simplifyString(q), a);
	            }
	            fileScan.close();
	        }
	    }
	}
	private static final char[] garbage = {' ','\'','�','�','�','+','-','.','\\','\"','/',',',';',':','*','(',')','@','!','?'};
	public static String simplifyString(String in) {
		for(char toRemove : garbage) {
			in = in.replace(toRemove, garbage[0]);
		}
		in = in.replace(garbage[0]+"", "");
		return in.strip().trim();
	}
	public static String getQuestion(String rawMessage) {
		for(String possibleQ : qna.keySet()) {
			if(simplifyString(rawMessage).contains(simplifyString(possibleQ)))
				return possibleQ;
		}
		return null;
	}
}
