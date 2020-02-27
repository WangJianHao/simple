package com.wjh.util;


import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class EmailUtil {
    /**
     * 发送密码给对应的邮件
     * @param toEmail   邮箱地址
     * @param msg      邮件信息
     * @throws EmailException
     */
    public static void toEmail(String toEmail,String msg) throws EmailException {
        SimpleEmail email = new SimpleEmail();
        email.setHostName("SMTP.qq.com");//邮件服务器

        //授权码：wvdyijdfglykbhhh
        email.setAuthentication("495586804@qq.com","zexelyjfrxcpbigb");//邮件登录用户名及授权码
        email.setSSLOnConnect(true);//ssl加密
        email.setFrom("495586804@qq.com","IMchat");//发送方邮箱/发送方名称
        email.setSubject("用户忘记密码邮件");//主题名称
        email.setCharset("UTF-8");//设置字符集编码
        email.setMsg(msg);//发送内容
        email.addTo(toEmail);//接收方邮箱
        email.send();//发送方法

    }

//    public static void main(String[] args) {
//        try {
//            toEmail("751878241@qq.com","123456");
//        } catch (EmailException e) {
//            e.printStackTrace();
//        }
//    }
}
