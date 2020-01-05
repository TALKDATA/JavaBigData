## 第五章：hadoop的高可用配置（HA）
date: 2018-12-29 15:52:11


### HDFS-HA架构原理介绍
![](http://ww1.sinaimg.cn/large/005BOtkIly1fynlvh2aiyj30dp0boac3.jpg)

当发生故障时，Active的 NN 挂掉后，Standby NN 会在它成为Active NN 前，读取所有的JN里面的修改日志，这样就能高可靠的保证与挂掉的NN的目录镜像树一致，然后无缝的接替它的职责，维护来自客户端请求，从而达到一个高可用的目的。

### HDFS-HA修改配置文件
1、修改hdfs-site.xml配置文件
参照官网格式：http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HDFSHighAvailabilityWithQJM.html
```xml
<configuration>
	<property>
	  <name>dfs.nameservices</name>
	  <value>ns</value>
	</property>
	<property>
	  <name>dfs.ha.namenodes.ns</name>
	  <value>nn1,nn2</value>
	</property>
	<property>
	  <name>dfs.namenode.rpc-address.ns.nn1</name>
	  <value>bigdata-pro01.kfk.com:8020</value>
	</property>
	<property>
	  <name>dfs.namenode.rpc-address.ns.nn2</name>
	  <value>bigdata-pro02.kfk.com:8020</value>
	</property>
	<property>
      <name>dfs.namenode.http-address.ns.nn1</name>
      <value>bigdata-pro01.kfk.com:50070</value>
    </property>
	<property>
       <name>dfs.namenode.http-address.ns.nn2</name>
       <value>bigdata-pro02.kfk.com:50070</value>
    </property>
    <property>
        <name>dfs.namenode.shared.edits.dir</name>
        <value>qjournal://bigdata-pro01.kfk.com:8485;bigdata-pro02.kfk.com:8485;bigdata-pro03.kfk.com:8485/ns</value>
    </property>
	<property>
       <name>dfs.journalnode.edits.dir</name>
       <value>/opt/modules/hadoop-2.6.0/data/jn</value>
    </property>
	<property>
		<name>dfs.client.failover.proxy.provider.ns</name>
		<value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>
    </property>
	<property>
        <name>dfs.ha.automatic-failover.enabled.ns</name>
        <value>true</value>
    </property>
	<property>
		<name>dfs.ha.fencing.methods</name>
		<value>sshfence</value>
	</property>
    <property>
        <name>dfs.ha.fencing.ssh.private-key-files</name>
        <value>/home/kfk/.ssh/id_rsa</value>
    </property>
	<property>
        <name>dfs.replication</name>
        <value>3</value>
    </property>
	<property>
        <name>dfs.permissions.enabled</name>
        <value>false</value>
    </property>
</configuration>
```
2、修改core-site.xml配置文件
```xml
<configuration>
	<property>
        <name>fs.defaultFS</name>
        <value>hdfs://ns</value>
	</property>
	<property>
        <name>hadoop.http.staticuser.user</name>
        <value>kfk</value>
	</property>	
	<property>
		<name>hadoop.tmp.dir</name>
		<value>/opt/modules/hadoop-2.6.0/data/tmp</value>
	</property>
	<property>
		<name>dfs.namenode.name.dir</name>
		<value>file://${hadoop.tmp.dir}/dfs/name</value>
	</property>
	<property>
		<name>ha.zookeeper.quorum</name>
		<value>bigdata-pro01.kfk.com:2181,bigdata-pro02.kfk.com:2181,bigdata-pro03.kfk.com:2181</value>
	</property>
</configuration>

```
3、将修改的配置分发到其他节点
```
scp hdfs-site.xml bigdata-pro02.kfk.com:/opt/modules/hadoop-2.6.0/etc/hadoop/
scp hdfs-site.xml bigdata-pro03.kfk.com:/opt/modules/hadoop-2.6.0/etc/hadoop/
scp core-site.xml bigdata-pro02.kfk.com:/opt/modules/hadoop-2.6.0/etc/hadoop/
scp core-site.xml bigdata-pro03.kfk.com:/opt/modules/hadoop-2.6.0/etc/hadoop/
```
### HDFS-HA自动故障转移测试
1、在所有节点启动zookeeper
 cd /opt/modules/zookeeper-3.4.5-cdh5.10.0/
 sbin/zkServer.sh start
 bin/hdfs zkfc -formatZK            （第一次使用zkfc需要格式化）
2、启动hdfs
 bin/hdfs namenode -format          （第一次使用hdfs需要格式化，在namenode）
 sbin/start-dfs.sh                    （会在各个节点上启动namenode/datanode/journalnode）
3、在HA的namenode节点上启动zkfc线程（两个namenode都要启动）
 sbin/hadoop-daemon.sh start zkfc
 查看两个namenode状态一个是active(先启动zkfc的)，一个是standy，查看网页。
 http://bigdata-pro01.kfk.com:50070
 http://bigdata-pro02.kfk.com:50070
4、上传文件到hdfs
 bin/hdfs dfs -mkdir /usr
 bin/hdfs dfs -put /opt/modules/hadoop-2.6.0/etc/hadoop/hdfs-site.xml /usr
 在网页中可以看到
5、杀死active的namenode
![](http://ww1.sinaimg.cn/large/005BOtkIly1fynmg2l3ptj30au032dfq.jpg)
6、再次查看namenode状态
应该完成了主备切换。原来的standy变成了active.
![](http://ww1.sinaimg.cn/large/005BOtkIly1fynmjhur96j30py096t94.jpg)
### HDFS-HA所遇到的问题（看输出日志和查看日志）
**1、输出提示：无法解析bigdata-pro03.kfk.com:2181**
原因：因为我的core-site.xml配置文件写错了,参数一栏不能有换行，要不然读的不对的。
```xml
	<property>
		<name>ha.zookeeper.quorum</name>
		<value>bigdata-pro01.kfk.com:2181,bigdata-pro02.kfk.com:2181,bigdata-pro03.kfk.com:2181</value>
	</property>
```
**2、 sbin/start-dfs.sh   启动不成功**
因为这个启动需要配置ssh，所以
（1）在节点1上
ssh-keygen
ssh-copy-id bigdata-pro1.kfk.com   (包括自己的也要ssh)
ssh-copy-id bigdata-pro2.kfk.com
ssh-copy-id bigdata-pro3.kfk.com
（2）测试ssh连接
ssh bigdata-pro1.kfk.com
ssh bigdata-pro2.kfk.com
ssh bigdata-pro3.kfk.com
**3、 namenode准备切换失败**
bigdata-pro1.kfk.com可以竞选成active，但是杀掉bigdata-pro1.kfk.com，而bigdata-pro2.kfk.com不会竞选成active，仍然是standby。
查看bigdata-pro2.kfk.com日志：
 tail -10f hadoop-kfk-zkfc-bigdata-pro02.kfk.com.log
 ![](http://ww1.sinaimg.cn/large/005BOtkIly1fynn11ym39j30q10ftq5a.jpg)
 **红线部分说明，在bigdata-pro2.kfk.com准备选举时，需要对pro1进行fence，但是失败了，原因是ssh失败，说明在节点2上没法ssh到节点1上，所以需要在节点2上进行ssh-keygen,然后拷贝到节点1，这样就解决了**
 ![](http://ww1.sinaimg.cn/large/005BOtkIly1fynn4p3524j30qd070aat.jpg)
 


----------

### YARN-HA架构原理及介绍
![](http://ww1.sinaimg.cn/large/005BOtkIly1fynn8cje51j30hp0azdhq.jpg)
ResourceManager HA 由一对Active，Standby结点构成，通过RMStateStore存储内部数据和主要应用的数据及标记。
目前支持的可替代的RMStateStore实现有：基于内存的MemoryRMStateStore，基于文件系统的FileSystemRMStateStore，及基于zookeeper的ZKRMStateStore。 
ResourceManager HA的架构模式同NameNode HA的架构模式基本一致，数据共享由RMStateStore，而ZKFC成为 ResourceManager进程的一个服务，非独立存在。
### YARN-HA配置文件
**1、修改yarn-site.xml配置文件**
```xml
<configuration>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
	<property>
        <name>yarn.log-aggregation-enable</name>
        <value>true</value>
    </property>
	<property>
        <name>yarn.log-aggregation.retain-seconds</name>
        <value>10000</value>
    </property>
	<property>
		<name>yarn.resourcemanager.ha.enabled</name>
		<value>true</value>
	</property>
	<property>
		<name>yarn.resourcemanager.cluster-id</name>
		<value>rs</value>
	</property>
	<property>
		<name>yarn.resourcemanager.ha.rm-ids</name>
		<value>rm1,rm2</value>
	</property>
	<property>
		<name>yarn.resourcemanager.hostname.rm1</name>
		<value>bigdata-pro01.kfk.com</value>
	</property>
	<property>
		<name>yarn.resourcemanager.hostname.rm2</name>
		<value>bigdata-pro02.kfk.com</value>
	</property>
	<property>
		  <name>yarn.resourcemanager.zk-address</name>
		  <value>bigdata-pro01.kfk.com:2181,bigdata-pro02.kfk.com:2181,bigdata-pro03.kfk.com:2181</value>
	</property>
	<property>
		<name>yarn.resourcemanager.recovery.enabled</name>
		<value>true</value>
	</property>
	<property>
		<name>yarn.resourcemanager.store.class</name>
		<value>org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore</value>
	</property>	

	

</configuration>

```
**2、分发至其他节点**
scp yarn-site.xml bigdata-pro02.kfk.com:/opt/modules/hadoop-2.6.0/etc/hadoop/
scp yarn-site.xml bigdata-pro03.kfk.com:/opt/modules/hadoop-2.6.0/etc/hadoop/
### YARN-HA故障转移测试
1、在rm1节点上启动yarn服务
 sbin/start-yarn.sh
2、在rm2节点上启动ResourceManager服务
sbin/yarn-daemon.sh start resourcemanager
3、查看yarn的web界面
http://bigdata-pro01.kfk.com:8088
http://bigdata-pro02.kfk.com:8088
4、上传wordcount所需的文件到hdfs并执行MapReduce例子
bin/hdfs dfs -put data/wc  /usr/kfk/data  
bin/yarn jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.6.0.jar wordcount /usr/kfk/data/wc /usr/kfk/data/wc.out
5、执行到一半的时候，kill掉rm1上的resourcemanager
任务会转移到rm2继续处理
这是bigdata-pro01.kfk.com输出的日志（额外打开一个bigdata-pro01.kfk.com进行kill）
![](http://ww1.sinaimg.cn/large/005BOtkIly1fynpuokvgdj30mb01xjrd.jpg)
![](http://ww1.sinaimg.cn/large/005BOtkIly1fynpxiwdvhj31cb0b2jsz.jpg)