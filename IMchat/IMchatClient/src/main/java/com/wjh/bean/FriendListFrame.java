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
 * 好友列表的面板
 */
public class FriendListFrame extends JFrame {
    private JCheckBox[] jCheckBoxes;
    private JButton logout,chatNow,sendFile;//注销用户和聊天的按钮
    private JLabel self,friend;
    private static final int width = FrameSize.FRIEND_WIDTH;
    private static final int height = FrameSize.FRIEND_HEIGHT;
    public FriendListFrame(){
        super(SendService.username+"的好友列表");
        init();
        addComponent();
        addListener();
        showFrame();
    }
    //有新的好友要刷新  另外处理
    //好友上线要刷新 好友有消息也要刷新
    public void initFriendList(){
        String[] friendList = ClientController.friendList;
        for(int i=0;i<friendList.length;i++){
            jCheckBoxes[i].setText(friendList[i]);
        }
    }
    private void init(){
        self = new JLabel("自己："+SendService.username);
        friend = new JLabel("好友列表：");
        logout = new JButton("注销");
        chatNow = new JButton("聊天");
        sendFile = new JButton("发送文件");
        String[] friendList = ClientController.friendList;
        jCheckBoxes = new JCheckBox[friendList.length];
        for(int i =0;i<friendList.length;i++){
            jCheckBoxes[i] = new JCheckBox(friendList[i]);
        }
    }
    private void addComponent(){
        this.setLayout(null);
        this.add(self);
        this.add(friend);
        this.add(logout);
        this.add(chatNow);
        this.add(sendFile);
        Font diaLog = new Font("DiaLog", 0, 15);
        self.setBounds(20,30,200,50);
        self.setFont(diaLog);
        friend.setBounds(20,80,200,50);
        friend.setFont(diaLog);
        logout.setBounds(20,600,100,50);
        chatNow.setBounds(240,600,100,50);
        sendFile.setBounds(130,600,100,50);
        for(int i=0;i<jCheckBoxes.length;i++){
            this.add(jCheckBoxes[i]);
            jCheckBoxes[i].setBounds(20,130+i*50,300,50);
            jCheckBoxes[i].setFont(diaLog);
        }
    }
    private void addListener(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new SendService(OnlineService.channel).send();
            }
        });
        chatNow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int length = jCheckBoxes.length;
                boolean flag = false;
                for(int i = 0;i<length;i++){
                    if(jCheckBoxes[i].isSelected()) {
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    JOptionPane.showMessageDialog(null,
                            "未选择好友");
                }else {
                    flag = true;//true代表单聊  false代表群聊
                    int goal = 0;
                    for (int i = 0, time = 0; i < length; i++) {
                        if (jCheckBoxes[i].isSelected()) {
                            flag = true;
                            goal = i;
                            time++;
                            if (time > 1) {
                                flag = false;
                                break;
                            }
                        }
                    }
                    if (flag) {
                        //单聊
                        int i = ClientController.friendList[goal].indexOf("(");
                        String name = ClientController.friendList[goal].substring(0, i);
                        ChatClientFrame chatClientFrame = new ChatClientFrame(name);
                        ClientController.frames.put(name,chatClientFrame);
                    } else {
                        //群聊
                        String allName = "";
                        for (int i = 0; i < length; i++) {
                            if (jCheckBoxes[i].isSelected()) {
                                int index = ClientController.friendList[i].indexOf("(");
                                String name = ClientController.friendList[i].substring(0, index);
                              allName += name + ",";
                            }
                        }
                        allName += SendService.username;
                        GroupChatFrame groupChatFrame = new GroupChatFrame(allName);
                        ClientController.groups.put(allName,groupChatFrame);
                    }
                }

            }
        });
        sendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int length = jCheckBoxes.length;
                boolean flag = false;
                for(int i = 0;i<length;i++){
                    if(jCheckBoxes[i].isSelected()) {
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    JOptionPane.showMessageDialog(null,
                            "未选择好友");
                }else {
                    flag = true;//true代表单聊  false代表群聊
                    int goal = 0;
                    for (int i = 0, time = 0; i < length; i++) {
                        if (jCheckBoxes[i].isSelected()) {
                            flag = true;
                            goal = i;
                            time++;
                            if (time > 1) {
                                flag = false;
                                break;
                            }
                        }
                    }
                    if (flag) {
                        //单聊
                        int i = ClientController.friendList[goal].indexOf("(");
                        String name = ClientController.friendList[goal].substring(0, i);
                        new SendFileFrame(name);
                    } else {
                        //群聊
                        String allName = "";
                        for (int i = 0; i < length; i++) {
                            if (jCheckBoxes[i].isSelected()) {
                                int index = ClientController.friendList[i].indexOf("(");
                                String name = ClientController.friendList[i].substring(0, index);
                                allName += name + ",";
                            }
                        }
                        new SendFileFrame(allName);
                    }
                }

            }
        });

    }
    private void showFrame(){
        int screen_height = (int)this.getToolkit().getScreenSize().getHeight();
        int screen_width = (int)this.getToolkit().getScreenSize().getWidth();
        this.setLocation((screen_width-width),(screen_height-height)/6);
        this.setSize(width,height);
        this.setVisible(true);
    }

//    public static void main(String[] args) {
//        String[] friendList = {"wjh(在线)","test(不在线)"};
//        new FriendListFrame(friendList);
//    }
}
