package com.wjh.dao;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wjh.constant.MsgType;
import com.wjh.util.C3p0Util;
import com.wjh.util.JsonUtil;
import sun.dc.pr.PRError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MessageDaoImpl implements MessageDao {
    /**
     * 插入单聊消息记录
     * @param sender
     * @param receiver
     * @param msg
     * @param status
     */
    @Override
    public void insertRecord(String sender,String receiver,String msg,int status) {
        String sql = "insert into message values(?,?,?,?, null )";

        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,sender);
            preparedStatement.setString(2,receiver);
            preparedStatement.setString(3,msg);
            preparedStatement.setInt(4,status);
            preparedStatement.executeUpdate();

            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取单聊消息记录
     * @param username
     * @return
     */
    @Override
    public ArrayList<String> getMessage(String username) {
        ArrayList<String> messages = new ArrayList<>();
        String sql = "select * from message where receiver = ? and status = 0 and allName is null";
        String sql1 ="update message set status = 1 where receiver = ? and allName is null ";

        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,username);
            ResultSet resultSet = preparedStatement.executeQuery();
            //获取所有的离线消息
            while (resultSet.next()){
                messages.add(resultSet.getString("sender")+":"
                        +resultSet.getString("message"));
            }
            preparedStatement = connection.prepareStatement(sql1);
            preparedStatement.setString(1,username);
            preparedStatement.executeUpdate();

            //关闭资源
            C3p0Util.close(connection,preparedStatement,resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }


    public ArrayList<ObjectNode> getGroupMessage(String name){
        ArrayList<ObjectNode> messages = new ArrayList<>();
        String sql = "select * from message where receiver = ? and status = 0 ";
        String sql1 ="update message set status = 1 where receiver = ?";

        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,name);
            ResultSet resultSet = preparedStatement.executeQuery();
            //获取所有的离线消息
            while (resultSet.next()){
                ObjectNode jsonNodes = JsonUtil.getObjectNode();
                jsonNodes.put("msgType", MsgType.GROUPTEXT_ACK);
                jsonNodes.put("sender", resultSet.getString("sender"));
                jsonNodes.put("receiver", resultSet.getString("receiver"));
                jsonNodes.put("allName",resultSet.getString("allName"));
                jsonNodes.put("msg", resultSet.getString("message"));
                messages.add(jsonNodes);
            }

            preparedStatement = connection.prepareStatement(sql1);
            preparedStatement.setString(1,name);
            preparedStatement.executeUpdate();
            //关闭资源
            C3p0Util.close(connection,preparedStatement,resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 插入群聊消息记录
     * @param sender
     * @param receiver
     * @param msg
     * @param status
     * @param allName
     */
    @Override
    public void insertGroupRecord(String sender, String receiver, String msg, int status, String allName) {
        String sql = "insert into message values(?,?,?,?,?)";

        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,sender);
            preparedStatement.setString(2,receiver);
            preparedStatement.setString(3,msg);
            preparedStatement.setInt(4,status);
            preparedStatement.setString(5,allName);
            preparedStatement.executeUpdate();

            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


//
//    public static void main(String[] args) {
//        MessageDaoImpl messageDao = new MessageDaoImpl();
//        messageDao.insertRecord("wjh","wk","54",1);
//    }
}
