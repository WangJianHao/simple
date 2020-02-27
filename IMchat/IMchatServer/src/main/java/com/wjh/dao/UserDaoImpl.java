package com.wjh.dao;

import com.wjh.bean.User;
import com.wjh.util.C3p0Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDaoImpl implements UserDao {
    /**
     * 根据姓名获取user的具体信息
     * @param name
     * @return
     */

    public User getUserByName(String name) {
        String sql = "select * from user where username = ? ";
        User user = null;
        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,name);

            //执行语句
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                user = new User();
                user.setName(name);
                user.setId(Integer.parseInt(resultSet.getString("id")));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
//                System.out.println(resultSet.getString("email"));
            }
            C3p0Util.close(connection,preparedStatement,resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * 根据用户名和密码查询用户表
     * @param username
     * @param password
     * @return
     */
    public boolean login(String username,String password,String ip_address){
        String sql = "select * from user where username = ? and password = ?";
        String online = "update user set status = 1 where username = ? and password = ?";
        String updateIP = "update user set ip_address = ? where username = ? and password = ?";
        boolean result = false;
        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            //执行语句
            ResultSet resultSet = preparedStatement.executeQuery();
            result = resultSet.next();
            if (result){
                preparedStatement = connection.prepareStatement(online);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                preparedStatement.executeUpdate();
                preparedStatement = connection.prepareStatement(updateIP);
                preparedStatement.setString(1, ip_address);
                preparedStatement.setString(2, username);
                preparedStatement.setString(3, password);
                preparedStatement.executeUpdate();
            }

            C3p0Util.close(connection,preparedStatement,resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 插入新的用户
     * @param username
     * @param password
     * @return
     */
    public boolean register(String username,String password,String email){
        String sql = "insert into user(username,password,email,status) values(?,?,?,0)";
        boolean result = false;
        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,username);
            preparedStatement.setString(2,password);
            preparedStatement.setString(3,email);

            //执行语句
            int i = preparedStatement.executeUpdate();
            if(i!=0) result = true;
            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
           return false;
        }
        return result;
    }


    /**
     * 更改用户密码
     * @param username
     * @param password
     * @param newPassword
     * @return
     */
    public boolean changePassword(String username,String password,String newPassword){
        String sql = "update user set password = ? where username =  ? and password = ?";
        boolean result = false;
        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,newPassword);
            preparedStatement.setString(2,username);
            preparedStatement.setString(3,password);

            //执行语句
            int i = preparedStatement.executeUpdate();
            if(i!=0) result = true;
            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
            return false;
        }
        return result;
    }

    /**
     * 查询好友列表
     * @param name
     * @return
     */
    public String searchFriend(String name){
        String sql = "select * from user";
        StringBuffer stringBuffer = new StringBuffer();
        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            //执行语句
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                String username = resultSet.getString("username");
                if(!username.equals(name)) {
                    if(resultSet.getBoolean("status")){
                        stringBuffer.append(username+"(在线),");
                    }else{
                        stringBuffer.append(username+"(不在线),");
                    }

                }
            }
            C3p0Util.close(connection,preparedStatement,resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    /**
     * 查看对应用户是否在线
     * @param username
     * @return
     */
    public  boolean isOnline(String username){
        String sql = "select status from user where username = ? ";
        Connection connection = C3p0Util.createConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,username);
            //执行sql语句
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getBoolean("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            C3p0Util.close(connection,preparedStatement,resultSet);
        }
        //走到这只能说明没有进入resultSet
        return false;
    }

    public String getIP(String username){
        String sql = "select ip_address from user where username = ? ";
        Connection connection = C3p0Util.createConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,username);
            //执行sql语句
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString("ip_address");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            C3p0Util.close(connection,preparedStatement,resultSet);
        }
        return null;
    }


    /**
     * 下线操作
     */
    public void offline(String ip_address){
        String sql = "update user set status = 0  where ip_address = ? ";
        String sql1 = "update user set ip_address = null where ip_address = ?";

        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,ip_address);
            //执行语句
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(sql1);
            preparedStatement.setString(1,ip_address);
            preparedStatement.executeUpdate();


            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取当前在线的所有好友
     * @return
     */
    public String getAllOnlineFriends(){
        StringBuffer onlineFriends = new StringBuffer();
        String sql = "select username from user where status = ? ";

        Connection connection = C3p0Util.createConnection();
        try {
            //创建preparedStatement对象防止简单的数据库注入异常
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,1);
            //执行语句
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                onlineFriends.append(resultSet.getString("username")+",");
            }
            //关闭资源
            C3p0Util.close(connection,preparedStatement,resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return onlineFriends.toString();
    }

    /**
     * 查看能不能找到对应的行，如果找到为true，找不到为false
     * @param username
     * @param email
     * @return
     */
    public String forgetPassword(String username,String email){
        String sql = "select * from user where username = ? and email = ?";
        Connection connection = C3p0Util.createConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,username);
            preparedStatement.setString(2,email);
            //执行sql语句
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            C3p0Util.close(connection,preparedStatement,resultSet);
        }
        //走到这只能说明没有进入resultSet
        return null;
    }



//    public static void main(String[] args) {
//        UserDaoImpl userDao = new UserDaoImpl();
//        String flag = userDao.forgetPassword("wjh","495586804@qq.com");
//        System.out.println(flag);
////        String wjh = userDao.getIP("wjh");
////        System.out.println(wjh);
//    }
}
