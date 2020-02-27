package com.wjh.dao;

import com.wjh.bean.User;

public interface UserDao {
    User getUserByName(String name);
    void offline(String ip_address);
    String searchFriend(String name);
    boolean changePassword(String username,String password,String newPassword);
    boolean register(String username,String password,String email);
    boolean login(String username,String password,String ip_address);
}
