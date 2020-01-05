## 第七章：Kafka简介和分布式部署
date: 2019-1-1 21:13:01


### Kafka简介
Kafka是一个分布式的消息系统，使用Scala编写，可水平扩展和高吞吐率而被广泛使用。
目前越来越多的开源分布式处理系统如Cloudera、Apache Storm、Spark都支持与Kafka集成。
![](http://ww1.sinaimg.cn/large/005BOtkIly1fyrd3u03srj30ja09k791.jpg)
图中名词介绍参考这边：https://www.cnblogs.com/hei12138/p/7805475.html
**理解zookeeper在其中的作用，简单了解生产和消费的之间的联系。**
官网下载：http://kafka.apache.org/
这里采用：kafka_2.10-0.9.0.0.tgz

### Kafka分布式部署
1、解压
tar -zxf kafka_2.10-0.9.0.0.tgz  -C /opt/modules/
2、配置server.properties文件
```propertis
#节点唯一标识
broker.id=1

listeners=PLAINTEXT://bigdata-pro01.kfk.com:9092
#默认端口号
port=9092
#主机名绑定
host.name=bigdata-pro01.kfk.com
#Kafka数据目录
log.dirs=/opt/modules/kafka_2.10-0.9.0.0/kafka-logs
#配置Zookeeper
zookeeper.connect=bigdata-pro01.kfk.com:2181,bigdata-pro02.kfk.com:2181,bigdata-pro03.kfk.com:2181

```
3、配置zookeeper.properties文件
```propertis
#Zookeeper的数据存储路径与Zookeeper集群配置保持一致
dataDir=/opt/modules/zookeeper-3.4.5-cdh5.10.0/zkData

```

4、配置consumer.properties文件
```
#配置Zookeeper地址
zookeeper.connect=bigdata-pro01.kfk.com:2181,bigdata-pro02.kfk.com:2181,bigdata-pro03.kfk.com:2181
```
5、配置producer.properties文件
```
#配置Kafka集群地址  ,分布在三台机器上
metadata.broker.list=bigdata-pro01.kfk.com:9092,bigdata-pro02.kfk.com:9092,bigdata-pro03.kfk.com:9092
```
6、拷贝
scp -r kafka_2.10-0.9.0.0 bigdata-pro02.kfk.com:/opt/modules/
scp -r kafka_2.10-0.9.0.0 bigdata-pro03.kfk.com:/opt/modules/
7、修改另外两个节点的server.properties
```
#bigdata-pro02.kfk.com节点
broker.id=2
listeners=PLAINTEXT://bigdata-pro02.kfk.com:9092
host.name=bigdata-pro02.kfk.com
#bigdata-pro03.kfk.com节点
broker.id=3
listeners=PLAINTEXT://bigdata-pro03.kfk.com:9092
host.name=bigdata-pro03.kfk.com

```

### kafka测试
```
1、所有节点启动zk
bin/zkServer.sh start
2、各个节点启动Kafka集群
bin/kafka-server-start.sh config/server.properties &
3、创建topic
bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic test --replication-factor 1 --partitions 1
4、查看topic
bin/kafka-topics.sh --zookeeper localhost:2181 –list

bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic test
结果：
        Topic:test      PartitionCount:1        ReplicationFactor:1     Configs:
        Topic: test     Partition: 0    Leader: 2       Replicas: 2     Isr: 2
5、生产者生产数据（节点1）
bin/kafka-console-producer.sh --broker-list bigdata-pro01.kfk.com:9092 --topic test
6、消费者消费数据（节点2）
bin/kafka-console-consumer.sh --zookeeper bigdata-pro02.kfk.com:2181 --topic test --from-beginning
```
额外说下分区和消费关系：（有点不好理解，问题不大）
一个主题可以有多个分区，具体分区方法有多种；关于消费，有消费组的概念。一种是指定消费组（每个消费者的组名一致），那么每个分区对应一个消费者；二是指定消费组（每个消费者的组名不一致），那么所有分区每个消息都会送至各个小组的消费者；三是不指定消费组，那么每条消息会发给消费组中一个消费者。