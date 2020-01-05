## 第十章：Flume+HBase+Kafka集成全流程测试
date: 2019-1-20 20:30:01


### 全流程测试简介
将完成对前面所有的设计进行测试，核心是进行flume日志的采集、汇总以及发送至kafka消费、hbase保存。
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzd3g6rboxj30go0gp43u.jpg)
###  原始日志数据简单处理
1、下载搜狗实验室数据
http://www.sogou.com/labs/resource/q.php
2、格式说明
数据格式为:访问时间\t用户ID\t[查询词]\t该URL在返回结果中的排名\t用户点击的顺序号\t用户点击的URL
其中，用户ID是根据用户使用浏览器访问搜索引擎时的Cookie信息自动赋值，即同一次使用浏览器输入的不同查询对应同一个用户ID
3、日志简单处理
1）将文件中的tab更换成逗号
cat weblog.log|tr "\t" "," > weblog2.log
2）将文件中的空格更换成逗号
cat weblog2.log|tr " " "," > weblog3.log
处理完：
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzd3eylp5zj30l008fab1.jpg)
###  编写模拟日志生成过程
1、代码实现
    实现功能是将原始日志，每次读取一行不断写入到另一个文件中（weblog-flume.log），所以这个文件就相等于服务器中日志不断增加的过程。编写完程序，将该项目打成weblogs.jar包，然后上传至bigdata-pro02.kfk.com节点和bigdata-pro03.kfk.com节点的/opt/jars目录下（目录需要提前创建）
