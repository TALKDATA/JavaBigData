## 第十三章：Cloudera HUE大数据可视化分析
date: 2019-1-26 19:30:01


### 下载和安装Hue
版本选择： hue-3.9.0-cdh5.15.0
1、首先需要利用yum安装依赖包，虚拟机需要联网，这里安装在节点3上。
```
yum -y install ant asciidoc cyrus-sasl-devel cyrus-sasl-gssapi gcc gcc-c++ krb5-devel libtidy libxml2-devel libxslt-devel openldap-devel python-devel sqlite-devel openssl-devel mysql-devel gmp-devel  
```
2、解压
tar -zxf hue-3.9.0-cdh5.15.0.tar.gz -C /opt/modules/
3、编译
cd  hue-3.9.0-cdh5.15.0
make apps
4、基本配置与测试
```java
1）修改配置文件
cd desktop
cd conf
vi hue.ini
#秘钥
secret_key=jFE93j;2[290-eiw.KEiwN2s3['d;/.q[eIW^y#e=+Iei*@Mn < qW5o
#host port
http_host=bigdata-pro03.kfk.com
http_port=8888
#时区
time_zone=Asia/Shanghai
2）修改desktop.db 文件权限
chmod o+w desktop/desktop.db
3）启动Hue服务
/opt/modules/hue-3.9.0-cdh5.15.0/build/env/bin/supervisor
4）查看Hue web界面
bigdata-pro03.kfk.com:8888
```
### Hue与HDFS集成
```
1）修改hadoop中core-site.xml配置文件，添加如下内容
<property>
    <name>hadoop.proxyuser.hue.hosts</name>
    <value>*</value>
</property>
<property>
    <name>hadoop.proxyuser.hue.groups</name>
    <value>*</value>
</property>

2）修改hue.ini配置文件
fs_defaultfs=hdfs://ns
webhdfs_url=http://bigdata-pro01.kfk.com:50070/webhdfs/v1
hadoop_hdfs_home=/opt/modules/hadoop-2.6.0
hadoop_bin=/opt/modules/hadoop-2.6.0/bin
hadoop_conf_dir=/opt/modules/hadoop-2.6.0/etc/hadoop
3）将core-site.xml配置文件分发到其他节点
scp core-site.xml bigdata-pro02.kfk.com:/opt/modules/hadoop-2.6.0/etc/hadoop
scp core-site.xml bigdata-pro01.kfk.com:/opt/modules/hadoop-2.6.0/etc/hadoop
4）重新启动hue
先启动zk,hdfs，再启动hue
/opt/modules/hue-3.9.0-cdh5.15.0/build/env/bin/supervisor
```
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzk8cc0l9rj30ev0ebmx9.jpg)

### Hue与YARN集成
1、修改hue.ini配置文件,参考https://www.cnblogs.com/zlslch/p/6817226.html
区分yarn是不是HA
```
 [[yarn_clusters]]

    [[[default]]]
      resourcemanager_host=rs
      resourcemanager_port=8032
      submit_to=True
      logical_name=rm1
      resourcemanager_api_url=http://bigdata-pro01.kfk.com:8088
      proxy_api_url=http://bigdata-pro01.kfk.com:8088
      history_server_api_url=http://bigdata-pro01.kfk.com:19888

     [[[ha]]]
      logical_name=rm2
      submit_to=True
      resourcemanager_api_url=http://bigdata-pro02.kfk.com:8088
	  history_server_api_url=http://bigdata-pro01.kfk.com:19888
```
2、测试
启动yarn，再重启hue。
图中的任务是我之前进行的任务
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzk8i4ao6kj314r07j74m.jpg)

### Hue与mysql、hive集成
1、修改hue.ini配置
```
    [beeswax]


      hive_server_host=bigdata-pro03.kfk.com
      hive_server_port=10000
      hive_conf_dir=/opt/modules/hive-2.1.0/conf
  
  
  .........中间其他......
     [[[mysql]]]
      nice_name="My SQL DB"

      name=metastore
      engine=mysql

      host=bigdata-pro01.kfk.com
      port=3306
      user=root
      password=123456
```
2、测试
启动节点1的mysql（这是元数据），再启动节点3的hive服
/opt/modules/hive-2.1.0/bin/hive --service hiveserver2 &    ##配合hue服务
再重启hue。
图中是利用hive中的sql查询，hive中的表。但是有一个问题是：我用hive查询hbase中的表，无法查询，出现超时情况，目前还没解决，搞了2天难受，（本来想直接在hue中用hive来处理hbase中的表进行离线计算，但是没法查询，只能查询hive本身自己的表，另外hive的beeline模式也无法查询hbase表，但是hive cli模式可以的查询）
问题日志：:java.io.IOException: org.apache.hadoop.hbase.client.RetriesExhaustedException
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzk8rvrxu6j30n20e3q35.jpg)

### Hue与hbase集成
1、修改hue.ini配置
```
[hbase]
   hbase_clusters=(Cluster|bigdata-pro01.kfk.com:9090)
   hbase_conf_dir=/opt/modules/hbase-1.0.0-cdh5.4.0/conf
  thrift_transport=buffered
```
2、启动测试
先启动hbase,再启动HBase中启动thrift服务
/opt/modules/hbase-1.0.0-cdh5.4.0/bin/hbase-daemon.sh start thrift
然后重启hue
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzk9mr2i83j30kp0ddaa9.jpg)

