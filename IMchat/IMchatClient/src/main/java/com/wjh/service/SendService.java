package com.wjh.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wjh.constant.MsgType;
import com.wjh.util.JsonUtil;
import io.netty.channel.Channel;

import java.util.Scanner;


public class SendService {
    private Channel channel;
    private Scanner scanner = new Scanner(System.in);
    public static String username;

    public SendService(Channel channel){
        this.channel = channel;
    }

    public void send(){
        retry:
        while (true){
            System.out.println("-------------------------登录菜单：-------------------");
            System.out.println("1.登录   2.注册  3.更改密码  4.忘记密码  5.退出系统");
            System.out.print("请输入：");
            String i = scanner.next();
            switch (i){
                case "1":
                    login();
                    break retry;
                case "2":
                    register();
                    break retry;
                case "3":
                    changePassword();
                    break retry;
                case "4":
                    forgetPassword();
                    break retry;
                case "5":
                    System.exit(0);
                    default:
                        System.out.println("输入错误的选项！请重新选择：");
                        continue retry;
            }
        }
    }

    private void login(){
        System.out.println("----------------登录---------------");
        System.out.print("请输入用户名：");
        String username = scanner.next();
        this.username = username;
        System.out.print("请输入密码：");
        String password = scanner.next();
        if(username != null && password != null){
            //封装给服务端JSON msg  msgType
            ObjectNode objectNode = JsonUtil.getObjectNode();
            objectNode.put("msgType",MsgType.LOGIN);
            objectNode.put("username",username);
            objectNode.put("password",password);

            //将用户名和密码发送给服务端
            channel.writeAndFlush(objectNode.toString());
        }

    }
    public void register(){
        System.out.println("----------------注册---------------");
        System.out.print("请输入用户名：");
        String username = scanner.next();
        System.out.print("请输入密码：");
        String password = scanner.next();
        System.out.print("请输入邮箱：");
        String email = scanner.next();
        if(username != null && password != null){
            //封装给服务端JSON msg  msgType
            ObjectNode objectNode = JsonUtil.getObjectNode();
            objectNode.put("msgType",MsgType.REGISTER);
            objectNode.put("username",username);
            objectNode.put("password",password);
            objectNode.put("email",email);

            //将用户名和密码发送给服务端
            channel.writeAndFlush(objectNode.toString());
        }
    }
    private void changePassword(){
        System.out.println("----------------更改密码---------------");
        System.out.print("请输入用户名：");
        String username = scanner.next();
        System.out.print("请输入旧密码：");
        String password = scanner.next();
        System.out.print("请输入新密码：");
        String newPassword = scanner.next();
        if(username != null && password != null){
            //封装给服务端JSON msg  msgType
            ObjectNode objectNode = JsonUtil.getObjectNode();
            objectNode.put("msgType",MsgType.CHANGEPASSWORD);
            objectNode.put("username",username);
            objectNode.put("password",password);
            objectNode.put("newPassword",newPassword);

            //将用户名和密码发送给服务端
            channel.writeAndFlush(objectNode.toString());
        }
    }

    /**
     * 输入用户名和邮箱来匹配，结果正确则发送密码至指定邮箱，结果不正确就返回错误信息
     */
    private void forgetPassword(){
        System.out.println("----------------忘记密码---------------");
        System.out.print("请输入用户名：");
        String username = scanner.next();
        System.out.print("请输入邮箱：");
        String email = scanner.next();
        if(username != null && email != null && !username.equals("")  && !email.equals("")){
            //封装给服务端JSON msg  msgType
            ObjectNode objectNode = JsonUtil.getObjectNode();
            objectNode.put("msgType",MsgType.FORGETPASSWORD);
            objectNode.put("username",username);
            objectNode.put("email",email);

            //将用户名和邮箱发送给服务端
            channel.writeAndFlush(objectNode.toString());
        }
    }
}