代码工程地址：https://github.com/changeforeda/Big-Data-Project/tree/master/code/DataProducer
2、编写运行模拟日志程序的shell脚本
```
1）
在bigdata-pro02.kfk.com节点的/opt/datas目录下，创建weblog-shell.sh脚本。
vi weblog-shell.sh
#/bin/bash
echo "start log......"
#第一个参数是原日志文件，第二个参数是日志生成输出文件
java -jar /opt/jars/weblogs.jar /opt/datas/weblog.log /opt/datas/weblog-flume.log

修改weblog-shell.sh可执行权限
chmod 777 weblog-shell.sh
2）
将bigdata-pro02.kfk.com节点上的/opt/datas/目录拷贝到bigdata-pro03节点.kfk.com
scp -r /opt/datas/ bigdata-pro03.kfk.com:/opt/datas/
```
3、运行测试
/opt/datas/weblog-shell.sh
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzdb284hefj30he0chn95.jpg)
###  编写一些shell脚本便于执行
1、编写启动flume服务程序的shell脚本
```
1.在bigdata-pro02.kfk.com节点的flume安装目录下编写flume启动脚本。
vi flume-kfk-start.sh
#/bin/bash
echo "flume-2 start ......"
bin/flume-ng agent --conf conf -f conf/flume-conf.properties -n agent2 -Dflume.root.logger=INFO,console
2.在bigdata-pro03.kfk.com节点的flume安装目录下编写flume启动脚本。
vi flume-kfk-start.sh
#/bin/bash
echo "flume-3 start ......"
bin/flume-ng agent --conf conf -f conf/flume-conf.properties -n agent3 -Dflume.root.logger=INFO,console
3.在bigdata-pro01.kfk.com节点的flume安装目录下编写flume启动脚本。
vi flume-kfk-start.sh
#/bin/bash
echo "flume-1 start ......"
bin/flume-ng agent --conf conf -f conf/flume-conf.properties -n agent1 -Dflume.root.logger=INFO,console

```
2、编写Kafka Consumer执行脚本
```
1.在bigdata-pro01.kfk.com节点的Kafka安装目录下编写Kafka Consumer执行脚本
vi kfk-test-consumer.sh
#/bin/bash
echo "kfk-kafka-consumer.sh start ......"
bin/kafka-console-consumer.sh --zookeeper bigdata-pro01.kfk.com:2181,bigdata-pro02.kfk.com:2181,bigdata-pro03.kfk.com:2181 --from-beginning --topic weblogs
2.将kfk-test-consumer.sh脚本分发另外两个节点
scp kfk-test-consumer.sh bigdata-pro02.kfk.com:/opt/modules/kakfa_2.11-0.8.2.1/
scp kfk-test-consumer.sh bigdata-pro03.kfk.com:/opt/modules/kakfa_2.11-0.8.2.1/

```
###  联调测试-数据采集分发
```
1、在各个节点上启动zk
/opt/modules/zookeeper-3.4.5-cdh5.10.0/sbin/zkServer.sh start  
/opt/modules/zookeeper-3.4.5-cdh5.10.0/bin/zkCli.sh  登陆客户端进行测试是否启动成功

2、启动hdfs  --- http://bigdata-pro01.kfk.com:50070/
在节点1：/opt/modules/hadoop-2.6.0/sbin/start-dfs.sh 
#节点1 和 节点2  启动namenode高可用
/opt/modules/hadoop-2.6.0/sbin/hadoop-daemon.sh start zkfc

3、启动hbase  ----http://bigdata-pro01.kfk.com:60010/
#节点 1  启动hbase
/opt/modules/hbase-1.0.0-cdh5.4.0/bin/start-hbase.sh
#在节点2 启动备用master
/opt/modules/hbase-1.0.0-cdh5.4.0/bin/hbase-daemon.sh start  master
#启动hbase的shell用于操作
/opt/modules/hbase-1.0.0-cdh5.4.0/bin/hbase shell
#创建hbase业务表
bin/hbase shell
create 'weblogs','info'

4、启动kafka
#在各个个节点启动kafka
cd /opt/modules/kafka_2.10-0.9.0.0
bin/kafka-server-start.sh config/server.properties &
#创建业务
bin/kafka-topics.sh --zookeeper bigdata-pro01.kfk.com:2181,bigdata-pro02.kfk.com:2181,bigdata-pro03.kfk.com:2181 --create --topic weblogs --replication-factor 2 --partitions 1
#消费(之前编写的脚本可以用)
bin/kafka-console-consumer.sh --zookeeper bigdata-pro01.kfk.com:2181,bigdata-pro02.kfk.com:2181,bigdata-pro03.kfk.com:2181 --from-beginning --topic weblogs
```
一定确保上述都启动成功能，利用jps查看各个节点进程情况。
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzdbmh1n31j309v042glj.jpg)![](http://ww1.sinaimg.cn/large/005BOtkIly1fzdbmovok3j309n03sa9y.jpg)![](http://ww1.sinaimg.cn/large/005BOtkIly1fzdbmw14tjj309o02cweb.jpg)
```
5、各个节点启动flume
#三节点启动flume
/opt/modules/flume-1.7.0-bin/flume-kfk-start.sh

6、在节点2和3启动日志模拟生产
/opt/datas/weblog-shell.sh

7、启动kafka消费程序
#消费（或者使用写好的脚本kfk-test-consumer.sh）
bin/kafka-console-consumer.sh --zookeeper bigdata-pro01.kfk.com:2181,bigdata-pro02.kfk.com:2181,bigdata-pro03.kfk.com:2181 --from-beginning --topic weblogs

8、查看hbase数据写入情况
./hbase-shell
count 'weblogs'
```
结果：
kafka不断消费
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzdbszmkybj30rh0940ue.jpg)
hbase数据不断增加
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzdbtek6eqj30rv0ar0ud.jpg)

###  遇到的一些问题
1、组件启动不起来
可能原因是环境变量没设置，比如在启动flume时，因为调用java，所以要设置环境变量在flume的配置文件中。
2、各个组件都启动了，但是没数据
我是因为flume的sink写错了，所以根本输出不了数据，我是通过先把sink设置成输出到控制台发现没数据，再去看配置sink到底怎么了
**3、解决各种小问题**
1）看问题的日志；或者把日志中问题复制到百度，基本可以解决60%
2）整个功能实现不了，应该从数据源头查看，一步一步向后排除原因，比如没数据，看源头到底输出数据了吗？
3）问题还是解决不了，就要反思自己是否有不懂的地方，设置错了。或者you can talk with me。。。
