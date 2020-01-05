## 第十四章：Spark2.X集群安装与spark on yarn部署
date: 2019-1-30 11:30:01


### spark学习准备
1、简介
Spark 是一个用来实现快速而通用的集群计算的平台。

Spark 的一个主要特点就是能够在内存中进行计算， 因而更快。不过即使是必须在磁盘上进行的复杂计算， Spark 依然比 MapReduce 更加高效。

2、学习网站
1）databricks 网站
**2）spark 官网**  
3）github 网站 中spark有很多例子

### spark集群安装
我用版本是spark-2.2.0-bin-hadoop2.6.tgz，因为我之前用的是hadoop2.6.0.
环境要求：scala-2.11.12.tgz/java8/hadoop2.6.0.
1、官网下载
https://spark.apache.org/downloads.html
2、spark配置
配置spark-env.sh
```
export JAVA_HOME=/opt/modules/jdk1.8.0_191
export SCALA_HOME=/opt/modules/scala-2.11.12

export HADOOP_CONF_DIR=/opt/modules/hadoop-2.6.0/etc/hadoop
export SPARK_CONF_DIR=/opt/modules/spark-2.2.0/conf
export SPARK_MASTER_HOST=bigdata-pro02.kfk.com
export SPARK_MASTER_PORT=7077
export SPARK_MASTER_WEBUI_PORT=8080
export SPARK_WORKER_CORES=1
export SPARK_WORKER_MEMORY=1g
export SPARK_WORKER_PORT=7078
export SPARK_WORKER_WEBUI_PORT=8081
```
配置slaves
```
bigdata-pro01.kfk.com
bigdata-pro02.kfk.com
bigdata-pro03.kfk.com
```
如果整合hive,hive用到mysql数据库的话，需要将mysql数据库连接驱动jmysql-connector-java-5.1.7-bin.jar放到$SPARK_HOME/jars目录下
3、分发至各个节点
4、设定的主节点上启动测试(这是standalone模式)
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzogese9lvj30on05o74m.jpg)
打开spark服务网址：http://bigdata-pro02.kfk.com:8080/
可以查看到各个节点的情况。
5、可以stop-all，因为yarn模式下根本不需要。

### spark on Yarn
standalonen模式和spark on Yarn模式比较： https://blog.csdn.net/lxhandlbb/article/details/70214003
spark on Yarn原理：https://blog.csdn.net/liuwei0376/article/details/78637732
1、前提条件
已经安装了hadoop2.6.0，并可以运行，因为spark运行需要依赖hadoop.
2、运行zk、hdfs和yarn
高可用下的zk也要运行
hadoop:http://bigdata-pro01.kfk.com:50070
yarn：http://bigdata-pro01.kfk.com:8088
3、主节点运行spark
./spark-shell --master yarn --deploy-mode client
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzohi45ckpj30o70auq3g.jpg)
在yarn的网页中也可以看到。
虚拟机内存小的话，会出现问题：
```
17/09/08 10:36:08 ERROR spark.SparkContext: Error initializing SparkContext.
org.apache.spark.SparkException: Yarn application has already ended! It might have been killed or unable to launch application master.
```
解决办法：先停止YARN服务，然后修改yarn-site.xml，分发至各个节点。再重启。
增加如下内容
```
    <property>
        <name>yarn.nodemanager.vmem-check-enabled</name>
        <value>false</value>
    </property>
    <property>
        <name>yarn.nodemanager.vmem-pmem-ratio</name>
        <value>4</value>
    </property>
```
4、测试下程序运行
```
sc.parallelize(1 to 100,5).count
```
查看程序运行情况：
1）入口yarn的web网页，
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzohlg58qyj31dz0b8tam.jpg)
2）点击applicationmaster进入
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzoi538xtkj31ck0bwjrz.jpg)
可能出现问题进不去网页：
配置显示在主节点：这里配置节点1，那么RM应该在1的时候可以显示，之前我配集群总名称rs，没法用。
修改yarn-site.xml，分发至各个节点，然后重启。
```	<property>
		<name>yarn.resourcemanager.webapp.address</name>
		<value>bigdata-pro01.kfk.com:8088</value>
	</property>
```