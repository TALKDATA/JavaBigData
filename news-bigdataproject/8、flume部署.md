## 第八章：Flume简介和分布式部署
date: 2019-1-1 21:30:01


### Flume简介

Flume是Cloudera提供的一个高可用的，高可靠的，分布式的海量日志采集、聚合和传输的系统，Flume支持在日志系统中定制各类数据发送方，用于收集数据；同时，Flume提供对数据进行简单处理，并写到各种数据接受方（可定制）的能力。
![](http://ww1.sinaimg.cn/large/005BOtkIly1fyre9dzhbfj30m208g75x.jpg)
入门学习就看这篇就好：https://www.cnblogs.com/zhangyinhua/p/7803486.html#_label0

下载版本：下载Apache版本的apache-flume-1.7.0-bin.tar.gz 
**关于Flume在项目中的说明**
![](http://ww1.sinaimg.cn/large/005BOtkIly1fyrego248hj30go0gp43u.jpg)
我们这次项目中共采用三个Flume。
两个节点采集(节点2,3)，一个节点把采集的汇总并分发至kafka和hbase（节点1）.
### Flume部署
**（这里先部署和设置了节点2和3采集部分，节点1的汇总分发后面继续）**
每一步都可以去查官方资料：官方地址：http://flume.apache.org/

1、解压Flume
tar -zxf apache-flume-1.7.0-bin.tar.gz  -C /opt/modules/
```
vi flume-env.sh
配置下环境变量问题
export JAVA_HOME=/opt/modules/jdk1.8.0_191
export HADOOP_HOME=/opt/modules/hadoop-2.6.0
export HBASE_HOME=/opt/modules/hbase-1.0.0-cdh5.4.0
```
2、将flume分发到其他两个节点
scp -r flume-1.7.0-bin bigdata-pro02.kfk.com:/opt/modules/
scp -r flume-1.7.0-bin bigdata-pro03.kfk.com:/opt/modules/
3、flume agent-2采集节点服务配置（在bigdata-pro02.kfk.com）
三个部分：sources、channels、sinks
/opt/datas/weblogs.log是我们要采集的日志
```
vi flume-conf.properties

agent2.sources = r1
agent2.channels = c1
agent2.sinks = k1

agent2.sources.r1.type = exec
agent2.sources.r1.command = tail -F /opt/datas/weblog-flume.log
agent2.sources.r1.channels = c1

agent2.channels.c1.type = memory
agent2.channels.c1.capacity = 10000
agent2.channels.c1.transactionCapacity = 10000
agent2.channels.c1.keep-alive = 5

agent2.sinks.k1.type = avro
agent2.sinks.k1.channel = c1
agent2.sinks.k1.hostname = bigdata-pro01.kfk.com
agent2.sinks.k1.port = 5555
```
4、flume agent-3采集节点服务配置（在bigdata-pro03.kfk.com）

```
vi flume-conf.properties

agent3.sources = r1
agent3.channels = c1
agent3.sinks = k1

agent3.sources.r1.type = exec
agent3.sources.r1.command = tail -F /opt/datas/weblog-flume.log
agent3.sources.r1.channels = c1

agent3.channels.c1.type = memory
agent3.channels.c1.capacity = 10000
agent3.channels.c1.transactionCapacity = 10000
agent3.channels.c1.keep-alive = 5

agent3.sinks.k1.type = avro
agent3.sinks.k1.channel = c1
agent3.sinks.k1.hostname = bigdata-pro01.kfk.com
agent3.sinks.k1.port = 5555
```
