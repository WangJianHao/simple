package com.wjh.bean;

import com.wjh.constant.FrameSize;
import com.wjh.service.OnlineService;
import com.wjh.service.SendService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class SendFileFrame extends JFrame {
    private JButton send,exit;
    private JLabel ps;
    private static final int width = 400;
    private static final int height = 400;
    private TextArea filePath;//文件路径
    private String name; //只能发送在线文件
    public SendFileFrame(String name){
        super("发送文件至"+name);
        this.name = name;
        init();
        addComponent();
        addListener();
        showFrame();
    }
    private void init(){
        ps = new JLabel("请输入要发送的文件的路径：");
        filePath = new TextArea("",10,30,TextArea.SCROLLBARS_VERTICAL_ONLY);
        send = new JButton("发送");
        exit = new JButton("退出");
    }
    private void addComponent(){
        this.setLayout(null);
        this.add(ps);
        this.add(filePath);
        this.add(send);
        this.add(exit);

        Font diaLog = new Font("DiaLog", 0, 15);
        ps.setBounds(20,40,200,40);
        ps.setFont(diaLog);
        filePath.setBounds(20,100,350,200);
        filePath.setFont(diaLog);
        send.setBounds(290,310,80,40);
        exit.setBounds(20,310,80,40);
    }
    private void addListener(){
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = filePath.getText();
                System.out.println("文件路径为："+path);
                if(name.contains(",")){//代表不止一个人，是群发
                    OnlineService.sendGroupFile(path,name);
                }else{
                    OnlineService.sendFile(path,name);
                }
               JOptionPane.showMessageDialog(null,"发送成功！");
//                //先读出来
//                try {
//                    FileInputStream fileInputStream = new FileInputStream(path);
//                    int available = fileInputStream.available();
//                    byte[] bytes = new byte[available];
//                    fileInputStream.read(bytes);
//                    //还需要获取文件名
//                    int index = path.lastIndexOf("\\");
//                    String fileName  = path.substring(index,path.length());
//
//                    File file = new File(FrameSize.DEFAULT_FILE_PATH+fileName);
//                    file.createNewFile();
//                    FileOutputStream fileOutputStream = new FileOutputStream(
//                            FrameSize.DEFAULT_FILE_PATH+fileName);
//                    fileOutputStream.write(bytes);
//                } catch (FileNotFoundException e1) {
//                    e1.printStackTrace();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
            }
        });
    }
    private void showFrame(){
        int screen_width = (int)this.getToolkit().getScreenSize().getWidth();
        int screen_height = (int)this.getToolkit().getScreenSize().getHeight();
        this.setSize(width,height);
        this.setLocation((screen_width - width)/2,(screen_height - height)/2);
        this.setVisible(true);
    }

//    public static void main(String[] args) {
//        new SendFileFrame("wjh");
//    }
}
