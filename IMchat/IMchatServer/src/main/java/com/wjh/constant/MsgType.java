package com.wjh.constant;

public interface MsgType {
        int LOGIN = 1;
        int LOGIN_ACK = -1;
        int REGISTER = 2;
        int REGISTER_ACK = -2;
        int CHANGEPASSWORD = 3;
        int CHANGEPASSWORD_ACK = -3;
        int FORGETPASSWORD = 4;
        int FORGETPASSWORD_ACK = -4;
        int TEXT = 5;
        int TEXT_ACK = -5;
        int SEEFRIEND = 6;
        int SEEFRIEND_ACK = -6;
        int GROUPTEXT = 7;
        int GROUPTEXT_ACK = -7;
        int File = 8;
        int File_ACK = -8;
}
