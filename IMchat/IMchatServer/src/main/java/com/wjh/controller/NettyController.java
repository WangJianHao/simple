package com.wjh.controller;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wjh.bean.User;
import com.wjh.constant.MsgType;
import com.wjh.dao.MessageDaoImpl;
import com.wjh.dao.UserDaoImpl;
import com.wjh.service.NettyControllerDaoImpl;
import com.wjh.util.EmailUtil;
import com.wjh.util.JsonUtil;
import io.netty.channel.Channel;
import org.apache.commons.mail.EmailException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


public class NettyController {
    public String message = "";
    public Channel channel;
    MessageDaoImpl messageDao = new MessageDaoImpl();
    UserDaoImpl userDao = new UserDaoImpl();
     private  User user;
    /**
     * netty发送的业务请求全部在该方法处理
     * @param msg
     * @return
     */
    public   String process(String msg){
        //解析客户端发送的消息
        ObjectNode objectNode = JsonUtil.getObjectNode(msg);

        //获取到发送的消息类型
        int msgType = objectNode.get("msgType").asInt();


        switch (msgType){
            case MsgType.LOGIN:
                 handleLogin(objectNode);
            case MsgType.REGISTER:
                return handleRegister(objectNode);
            case MsgType.CHANGEPASSWORD:
                return handleChangePassword(objectNode);
            case MsgType.FORGETPASSWORD:
                return  handleForgetPassword(objectNode);
            case MsgType.TEXT:
                 handleText(objectNode);
                 break;
            case MsgType.SEEFRIEND:
                return handleSearchFriend();
            case MsgType.GROUPTEXT:
                handleGroupText(objectNode);
                break;
            case MsgType.File:
                handleFile(objectNode);
                break;
        }

        return null;
    }

    private void handleFile(ObjectNode objectNode) {
        //将json封装的属性读取出来
        String sender = objectNode.get("sender").asText();
        String receiver = objectNode.get("receiver").asText();
        String fileName = objectNode.get("fileName").asText();
        String file= objectNode.get("file").asText();


        //将属性封装起来发送给对应的用户
        ObjectNode jsonNodes = JsonUtil.getObjectNode();
        jsonNodes.put("msgType",MsgType.File_ACK);
        jsonNodes.put("sender",sender);
        jsonNodes.put("receiver",receiver);
        jsonNodes.put("fileName",fileName);
        jsonNodes.put("file",file);

        //负责将信息写过去
        String receiver_id_address = userDao.getIP(receiver);
        NettyController nettyControllerByIP = NettyControllerDaoImpl.
                getNettyControllerByIP(receiver_id_address);
        nettyControllerByIP.channel.writeAndFlush(jsonNodes.toString());
        System.out.println("已发送至"+receiver);

    }

