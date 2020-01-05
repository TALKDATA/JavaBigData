## 第九章：Flume源码修改与HBase+Kafka集成
date: 2019-1-20 11:30:01


### 如何修改flume源码？
因为我们需要在节点1上将flume同时发送至Hbase以及kafka，但是hbase结构需要自定义，所以由flume发送至hbase代码需要进行修改。
项目源码：https://github.com/changeforeda/Big-Data-Project/tree/master/code/flume-ng-sinks
步骤：  
1.下载Flume源码并导入Idea开发工具
1）将apache-flume-1.7.0-src.tar.gz源码下载到本地解压
2）通过idea导入flume源码
打开idea开发工具，选择File——》Open，找到源码包，选中flume-ng-hbase-sink，点击ok加载相应模块的源码。
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzd1atesvcj30dw0fmaae.jpg)
2、自己写个类完成类的修改。KfkAsyncHbaseEventSerializer这个是我自定义的。修改其中的下面这个方法。
```java
@Override
    public List<PutRequest> getActions() {
        List<PutRequest> actions = new ArrayList<>();
        if (payloadColumn != null) {
            byte[] rowKey;
            try {
                /*---------------------------代码修改开始---------------------------------*/
                //解析列字段
                String[] columns = new String(this.payloadColumn).split(",");
                //解析flume采集过来的每行的值
                String[] values = new String(this.payload).split(",");
                for(int i=0;i < columns.length;i++) {
                    byte[] colColumn = columns[i].getBytes();
                    byte[] colValue = values[i].getBytes(Charsets.UTF_8);

                    //数据校验：字段和值是否对应
                    if (colColumn.length != colValue.length) break;

                    //时间
                    String datetime = values[0].toString();
                    //用户id
                    String userid = values[1].toString();
                    //根据业务自定义Rowkey
                    rowKey = SimpleRowKeyGenerator.getKfkRowKey(userid, datetime);
                    //插入数据
                    PutRequest putRequest = new PutRequest(table, rowKey, cf,
                            colColumn, colValue);
                    actions.add(putRequest);
                    /*---------------------------代码修改结束---------------------------------*/
                }
            } catch (Exception e) {
                throw new FlumeException("Could not get row key!", e);
            }
        }
        return actions;
    }
```
修改这个类中自定义KEY生成方法
```java
public class SimpleRowKeyGenerator {

  public static byte[] getKfkRowKey(String userid,String datetime)throws UnsupportedEncodingException {
    return (userid + datetime + String.valueOf(System.currentTimeMillis())).getBytes("UTF8");
  }
}
```
3、应该进行测试，但是这边测试完成，目前不知如何搭建，就直接生成jar包放到虚拟机直接用了。
4、生成jar包，idea很好用
可参考：https://jingyan.baidu.com/article/c275f6ba0bbb65e33d7567cb.html
1）在idea工具中，选择File——》ProjectStructrue
2）左侧选中Artifacts，然后点击右侧的+号，最后选择JAR——》From modules with dependencies
3）一定要设置main class这一项选择自己要打包的类，然后直接点击ok
4）删除其他依赖包，只把flume-ng-hbase-sink打成jar包就可以了。
5）然后依次点击apply，ok
6）点击build进行编译，会自动打成jar包
7）到项目的apache-flume-1.7.0-src\flume-ng-sinks\flume-ng-hbase-sink\classes\artifacts\flume_ng_hbase_sink_jar目录下找到刚刚打的jar包
8）将打包名字替换为flume自带的包名flume-ng-hbase-sink-1.7.0.jar ，然后上传至虚拟机上flume/lib目录下，覆盖原有的jar包即可。
### 修改flume配置
这里在节点1上修改flume的配置，完成与hbase和kafka的集成。（flume自定义的jar已经上传覆盖）
修改flume-conf.properties
```
agent1.sources = r1
agent1.channels = kafkaC hbaseC 
agent1.sinks =  kafkaSink hbaseSink

agent1.sources.r1.type = avro
agent1.sources.r1.channels = hbaseC kafkaC
agent1.sources.r1.bind = bigdata-pro01.kfk.com
agent1.sources.r1.port = 5555
agent1.sources.r1.threads = 5
# flume-hbase
agent1.channels.hbaseC.type = memory
agent1.channels.hbaseC.capacity = 100000
agent1.channels.hbaseC.transactionCapacity = 100000
agent1.channels.hbaseC.keep-alive = 20

agent1.sinks.hbaseSink.type = asynchbase
agent1.sinks.hbaseSink.table = weblogs
agent1.sinks.hbaseSink.columnFamily = info
agent1.sinks.hbaseSink.channel = hbaseC
agent1.sinks.hbaseSink.serializer = org.apache.flume.sink.hbase.KfkAsyncHbaseEventSerializer
agent1.sinks.hbaseSink.serializer.payloadColumn = datatime,userid,searchname,retorder,cliorder,cliurl
#flume-kafka
agent1.channels.kafkaC.type = memory
agent1.channels.kafkaC.capacity = 100000
agent1.channels.kafkaC.transactionCapacity = 100000
agent1.channels.kafkaC.keep-alive = 20

agent1.sinks.kafkaSink.channel = kafkaC
agent1.sinks.kafkaSink.type = org.apache.flume.sink.kafka.KafkaSink
agent1.sinks.kafkaSink.brokerList = bigdata-pro01.kfk.com:9092,bigdata-pro02.kfk.com:9092,bigdata-pro03.kfk.com:9092
agent1.sinks.kafkaSink.topic = weblogs
agent1.sinks.kafkaSink.zookeeperConnect = bigdata-pro01.kfk.com:2181,bigdata-pro02.kfk.com:2181,bigdata-pro03.kfk.com:2181
agent1.sinks.kafkaSink.requiredAcks = 1
agent1.sinks.kafkaSink.batchSize = 1
agent1.sinks.kafkaSink.serializer.class = kafka.serializer.StringEncoder
```

### 小结
项目进行到这里，已经完成了节点2和节点3上flume采集配置、节点1上flume采集并发送至kafka和hbase配置。
如下图，这部分都已经完成，下一章进行联调。加油！！！！
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzd2e99ywhj30go0gp43u.jpg)