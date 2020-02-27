# 基于netty的聊天系统设计文档

## 1、项目背景

聊天软件：即时通信软件（IM系统），极大的方便了所有人的交流，俨然成为了一种必不可少的交流沟通方式。 那么你是否能知道即时通信软件背后的实现技术奥妙呢？基于此我们也来设计实现一套类QQ、微信的聊天系统。来探究一下这类软件背后实现的真实流程。

## 2、项目概括

单聊、群聊功能

## 2.1、功能点介绍

主要开发的功能模块：用户账号模块、聊天模块、文件传输模块
用户端

### 1、用户模块
- 1.1、用户登录模块  
- 1.2、用户的注册模块
- 1.3、忘记密码
- 1.4、修改密码
### 2、聊天模块
- 2.1、单聊（一对一聊天） 
- 2.2、群聊
- 2.3、好友列表
- 2.4、好友上下线通知
### 3、文件传输
- 3.1、指定用户发送文件
- 3.2、群发文件
 服务端

服务端要完成客户端的交互，数据落地（存储：MySQL），通信（netty）、TCP封装，抽象，高并发

服务端按照分层架构的思想划分成三层：包含网络链接层，业务处理层，数据落地层
网络两阶层：
主要基于netty封装的方法的熟练使用，服务端和客户端所有内容交互都需要通过该层传输
业务处理层：
该层基于网络层的数据做具体业务处理
数据落地层：
这里主要是存储相关数据，包括用户信息、离线信息，文件内容等。
2.2、约束定义

channel
对服务端而言，所有业务内容交互都需要通过网络链接层，那么网络链接层统一获取数据他有怎么能区分当前数据到底是干什么的呢？
这个时候就需要考虑数据如何做到区分，
该系统的处理所有数据以JSON格式传输，在JSON格式中添加专有字段MsgType来做消息内容区分，
客户端和服务端中的发送方首先填充MsgType字段，明确当前消息的意义，接收方接收消息后首先根据解析该字段来明确消息具体干什么的，
然后在做相应转发处理 

enum EnMsgType{
       EN_MSG_LOGIN, //用户登录消息
       EN_MSG_REGISTER, //用户注册消息
       EN_MSG_FORGET_PWD, //用户忘记密码消息
       EN_MSG_MODIFY_PWD, //修改密码休息
       EN_MSG_CHAT, //一对一聊天消息
       EN_MSG_CHAT_ALL, //群聊消息
       EN_MSG_NOTIFY_ONLINE, //群发用户上线消息
       EN_MSG_NOTIFY_OFFLINE, //群发用户下线消息
       EN_MSG_OFFLINE, //用户下线消息
       EN_MSG_GET_ALL_USERS, //获取所有在线用户信息
       EN_MSG_TRANSFER_FILE, //传输文件消息
       EN_MSG_CHECK_USER_EXIST, //用户是否存在【新增】
       EN_MSG_OFFLINE_MSG_EXIST, //是否存在离线消息【新增】
       EN_MSG_OFFLINE_FILE_EXIST, //是否存在离线文件【新增】
       EN_MSG_ACK //响应消息
}
## 3、项目详设

### 3.1、数据库表设计

   `确定表及表中的属性，类型等
   用户表（user）
   ID、用户名、密码、邮箱
    用户聊天表（chat）`
    
### 3.2、功能点详设

    1、用户模块
    1.1、用户登录模块  
         输入用户名，密码（客户端）-》服务端 -》匹配数据库 -》判断是否成功登录（封装返回消息：状态码） -》用户端  
    1.2、用户的注册模块
         【用户端】输入注册信息，校验信息合法性 -》【服务端】-》用户信息是否合法-》用户信息入库：用户名，密码、邮箱 -》【用户端】成功或失败
    1.3、忘记密码
        【用户端】输入用户名和邮箱 -》【服务端】-》检测用户名和邮箱合法 -》通过邮箱将密码等信息发送给用户邮箱
    1.4、修改密码
        【用户端】进入到登录成功部分-》通过账号、原始密码、新密码-》【服务器】通过用户名和原始密码判断合法-》修改数据库用户的密码
    2、聊天模块
    2.1、单聊（一对一聊天）
         A向B发送消息：A的账号信息、B的账号信息、 消息体、消息类型
         发送发-》服务端（消息转发（存在已知的用户信息，用户信息找到IP（Channel）））-》接收端
         
         接收端是否在线：
         接收端在线：服务端获取到消息直接转发到接收端
         接收端不在线：
         发送端A（发送方ID、接收方的ID、消息内容） 
         -》服务器（接收方不在线）消息存储-》接收方B上线消息发送（发送方ID、消息内容）
         -》接收方B（发送方ID、消息内容）
         
         消息：发送端ID，接收端ID，消息内容，时间（保证消息顺序性）、状态位（标识是否成功）
    2.2、群聊
        所有的好友发送(考虑好友是否在线问题？)
    2.3、好友列表
        将所有在线的用户发送给用户
         用户是否在线（如何来处理用户在线？？？）
    2.4、好友上下线通知
        将用户登录/用户下线消息转发给在线的好友
    3、文件传输
    3.1、指定用户发送文件
    【发送方】：指定接收方用户名，指定本地的文件位置 -》【服务端】：接收方用户名找到channel，将文件流转发给接收端（接收端是否在线）
    3.2、群发文件
