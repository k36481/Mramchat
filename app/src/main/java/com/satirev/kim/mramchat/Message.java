package com.satirev.kim.mramchat;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Message {

    public String username;
    public String text;

    public Message() {
    }

    public Message(String username, String text){
        this.username =  username;
        this.text = text;
    }

}
