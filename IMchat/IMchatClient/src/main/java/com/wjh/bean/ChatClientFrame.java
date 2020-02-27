package com.wjh.bean;

import com.wjh.constant.FrameSize;
import com.wjh.controller.ClientController;
import com.wjh.service.OnlineService;
import com.wjh.service.SendService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 打开聊天界面或者接受消息等会弹出此窗口
 */
public class ChatClientFrame extends JFrame {
    private JLabel sendPS;
    private JButton send,exit;//发送消息按钮
    private JTextArea chatWindow,sendWindow;//聊天窗口和发送窗口
    private JPanel chatPanel,sendPanel;//为窗口添加若干功能
    private static final int width = FrameSize.WIDTH;
    private static final int height = FrameSize.HEIGHT;
    private static String name;
    public ChatClientFrame(String name){
        super("RHR---与"+name+"的对话");
        this.name = name;
        init();
        addComponent();
        addListener();
        showFrame();
    }
    private void init(){
        chatPanel = new JPanel();
        sendPanel = new JPanel();
        chatWindow = new JTextArea(30,36);
        sendWindow = new JTextArea(5,36);
        sendWindow.setWrapStyleWord(true);
        sendWindow.setLineWrap(true);
        sendWindow.setAutoscrolls(true);
        chatWindow.setWrapStyleWord(true);
        chatWindow.setLineWrap(true);
        chatWindow.setAutoscrolls(true);
        chatWindow.setEditable(false);
        sendPS = new JLabel("发送窗口：");
        send = new JButton("发送");
        exit = new JButton("退出");
    }
    private void addComponent(){
        this.setLayout(null);
        this.add(chatWindow);
        this.add(sendWindow);
        this.add(chatPanel);
        this.add(sendPanel);
        this.add(sendPS);
        this.add(send);
        this.add(exit);
        //设置组件的位置和大小
        chatPanel.setBounds(0,20,500,300);
        sendPanel.setBounds(0,350,500,150);
        sendPS.setBounds(0,320,300,30);
        chatWindow.setFont(new Font("DiaLog",0,15));
        sendWindow.setFont(new Font("DiaLog",0,15));
        chatPanel.add(new JScrollPane(chatWindow));
        sendPanel.add(new JScrollPane(sendWindow));
        send.setBounds(420,510,80,40);
        exit.setBounds(20,510,80,40);

        //更改聊天界面UI风格
        try {
            UIManager.setLookAndFeel(
                    "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private synchronized void addListener(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //发送按钮的事件
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = sendWindow.getText();
                sendWindow.setText("");
                String name = ChatClientFrame.name;
                OnlineService.chatWithFriend(name,msg);
                //将自己发送的消息添加到聊天窗口上
                chatWindow.append(SendService.username+"：\n");
                chatWindow.append(msg+"\n");
            }
        });
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    private void showFrame(){
        int screen_height = (int)this.getToolkit().getScreenSize().getHeight();
        int screen_width = (int)this.getToolkit().getScreenSize().getWidth();
        this.setLocation((screen_width-width)/2,(screen_height-height)/2);
        this.setSize(width,height);
        this.setVisible(true);
    }
    public synchronized void showRecvMsg(String msg){
        chatWindow.append(name+"：\n");
        chatWindow.append(msg+"\n");
    }

//    public static void main(String[] args) {
//        ChatClientFrame chatClientFrame = new ChatClientFrame("wk");
//        chatClientFrame.showRecvMsg("你好");
//        chatClientFrame.showRecvMsg("我是某某某");
////        while (chatClientFrame.isDisplayable()){
////            int i = 1;
////        }
////        System.out.println(chatClientFrame);
//    }

}
