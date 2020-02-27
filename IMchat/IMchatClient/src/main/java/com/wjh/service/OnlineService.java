package com.wjh.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wjh.bean.ChatClientFrame;
import com.wjh.constant.MsgType;
import com.wjh.controller.ClientController;
import com.wjh.util.JsonUtil;
import io.netty.channel.Channel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.util.Hashtable;
import java.util.Scanner;

public class OnlineService {
    public static Channel channel;
    private Scanner scanner = new Scanner(System.in);

    public OnlineService(Channel channel){
        this.channel = channel;
    }

//    public void online(){
//        retry:
//        while (true){
//            System.out.println("功能菜单：");
//            System.out.println("1.查看好友   2.选择好友聊天   3.群发消息   4.选择好友发送文件  5.群发文件   6.下线");
//            System.out.print("请输入：");
//            int i = scanner.nextInt();
//            switch (i){
//                case 1:
//                    searchFriend();
//                    break retry;
//                case 2:
//                    chatWithFriend0();
//                    break retry;
//                case 3:
//
//                    break retry;
//                case 4:
//
//                    break retry;
//                case 5:
//
//                    break retry;
//                case 6:
//                    System.exit(0);
//                    break retry;
//                default:
//                    System.out.println("输入错误的选项！请重新选择：");
//                    continue retry;
//            }
//        }
//    }

    public  void chatWithFriend0() {
        System.out.print("接收人：");
        String username = scanner.next();
        Hashtable<String, ChatClientFrame> frames = ClientController.frames;
        ChatClientFrame chatClientFrame = frames.get(username);
        if(chatClientFrame == null){
            chatClientFrame = new ChatClientFrame(username);
            frames.put(username,chatClientFrame);
        }else{
            chatClientFrame.show();
        }
    }

    /**
     * names中包括很多人的名字
     * @param allName
     * @param names
     * @param msg
     */
    public static void chatWithGroup(String allName,String[] names,String msg){
       for(int i =0;i<names.length;i++) {
           if(SendService.username.equals(names[i])){
               continue;
           }
           ObjectNode objectNode = JsonUtil.getObjectNode();
           objectNode.put("msgType", MsgType.GROUPTEXT);
           objectNode.put("sender", SendService.username);
           objectNode.put("receiver", names[i]);//接收人的用户名
           objectNode.put("allName",allName);
           objectNode.put("msg", msg);
           System.out.println("发送消息：" + objectNode.toString());

           //将内容发送给服务端
           channel.writeAndFlush(objectNode.toString());
           try {
               Thread.sleep(100);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
    }


    /**
     * 给name发送消息
     * @param name
     * @param msg
     */
    public  static void chatWithFriend(String name,String msg) {
        //封装给服务端JSON msg  msgType
        ObjectNode objectNode = JsonUtil.getObjectNode();
        objectNode.put("msgType",MsgType.TEXT);
        objectNode.put("sender",SendService.username);
        objectNode.put("receiver",name);//接收人的用户名
        objectNode.put("msg",msg);
        System.out.print("发送消息："+objectNode.toString());


        //将内容发送给服务端
        channel.writeAndFlush(objectNode.toString());
    }

    public static void searchFriend(){
        //封装给服务端JSON msg  msgType
        ObjectNode objectNode = JsonUtil.getObjectNode();
        objectNode.put("msgType",MsgType.SEEFRIEND);
        System.out.println("正在更新好友列表状态ing。。。");
        System.out.println("正在获取是否有离线消息ing。。。");
        //将请求发送给服务端
        channel.writeAndFlush(objectNode.toString());

    }

    /**
     * 发送文件给某个好友
     * @param path
     * @param name
     */
    public static void sendFile(String path,String name){

        FileInputStream fileInputStream = null;
        ObjectNode objectNode = JsonUtil.getObjectNode();
        try {
            //读文件到byte数组中，然后封装起来发送到服务端
            fileInputStream = new FileInputStream(path);
            int available = fileInputStream.available();
            byte[] bytes = new byte[available];
            fileInputStream.read(bytes);

            String file = Base64.encode(bytes,Base64.BASE64DEFAULTLENGTH);

            //获取文件名
            int index = path.lastIndexOf("\\");
            String fileName  = path.substring(index,path.length());

            //封装属性给json  类型为文件
            objectNode.put("msgType",MsgType.File);
            objectNode.put("fileName",fileName);
            objectNode.put("sender",SendService.username);
            objectNode.put("receiver",name);
            objectNode.put("file",file);


            System.out.println("文件序列化为"+file);
            System.out.println("发送消息："+objectNode.toString());
            //将json发送给服务器端
            channel.writeAndFlush(objectNode.toString());
            System.out.println("文件已发送！");
        } catch (FileNotFoundException e) {
            System.out.println("文件路径错误！找不到指定文件，请重新检查路径！");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 群发文件
     * @param path
     * @param allName
     */
    public static void sendGroupFile(String path,String allName){
        try {
            //读文件到byte数组中，然后封装起来发送到服务端
            FileInputStream fileInputStream = new FileInputStream(path);
            int available = fileInputStream.available();
            byte[] bytes = new byte[available];
            fileInputStream.read(bytes);

            //将byte数组序列化然后发送
            String file = Base64.encode(bytes,Base64.BASE64DEFAULTLENGTH);

            //获取文件名
            int index = path.lastIndexOf("\\");
            String fileName  = path.substring(index,path.length());

            String[] split = allName.split(",");
            for(String name:split) {
                //封装属性给json  类型为文件
                ObjectNode objectNode = JsonUtil.getObjectNode();
                objectNode.put("msgType", MsgType.File);
                objectNode.put("fileName", fileName);
                objectNode.put("sender", SendService.username);
                objectNode.put("receiver", name);
                objectNode.put("file", file);

                //将json发送给服务器端
                channel.writeAndFlush(objectNode.toString());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("文件已发送！");
        } catch (FileNotFoundException e) {
            System.out.println("文件路径错误！找不到指定文件，请重新检查路径！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
