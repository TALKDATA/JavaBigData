## 第十一章：mysql、Hive安装与集成
date: 2019-1-22 22:30:01


### 为什么要用mysql?
一方面，本项目用来存储Hive的元数据；另一方面，可以把离线分析结果放入mysql中；

### 安装mysql
通过yum在线mysql，具体操作命令如下所示(关于yum源可以修改为阿里的，比较快和稳定)
```
1、在线安装mysql
通过yum在线mysql，具体操作命令如下所示。
yum clean all
yum install mysql-server
2、mysql 服务启动并测试
sudo chown -R kfk:kfk /usr/bin/mysql    修改权限给kfk
1）查看mysql服务状态
sudo service mysqld status  
2）启动mysql服务
sudo service mysqld start
3）设置mysql密码
/usr/bin/mysqladmin -u root password '123456'
4）连接mysql
mysql –uroot -p123456
a）查看数据库
show databases;
mysql
test
b）查看数据库
use test;
c）查看表列表
show tables;
```
出现问题，大多数是权限问题，利用sudo执行或者重启mysql.

### 安装Hive
Hive在本项目中功能是，将hbase中的数据进行离线分析，输出处理结果，可以到mysql或者hbase，然后进行可视化。
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzfpw9k0v7j30kv09rtfp.jpg)
这里版本采用的是：apache-hive-2.1.0-bin.tar.gz
（之前用apache-hive-0.13.1-bin.tar.gz出现和hbase集成失败，原因很奇怪，下一章详细讲）。
1、解压
```
步骤都老生常谈了。。。
tar -zxf apache-hive-2.1.0-bin.tar.gz -C /opt/modules/
mv  apache-hive-2.1.0-bin hive-2.1.0     //重命名
```
2、修改配置文件
```
1）hive-log4j.properties
#日志目录需要提前创建
hive.log.dir=/opt/modules/hive-2.1.0/logs
2）修改hive-env.sh配置文件
HADOOP_HOME=/opt/modules/hadoop-2.6.0
HBASE_HOME=/opt/modules/hbase-1.0.0-cdh5.4.0
# Hive Configuration Directory can be controlled by:
export HIVE_CONF_DIR=/opt/modules/hive-2.1.0/conf
```
3、启动进行测试
首先启动HDFS，然后创建Hive的目录
bin/hdfs dfs -mkdir -p /tmp
bin/hdfs dfs -chmod g+w /tmp
bin/hdfs dfs -mkdir -p /user/hive/warehouse
bin/hdfs dfs -chmod g+w /user/hive/warehouse
4、测试
```
./hive
#查看数据库
show databases;
#使用默认数据库
use default;
#查看表
show tables;

```
### Hive与mysql集成
利用mysql放Hive的元数据。
1、在/opt/modules/hive-2.1.0/conf目录下创建hive-site.xml文件，配置mysql元数据库。
```
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>


<configuration>
  <property>
    <name>javax.jdo.option.ConnectionURL</name>
    <value>jdbc:mysql://bigdata-pro01.kfk.com/metastore?createDatabaseIfNotExist=true</value>
  </property>
  <property>
    <name>javax.jdo.option.ConnectionDriverName</name>
    <value>com.mysql.jdbc.Driver</value>
  </property>
 <property>
    <name>javax.jdo.option.ConnectionUserName</name>
    <value>root</value>
  </property>
  <property>
    <name>javax.jdo.option.ConnectionPassword</name>
    <value>123456</value>
  </property>
  <property>
    <name>hbase.zookeeper.quorum</name>   
	<value>bigdata-pro01.kfk.com,bigdata-pro02.kfk.com,bigdata-pro03.kfk.com</value>
  </property>


</configuration>
```
2、设置用户连接信息

1）查看用户信息
mysql -uroot -p123456
show databases;
use mysql;
show tables;
select User,Host,Password from user;
2）更新用户信息
update user set Host='%' where User = 'root' and Host='localhost'
3）删除用户信息
delete from user where user='root' and host='127.0.0.1'
select User,Host,Password from user;
delete from user where host='localhost';
删除到只剩图中这一行数据
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzfqckmjxej30ej031q2s.jpg)
4）刷新信息
flush privileges;
3.拷贝mysql驱动包到hive的lib目录下
cp  mysql-connector-java-5.1.35.jar /opt/modules/hive-2.1.0/lib/
4.保证第三台集群到其他节点无秘钥登录

### Hive与mysql测试
1.启动HDFS和YARN服务
2.启动hive
./hive
3.通过hive服务创建表
CREATE TABLE stu(id INT,name STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' ;
4.创建数据文件
vi /opt/datas/stu.txt
00001	zhangsan
00002	lisi
00003	wangwu
00004	zhaoliu
5.加载数据到hive表中
load data local inpath '/opt/datas/stu.txt' into table stu;
直接在hive查看表中内容就ok。
在mysql数据库中hive的metastore元数据。（元数据是啥，去看看hive介绍吧）
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzfqeibkrtj306103ta9v.jpg)