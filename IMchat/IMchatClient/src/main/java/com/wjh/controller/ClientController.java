package com.wjh.controller;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.wjh.bean.ChatClientFrame;
import com.wjh.bean.FriendListFrame;
import com.wjh.bean.GroupChatFrame;
import com.wjh.constant.FrameSize;
import com.wjh.constant.MsgType;
import com.wjh.service.OnlineService;
import com.wjh.service.SendService;
import com.wjh.util.JsonUtil;
import io.netty.channel.Channel;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.util.Hashtable;


public class ClientController {
    public static String message = "";
    public static FriendListFrame friendListFrame;
    public static Channel channel;
    public static String[] friendList;
    public static final Hashtable<String,ChatClientFrame> frames = new Hashtable<>();
    public static final Hashtable<String,GroupChatFrame> groups = new Hashtable<>();
    public static void process(String msg){
        //解析客户端发送的消息
        ObjectNode objectNode = JsonUtil.getObjectNode(msg);
        //获取到发送的消息类型
        int msgType = objectNode.get("msgType").asInt();
//        System.out.println(msgType);

        switch (msgType){
            case MsgType.LOGIN_ACK:
                handleLogin(objectNode);
                break;
            case MsgType.REGISTER_ACK:
                handleRegister(objectNode);
                break;
            case MsgType.CHANGEPASSWORD_ACK:
                handleChangePassword(objectNode);
                break;
            case MsgType.FORGETPASSWORD_ACK:
                handleForgetPassword(objectNode);
                break;
            case MsgType.TEXT_ACK:
                handleText(objectNode);
                break;
            case MsgType.SEEFRIEND_ACK:
                handleSearchFriend(objectNode);
                break;
            case MsgType.GROUPTEXT_ACK:
                handleGroupText(objectNode);
                break;
            case MsgType.File_ACK:
                handleFile(objectNode);
                break;
        }
    }

    /**
     * 接收文件
     * @param objectNode
     */
    private static void handleFile(ObjectNode objectNode) {
        String sender = objectNode.get("sender").asText();
        String receiver = objectNode.get("receiver").asText();
        String fileName = objectNode.get("fileName").asText();
        try {
            String string = objectNode.get("file").asText();
            byte[] decode = Base64.decode(string);
            File file = new File(FrameSize.DEFAULT_FILE_PATH+fileName);
            file.createNewFile();
            if(file.exists()){
                String[] split = fileName.split(".");
                file = new File(FrameSize.DEFAULT_FILE_PATH+split[0]+"(1)."+split[1]);
            }
            //将信息写入到文件中
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(decode);
            JOptionPane.showMessageDialog(null,"接收到来自"+sender+"的文件，已存放至"+FrameSize.DEFAULT_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Base64DecodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接受忘记密码的结果
     * @param objectNode
     */
    private static void handleForgetPassword(ObjectNode objectNode) {
        boolean status = objectNode.get("status").asBoolean();
        if(status){//匹配结果正确
            System.out.println("密码已发送至指定邮箱！请查收！");
        }else{//匹配结果不正确
            System.out.println("用户名或密码错误！请重新选择！");
        }
        new SendService(channel).send();
    }


    /**
     * 接受更改密码的结果
     * @param objectNode
     */
    private static void handleChangePassword(ObjectNode objectNode) {
        boolean status = objectNode.get("status").asBoolean();
        if(status){
            System.out.println("更改密码成功！");
        }else{
            System.out.println("更改密码失败");
        }
        new SendService(channel).send();
    }

    /**
     * 接受注册的结果
     * @param objectNode
     */
    private static void handleRegister(ObjectNode objectNode) {
        //注册的成功与否
        boolean status = objectNode.get("status").asBoolean();
        if(status){
            System.out.println("注册成功！");
            new SendService(channel).send();
        }else{
            System.out.println("注册失败！请重新选择！");
            new SendService(channel).send();
        }
    }

    /**
     * 接收并打印好友列表
     * @param objectNode
     */
    private static void handleSearchFriend(ObjectNode objectNode) {
        String result = objectNode.get("friendList").asText();
        friendList = result.split(",");
        if(friendListFrame == null || !friendListFrame.isShowing()) {
            friendListFrame = new FriendListFrame();
        }else{
            //业务是其他人上线时让服务端执行查询好友的逻辑
            //刷新好友列表
            friendListFrame.initFriendList();
        }

    }

    /**
     * 接收登录的结果
     * @param objectNode
     */
    private static void handleLogin(ObjectNode objectNode) {
        boolean status = objectNode.get("status").asBoolean();
        if(status){
            System.out.println("登录成功！");
            OnlineService.channel = channel;
            OnlineService.searchFriend();
//            new OnlineService(channel).online();
        }else{
            System.out.println("登录失败！请重新选择！");
            new SendService(channel).send();
        }
    }

    /**
     * 接收消息
     * @param objectNode
     */
    private static void handleText(ObjectNode objectNode) {
//        System.out.println("接收到离线消息");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //身为接收方只用知道发送方的名字即可
                String sender = objectNode.get("sender").asText();
                String msg = objectNode.get("msg").asText();
//                JOptionPane.showMessageDialog(null,"您有来自"+sender+"的消息！");
                //根据发送者名字获取对应的聊天窗口
                ChatClientFrame chatClientFrame = frames.get(sender);

                if(chatClientFrame == null){
                    chatClientFrame = new ChatClientFrame(sender);
                    frames.put(sender,chatClientFrame);
                    chatClientFrame.showRecvMsg(msg);
                }else{
                    chatClientFrame.showRecvMsg(msg);
                }

            }
        }).start();
    }
    /**
     * 处理群聊消息
     * @param objectNode
     */
    private static void handleGroupText(ObjectNode objectNode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //身为接收方只用知道发送方的名字即可
                String sender = objectNode.get("sender").asText();
                String allName = objectNode.get("allName").asText();
                String msg = objectNode.get("msg").asText();
//                JOptionPane.showMessageDialog(null,"您有来自"+sender+"的消息！");
                //根据发送者名字获取对应的聊天窗口
                GroupChatFrame groupChatFrame = groups.get(allName);

                if(groupChatFrame == null){
                    groupChatFrame = new GroupChatFrame(allName);
                    groups.put(allName,groupChatFrame);
                    groupChatFrame.showRecvMsg(sender,msg);
                }else{
                    groupChatFrame.showRecvMsg(sender,msg);
                }

            }
        }).start();
    }
}
