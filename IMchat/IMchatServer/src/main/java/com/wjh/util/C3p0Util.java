package com.wjh.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class C3p0Util {
    private static ComboPooledDataSource dataSource =
            new  ComboPooledDataSource("mysql");

    private static Connection connection = null;

    static {

            try {
                if(connection == null ){
                    connection = dataSource.getConnection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

    }

    /**
     * 获取连接
     * @return
     */
    public static Connection createConnection(){
        try {
            if(connection.isClosed()){
                return dataSource.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void close(Connection connection,PreparedStatement preparedStatement,ResultSet resultSet){

        try {
            if(connection != null){
                connection.close();
            }
            if(preparedStatement != null){
                preparedStatement.close();
            }
            if(resultSet != null){
                resultSet.close();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

    }

//    public static void main(String[] args) {
//        Connection connection = C3p0Util.createConnection();
//        String sql = "select * from user where id = ?";
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setInt(1,1);
//            ResultSet resultSet = preparedStatement.executeQuery();
//            while (resultSet.next()){
//                System.out.println(resultSet.getString("username"));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

}
