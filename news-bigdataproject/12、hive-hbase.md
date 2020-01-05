## 第十二章：Hive与Hbase集成
date: 2019-1-23 21:30:01


### Hive与HBase集成配置
1、在hive-site.xml文件中配置Zookeeper，hive通过这个参数去连接HBase集群。
```
<property>
    <name>hbase.zookeeper.quorum</name>   <value>bigdata-pro01.kfk.com,bigdata-pro02.kfk.com,bigdata-pro03.kfk.com</value>
</property>
```
2、需要把hbase中的部分jar包拷贝到hive中
这里采用软连接的方式：
执行如下命令：
```
export HBASE_HOME=/opt/modules/hbase-1.0.0-cdh5.4.0
export HIVE_HOME=/opt/modules/hive-2.1.0
ln -s $HBASE_HOME/lib/hbase-server-1.0.0-cdh5.4.0.jar $HIVE_HOME/lib/hbase-server-1.0.0-cdh5.4.0.jar

ln -s $HBASE_HOME/lib/hbase-client-1.0.0-cdh5.4.0.jar $HIVE_HOME/lib/hbase-client-1.0.0-cdh5.4.0.jar

ln -s $HBASE_HOME/lib/hbase-protocol-1.0.0-cdh5.4.0.jar $HIVE_HOME/lib/hbase-protocol-1.0.0-cdh5.4.0.jar 

ln -s $HBASE_HOME/lib/hbase-it-1.0.0-cdh5.4.0.jar $HIVE_HOME/lib/hbase-it-1.0.0-cdh5.4.0.jar 

ln -s $HBASE_HOME/lib/htrace-core-3.0.4.jar $HIVE_HOME/lib/htrace-core-3.0.4.jar

ln -s $HBASE_HOME/lib/hbase-hadoop2-compat-1.0.0-cdh5.4.0.jar $HIVE_HOME/lib/hbase-hadoop2-compat-1.0.0-cdh5.4.0.jar 

ln -s $HBASE_HOME/lib/hbase-hadoop-compat-1.0.0-cdh5.4.0.jar $HIVE_HOME/lib/hbase-hadoop-compat-1.0.0-cdh5.4.0.jar

ln -s $HBASE_HOME/lib/high-scale-lib-1.1.1.jar $HIVE_HOME/lib/high-scale-lib-1.1.1.jar 

ln -s $HBASE_HOME/lib/hbase-common-1.0.0-cdh5.4.0.jar $HIVE_HOME/lib/hbase-common-1.0.0-cdh5.4.0.jar 
```
3、测试
在hbase中建立一个表，里面存有数据（实际底层就是在hdfs上），然后Hive创建一个表与HBase中的表建立联系。
1）先在hbase建立一个表
（不熟悉的，看指令https://www.cnblogs.com/cxzdy/p/5583239.html）
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzgupdmei1j30h5037mx4.jpg)
2）启动hive,建立联系（之前要先启动mysql，因为元数据在里面）
```
create external table t1(
key int,
name string,
age string
)  
STORED BY  'org.apache.hadoop.hive.hbase.HBaseStorageHandler' 
WITH SERDEPROPERTIES("hbase.columns.mapping" = ":key,info:name,info:age") 
TBLPROPERTIES("hbase.table.name" = "t1");
```
3）hive结果
执行 select * from t1;
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzgutrr5x7j30b0035glg.jpg)
4、为项目中的weblogs建立联系
之前我们把数据通过flume导入到hbase中了，所以同样我们在hive中建立联系，可以用hive对hbase中的数据进行简单的sql分析，离线分析。
```
create external table weblogs(
id string,
datatime string,
userid string,
searchname string,
retorder string,
cliorder string,
cliurl string
)  
STORED BY  'org.apache.hadoop.hive.hbase.HBaseStorageHandler' 
WITH SERDEPROPERTIES("hbase.columns.mapping" = ":key,info:datatime,info:userid,info:searchname,info:retorder,info:cliorder,info:cliurl") 
TBLPROPERTIES("hbase.table.name" = "weblogs");
```

### Hive与HBase集成中的致命bug
问题如图：
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzguxe0p4ej30nu0hl0ua.jpg)
参考办法：https://www.cnblogs.com/zlslch/p/8228781.html
按照上述，参考还是解决不了。
最初怀疑是hbase中的jar包没有导入到hive中，或者导入错了，结果不是这个原因。网上有个大哥也是遇到这个问题了，写了一篇日志，最后他说不知如何解决？？
*********************************************
最终：我去官网看看，官网上说，hbase 1.x之后的版本，需要更高版本的hive匹配，最好是hive 2.x,上述的错误是因为我用的hive-0.13.1-bin和hbase-1.0.0-cdh5.4.0，应该是不兼容导致的，莫名bug。于是采用了 hive-2.1.0，我查了下这个版本与hadoop其他组件也是兼容的，所以，采用这个。配置仍然采用刚才的方法（上一章和这一章），主要有mysql元数据配置（驱动包别忘了），各种xml配置，测试下。最后，在重启hive之前，**先把hbase重启了**，很重要。终于成功了。。开心。
