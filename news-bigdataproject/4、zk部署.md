## 第四章：Zookeeper分布式集群部署
date: 2018-12-29 14:52:11


### ZooKeeper简介
ZooKeeper 是一个针对大型分布式系统的可靠协调系统；它提供的功能包括：配置维护、名字服务、分布式同步、组服务等； 它的目标就是封装好复杂易出错的关键服务，将简单易用的接口和性能高效、功能稳定的系统提供给用户； ZooKeeper 已经成为 Hadoop 生态系统中的基础组件。
Zookeeper可以选择Apache版本，也可以选择Cloudera版本。
1）下载Apache版本的Zookeeper。
2）下载Cloudera版本的Zookeeper。

### ZooKeeper部署步骤
**1.下载Zookeeper**
这里选择cdh版本的zookeeper-3.4.5-cdh5.10.0.tar.gz，将下载好的安装包上传至bigdata-pro01.kfk.com节点的/opt/softwares目录下。
**2.解压Zookeeper**
tar -zxf zookeeper-3.4.5-cdh5.10.0.tar.gz -C /opt/modules/
**3.修改配置**
1）复制配置文件
cp conf/zoo_sample.cfg zoo.cfg
2）修改配置文件zoo.cfg
```xml
vi zoo.cfg
#这个时间是作为Zookeeper服务器之间或客户端与服务器之间维持心跳的时间间隔
tickTime=2000
#配置 Zookeeper 接受客户端初始化连接时最长能忍受多少个心跳时间间隔数。
initLimit=10
#Leader 与 Follower 之间发送消息，请求和应答时间长度
syncLimit=5
#数据目录需要提前创建
dataDir=/opt/modules/zookeeper-3.4.5-cdh5.10.0/zkData
#访问端口号
clientPort=2181
#server.每个节点服务编号=服务器ip地址：集群通信端口：选举端口
server.1=bigdata-pro01.kfk.com:2888:3888
server.2=bigdata-pro02.kfk.com:2888:3888
server.3=bigdata-pro03.kfk.com:2888:3888
```
**4.分发各个节点**
将Zookeeper安装配置分发到其他两个节点，具体操作如下所示：
scp -r zookeeper-3.4.5-cdh5.10.0/ bigdata-pro02.kfk.com:/opt/modules/
scp -r zookeeper-3.4.5-cdh5.10.0/ bigdata-pro03.kfk.com:/opt/modules/
**5.创建相关目录和文件**
1）在3个节点上分别创建数据目录
mkdir /opt/modules/zookeeper-3.4.5-cdh5.10.0/zkData
2）在各个节点的数据存储目录下创建myid文件，并且编辑每个机器的myid内容为
```java
#切换到数据目录
cd /opt/modules/zookeeper-3.4.5-cdh5.10.0/zkData
#bigdata-pro01.kfk.com节点
touch myid
vi myid
1
#bigdata-pro02.kfk.com节点
touch myid
vi myid
2
#bigdata-pro03.kfk.com节点
touch myid
vi myid
3
```
**6.启动Zookeeper服务**
1）各个节点使用如下命令启动Zookeeper服务
bin/zkServer.sh start
2）查看各个节点服务状态
bin/zkServer.sh status
不是follower
3）关闭各个节点服务
bin/zkServer.sh stop
4）查看Zookeeper目录树结构
bin/zkCli.sh