3.3、技术点

1、基于netty网络框架：
对于聊天这种并发量高，业务逻辑简单的应用场景使用NIO模型
netty是基于NIO实现的成熟的网络通信模型  

 BIO模型：并发量固定，业务复杂  
NIO模型：高并发，业务简单  应用场景：聊天系统
AIO模型：高并发，业务逻辑复杂   

2、消息处理
消息类型区分：增加标识字段
通过JSON进行数据的字段的区分

XML形式：
login：
<login>
   <name>张三</name>
   <passwd>1234</passwd>
</login>

JSON形式：
{
  login:{
     name:"张三"，
     passwd:1234
  }
}

3、邮箱发送
 邮件发送：
 配置+代码开发
 
 4、数据库 MySQL
 JDBC -》连接池：c3p0,druid、dbcp...
netty通信框架搭建 父子工程介绍 netty开发步骤 项目目录结构

搭建系统 两个项目 服务端：chatServer 客户端：chatClient

项目的目录结构 |src |-main //项目源代码 |--java //java源代码 |---com |----tulun |-----controller //接收请求（web页面首先请求到controller层） |-----service //业务逻辑处理（接口） |------impl //业务逻辑处理（实现类） |-----dao //数据库操作（接口） |------impl //数据库操作（实现类） |-----bean //自己定义的基础类型 |-----constant //常量、枚举等类型类 |-----util //工具类 |--resources //配置信息存放 |--[webapp] //前端页面存放 |-test //测试用例代码

Netty聊天系统03 基础配置 1、数据库建搭 2、JSON数据配置

需求：通过用户名来查询用户的信息 【客户端】用户名-》【服务单】数据库查询用户的基本 -》【客户端】信息价交给客户端

前提：安装MySQL数据库 使用c3p0连接池来连接数据

1、添加依赖 数据的驱动连接，c3p0配置 mysql mysql-connector-java 5.1.30 c3p0 c3p0 0.9.1.1

2、c3p0的配置文件（c3p0-config.xml配置）

com.mysql.jdbc.Driver jdbc:mysql://localhost:3306/chat root 123456 10 30 100 10
3、创建数据（user） 4、配置连接池的工具类 public class C3p0Util { private static ComboPooledDataSource dataSource = new ComboPooledDataSource("mysql");

static Connection cn = null;

static {
    if (cn == null) {
        try {
            cn = dataSource.getConnection();
        } catch (SQLException e) {
            System.out.println("读取MySQL配置异常");
            e.printStackTrace();
        }
    }
}

/**
 * 获取连接
 *
 * @return
 */
public static Connection getConnection() {
    return cn;
}

//关闭连接
public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet) {
    try {
        if (connection != null) {
            connection.close();
        }
        if (statement != null) {
            statement.close();
        }
        if (resultSet != null) {
            resultSet.close();
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }


}
}

JSON在Maven中的配置 com.fasterxml.jackson.core jackson-databind 2.9.3

/**

JSON工具类 / public class JsonUtils { /*

从json字符串中解析ObjectNode

@param json

@return */ public static ObjectNode getObjectNode(String json) { // TODO Auto-generated method stub ObjectMapper jsonMapper = new ObjectMapper(); ObjectNode objectNode = null; try { objectNode = jsonMapper.readValue(json, ObjectNode.class); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }

return objectNode; }

/**

创建一个新的objectNode，用于封装json字符串
@return */ public static ObjectNode getObjectNode(){ ObjectMapper jsonMapper = new ObjectMapper(); return jsonMapper.createObjectNode(); } }