    /**
     * 处理忘记密码的业务逻辑
     * 1.获取用户名和邮箱
     * 2.与数据库进行匹配，结果正确为true，不正确为false
     * 3.如果匹配结果正确，就发送邮箱，不正确就直接返回
     * @param objectNode
     * @return
     */
    private String handleForgetPassword(ObjectNode objectNode) {
        String username = objectNode.get("username").asText();
        String email = objectNode.get("email").asText();
        String password = userDao.forgetPassword(username, email);
        ObjectNode jsonNodes = JsonUtil.getObjectNode();
        jsonNodes.put("msgType",MsgType.FORGETPASSWORD_ACK);

        if(password != null){
            jsonNodes.put("status",true);
            try {
                EmailUtil.toEmail(email,password);
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }else{
            jsonNodes.put("status",false);
        }
        return jsonNodes.toString();
    }


    /**
     * 处理更改密码的业务逻辑
     * @param objectNode
     * @return
     */
    private  String handleChangePassword(ObjectNode objectNode) {
        String username = objectNode.get("username").asText();
        String password = objectNode.get("password").asText();
        String newPassword = objectNode.get("newPassword").asText();
        boolean status = userDao.changePassword(username, password, newPassword);
        ObjectNode jsonNodes = JsonUtil.getObjectNode();
        jsonNodes.put("msgType",MsgType.CHANGEPASSWORD_ACK);
        jsonNodes.put("status",status);
        return jsonNodes.toString();
    }

    /**
     * 处理注册的业务逻辑
     * @param objectNode
     * @return
     */
    private  String handleRegister(ObjectNode objectNode) {
        String username = objectNode.get("username").asText();
        String password = objectNode.get("password").asText();
        String email = objectNode.get("email").asText();
        boolean register = userDao.register(username, password,email);
        ObjectNode jsonNodes = JsonUtil.getObjectNode();
        jsonNodes.put("msgType",MsgType.REGISTER_ACK);
        jsonNodes.put("status",register);

        return jsonNodes.toString();
    }

    /**
     * 获取好友列表
     * @return
     */
    private  String handleSearchFriend() {
        String result = userDao.searchFriend(user.getName());
        ObjectNode jsonNodes = JsonUtil.getObjectNode();
        jsonNodes.put("msgType",MsgType.SEEFRIEND_ACK);
        jsonNodes.put("friendList",result);
        return jsonNodes.toString();
    }

    /**
     * 处理登录业务的方法
     * 用户登陆以后需要向好友通知更新好友列表
     * @param objectNode
     * @return
     */
    private  void  handleLogin(ObjectNode objectNode) {
        String username = objectNode.get("username").asText();
        String password = objectNode.get("password").asText();
        boolean login = userDao.login(username, password, channel.remoteAddress().toString());
        ObjectNode jsonNodes = JsonUtil.getObjectNode();
        jsonNodes.put("msgType", MsgType.LOGIN_ACK);
        jsonNodes.put("status", login);

        channel.writeAndFlush(jsonNodes.toString());

        if (login) {
            user = userDao.getUserByName(username);//给user赋值方便操作
            //通过数据库查找所有在线的人，然后向他们发送消息  以查询好友列表的形式


            new Thread(new Runnable() {
                @Override
                public void run() {
                    //该线程负责更新其他人的好友列表状况
                    String[] split = userDao.getAllOnlineFriends().split(",");
                    for (int i = 0; i < split.length; i++) {
                        NettyController nettyControllerByIP = NettyControllerDaoImpl.
                                getNettyControllerByIP(userDao.getIP(split[i]));
                        Channel channel = nettyControllerByIP.channel;
                        String s = nettyControllerByIP.handleSearchFriend();

//                System.out.println(s);
                        channel.writeAndFlush(s);
                    }

                }
            }).start();


            {//此代码块解决离线的单聊消息
                //username为receiver，获取所有receiver是username的消息
                ArrayList<String> message = messageDao.getMessage(username);
//            NettyController nettyControllerByIP = NettyControllerDaoImpl.
//                    getNettyControllerByIP(userDao.getIP(username));
                Iterator<String> iterator = message.iterator();
                while (iterator.hasNext()) {
                    String next = iterator.next();
//                String[] split = next.split(":");
//                String sender = split[0];
//                String msg = split[1];
                    int i = next.indexOf(":");
                    String sender = next.substring(0, i);
                    String msg = next.substring(i + 1, next.length());
                    ObjectNode jsonNode = JsonUtil.getObjectNode();
                    jsonNode.put("msgType", MsgType.TEXT_ACK);
                    jsonNode.put("sender", sender);
                    jsonNode.put("receiver", username);
                    jsonNode.put("msg", msg);

                    System.out.println(channel.remoteAddress()+"的离线消息：" + jsonNode);
//                    System.out.println(channel);


                    channel.writeAndFlush(jsonNode.toString());
//                nettyControllerByIP.channel.writeAndFlush(jsonNodes.toString());
                }
            }

            //这个代码块解决群聊消息
            {
                ArrayList<ObjectNode> message = messageDao.getGroupMessage(username);
                Iterator<ObjectNode> iterator = message.iterator();
                while (iterator.hasNext()) {
//                    String next = iterator.next();
//                    next.split("");
//                    ObjectNode jsonNode = JsonUtil.getObjectNode();
//                    jsonNode.put("msgType", MsgType.TEXT_ACK);
//                    jsonNode.put("sender", sender);
//                    jsonNode.put("receiver", username);
//                    jsonNode.put("allName",allName);
//                    jsonNode.put("msg", msg);
                    ObjectNode jsonNode = iterator.next();
//                    System.out.println("离线消息：" + jsonNode);
//                    System.out.println(channel);

                    channel.writeAndFlush(jsonNode.toString());


                }
            }
        }
    }


    /**
     * 接受客户端发送的消息并转发
     * 1.检验对方是否在线
     * 2.先将消息存放到数据库中
     * 3.如果在线直接转发   找到接收方所在的channel然后返回
     * 4.如果不在线就结束，等到对方登录遍历消息表把消息发过去
     * @param objectNode
     * @return
     */
    private  void  handleText(ObjectNode objectNode){
        //消息只包括接收方和消息的内容
        String receiver = objectNode.get("receiver").asText();
        String msg = objectNode.get("msg").asText();
        String sender = objectNode.get("sender").asText();
        boolean online = userDao.isOnline(receiver);
        if(online){
            //先将消息保存到数据库中
            messageDao.insertRecord(sender,receiver,msg,1);
            ObjectNode jsonNodes = JsonUtil.getObjectNode();
            jsonNodes.put("msgType",MsgType.TEXT_ACK);
            jsonNodes.put("sender",sender);
            jsonNodes.put("receiver",receiver);
            jsonNodes.put("msg",msg);

            //负责将信息写过去
            String receiver_id_address = userDao.getIP(receiver);
            System.out.println("----"+jsonNodes.toString());
            NettyController nettyControllerByIP = NettyControllerDaoImpl.
                    getNettyControllerByIP(receiver_id_address);
            nettyControllerByIP.channel.writeAndFlush(jsonNodes.toString());

        }else{
            messageDao.insertRecord(sender,receiver,msg,0);
        }

    }
    private void handleGroupText(ObjectNode objectNode) {
        //消息只包括接收方和消息的内容
        String receiver = objectNode.get("receiver").asText();
        String msg = objectNode.get("msg").asText();
        String allName = objectNode.get("allName").asText();
        String sender = objectNode.get("sender").asText();
        boolean online = userDao.isOnline(receiver);
        if(online){
            //先将消息保存到数据库中
            messageDao.insertGroupRecord(sender,receiver,msg,1,allName);
            ObjectNode jsonNodes = JsonUtil.getObjectNode();
            jsonNodes.put("msgType",MsgType.GROUPTEXT_ACK);
            jsonNodes.put("sender",sender);
            jsonNodes.put("allName",allName);
            jsonNodes.put("receiver",receiver);
            jsonNodes.put("msg",msg);
            System.out.println(receiver);

            //负责将信息写过去
            String receiver_id_address = userDao.getIP(receiver);
            System.out.println("----"+jsonNodes.toString());
            NettyController nettyControllerByIP = NettyControllerDaoImpl.
                    getNettyControllerByIP(receiver_id_address);
            nettyControllerByIP.channel.writeAndFlush(jsonNodes.toString());

        }else{
            messageDao.insertGroupRecord(sender,receiver,msg,0,allName);
        }

    }
}
