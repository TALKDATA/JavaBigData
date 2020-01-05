## 第十六章：Spark Streaming实时数据处理
date: 2019-2-03 14:30:01


### Spark Streaming简介
本质上就是利用批处理时间间隔来处理一小批的RDD集合。
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzosp71irxj30g108hq75.jpg)

### idea中程序测试读取socket
1、在节点1启动nc
nc -lk 9999
输入一些单词
2、在idea中运行程序
```
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
object TestStreaming {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val ssc = new StreamingContext(conf, Seconds(5))

    val lines = ssc.socketTextStream("bigdata-pro01.kfk.com",9999)
    val words = lines.flatMap(_.split(" "))
    //map reduce 计算
    val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
    wordCounts.print()
    ssc.start()
    ssc.awaitTermination()

  }
}
```
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzot0y7ib8j30tl0a4mxy.jpg)

### sparkstreaming和kafka进行集成
版本问题：
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzr9u0l3szj31cl048js0.jpg)
遇到了版本问题，之前用的是kafka0.9，现在和idea集成开发一般是kafka0.10了，还好官网里有支持kafka0.9程序案例，要不然就完犊子了，参考官网进行编写：
http://spark.apache.org/docs/2.2.0/streaming-kafka-0-8-integration.html
代码案例：https://github.com/apache/spark/blob/v2.2.0/examples/src/main/scala/org/apache/spark/examples/streaming/DirectKafkaWordCount.scala
基于kafka0.9的测试程序
```scala
import kafka.serializer.StringDecoder
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}


object KfkStreaming {
   def main(args: Array[String]): Unit = {

     val spark  = SparkSession.builder()
       .master("local[2]")
       .appName("kfkstreaming").getOrCreate()

     val sc =spark.sparkContext
     val ssc = new StreamingContext(sc, Seconds(5))

     val topicsSet = Set("weblogs")
     val kafkaParams = Map[String, String]("metadata.broker.list" -> "bigdata-pro01.kfk.com:9092")
     val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
       ssc, kafkaParams, topicsSet)

     val lines = messages.map(_._2)
     val words = lines.flatMap(_.split(" "))
     val wordCounts = words.map(x => (x, 1L)).reduceByKey(_ + _)
     wordCounts.print()

     // Start the computation
     ssc.start()
     ssc.awaitTermination()

   }

}

```
在节点1上启动kafka程序
```
bin/kafka-server-start.sh config/server.properties
bin/kafka-console-producer.sh --broker-list bigdata-pro01.kfk.com:9092 --topic weblogs

```
运行结果：
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzr9tkp4szj30om07paas.jpg)