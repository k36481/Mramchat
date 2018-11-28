package com.satirev.kim.mramchat;

public class Post {

    public String userName;
    public String mainText;
    public Integer like;

    public Post(){}

    public Post(String userName ,String  mainText){
        this.userName = userName;
        this.mainText = mainText;
        this.like =0;
    }


}
