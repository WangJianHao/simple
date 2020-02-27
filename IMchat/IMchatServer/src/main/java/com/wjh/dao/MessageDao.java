package com.wjh.dao;

import java.util.ArrayList;

public interface MessageDao {
    void insertRecord(String sender,String receiver,String msg,int status);
    ArrayList<String> getMessage(String username);
    void insertGroupRecord(String sender,String receiver,String msg,int status,String allName);
}
