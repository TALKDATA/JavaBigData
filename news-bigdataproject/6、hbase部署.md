## 第六章：hadoop的HA下的高可用HBase部署
date: 2018-12-30 15:52:11


### HBase简介与设计
HBase是一个高可靠、高性能、面向列、可伸缩的分布式存储系统，利用Hbase技术可在廉价PC Server上搭建 大规模非结构化存储集群。底层就是在hdfs一个目录。
下载Apache版本的HBase：https://archive.apache.org/dist/
下载Cloudera版本的HBase：http://archive-primary.cloudera.com/cdh5/cdh/5/
这里选择Cloudera是hbase-1.0.0-cdh5.4.0.tar.gz
![](http://ww1.sinaimg.cn/large/005BOtkIly1fynxysp89aj30js08mn04.jpg)
![](http://ww1.sinaimg.cn/large/005BOtkIly1fynyfa2lptj30y10esgm9.jpg)
### HBase安装与部署
![](http://ww1.sinaimg.cn/large/005BOtkIly1fynxzha2acj30hr053weh.jpg)
1、解压安装到/opt/modules/
2、修改配置文件
**a.hbase-env.sh**
配置jdk
export JAVA_HOME=/opt/modules/jdk1.8.0_191
使用外部的Zookeeper
export HBASE_MANAGES_ZK=false
**b.hbase-site.xml**
这里采用hadoop高可用下的配置
```xml
<configuration>
	<property>
    		<name>hbase.rootdir</name>
    		<value>hdfs://ns/hbase</value>
	</property>
	<property>
    		<name>hbase.cluster.distributed</name>
    		<value>true</value>
	</property>
	<property>
		<name>hbase.zookeeper.quorum</name>
		<value>bigdata-pro01.kfk.com,bigdata-pro02.kfk.com,bigdata-pro03.kfk.com</value>
	</property>
</configuration>
```
**c.regionservers**
bigdata-pro01.kfk.com
bigdata-pro02.kfk.com
bigdata-pro03.kfk.com

**3、将hadoop中hdfs-site.xml和core-site.xml拷贝到hbase的conf下**
要不然会启动失败，具体日志如下：不认识ns，因为ns在hadoop中配置的
![](http://ww1.sinaimg.cn/large/005BOtkIly1fyny4rdsrfj30pu0cjdh8.jpg)
4、将hbase配置分发到各个节点
scp -r hbase-1.0.0-cdh5.4.0 bigdata-pro02.kfk.com:/opt/modules/
scp -r hbase-1.0.0-cdh5.4.0 bigdata-pro03.kfk.com:/opt/modules/

### HBase启动与测试
1、先启动zookeeper
    zkServer.sh start
2、启动高可用下的hdfs
    sbin/start-dfs.sh （会在各个节点上启动namenode/datanode/journalnode）
在HA的namenode节点上启动zkfc线程（两个namenode都要启动）
sbin/hadoop-daemon.sh start zkfc
3、启动hbase
bin/start-hbase.sh
4、查看HBase Web界面
bigdata-pro01.kfk.com:60010/
5、HBase的master高可用测试
```
在bigdata-pro02.kfk.com上启动master,
./hbase-daemon.sh start master
然后杀死bigdata-pro01.kfk.com的Hmaster
zookeeper会自动切换master
```
![](http://ww1.sinaimg.cn/large/005BOtkIly1fynycepyczj30xp0cl3zb.jpg)

### HBase的shell测试
1、启动shell
bin/hbase shell
2、创建表
create 'weblogs','info'
3、列出表
list
![](http://ww1.sinaimg.cn/large/005BOtkIly1fyososol82j306q02o0sk.jpg)