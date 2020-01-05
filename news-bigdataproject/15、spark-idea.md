## 第十五章：基于IDEA环境下的Spark2.X程序开发
date: 2019-1-30 14:30:01


### 开发环境配置
1、安装idea
2、安装maven
官网下载：apache-maven-3.6.0
3、安装java8，并配置环境变量
4、安装scala，直接从idea插件下载安装
5、安装hadoop在Windows中的运行环境，并配置环境变量
（软件下载链接：https://github.com/changeforeda/Big-Data-Project/blob/master/README.md）

### IDEA程序开发
可以参考这个链接很全：https://blog.csdn.net/zkf541076398/article/details/79297820
1、新建maven项目
2、配置maven
3、选择配置scala和java版本
4、新建scala目录并设置为source(看图)
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzokar9bjxj30pj0dc13u.jpg)
5、编写pom.xml文件
这里主要你需要什么就放什么，可以github上找例子
https://github.com/apache/spark/blob/master/examples/pom.xml
我的pom，我自己可以用
```
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

  <modelVersion>4.0.0</modelVersion>
  <packaging>war</packaging>

  <name>TestSpark</name>
  <groupId>com.kfk.spark</groupId>
  <artifactId>TestSpark</artifactId>
  <version>1.0-SNAPSHOT</version>


  <properties>
    <scala.version>2.11.12</scala.version>
    <scala.binary.version>2.11</scala.binary.version>
    <spark.version>2.2.0</spark.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.apache.spark</groupId>
      <artifactId>spark-core_${scala.binary.version}</artifactId>
      <version>${spark.version}</version>
    </dependency>
    <dependency>
      <groupId>org.apache.spark</groupId>
      <artifactId>spark-streaming_${scala.binary.version}</artifactId>
      <version>${spark.version}</version>
    </dependency>
    <dependency>
      <groupId>org.apache.spark</groupId>
      <artifactId>spark-sql_${scala.binary.version}</artifactId>
      <version>${spark.version}</version>
    </dependency>
    <dependency>
      <groupId>org.apache.spark</groupId>
      <artifactId>spark-hive_${scala.binary.version}</artifactId>
      <version>${spark.version}</version>
    </dependency>
    <dependency>
      <groupId>org.apache.spark</groupId>
      <artifactId>spark-streaming-kafka-0-10_${scala.binary.version}</artifactId>
      <version>${spark.version}</version>
    </dependency>
    <dependency>
      <groupId>org.apache.hadoop</groupId>
      <artifactId>hadoop-client</artifactId>
      <version>2.6.0</version>
    </dependency>
  </dependencies>

</project>

```
6、编写测试程序
```
import org.apache.spark.sql.SparkSession

object test {
  def main(args: Array[String]): Unit = {

     val spark = SparkSession
      .builder
       .master("yarn-cluster")
     //  .master("local[2]")
      .appName("HdfsTest")
      .getOrCreate()

    val path = args(0)
    val out = args(1)

    val rdd = spark.sparkContext.textFile(path)
    val lines = rdd.flatMap(_.split(" ")).map(x=>(x,1)).reduceByKey((a,b)=>(a+b)).saveAsTextFile(out)
  }

}
```
7、本地测试
直接master("local[2]")，指定windows下的路径就可以了。如果不能运行一定是开发环境有问题，主要看看hadoop环境变量配置了吗
8、打成jar包
可参考：https://jingyan.baidu.com/article/c275f6ba0bbb65e33d7567cb.html
9、上传至虚拟机中进行jar包方式提交到spark on yarn.
运行底层还是依赖于hdfs，前提要启动zk /hadoop /yarn.
```
 bin/spark-submit --class  test  --master yarn --deploy-mode cluster /opt/jars/TestSpark.jar  hdfs://ns/input/stu.txt  hdfs://ns/out
```
运行结束去，可以在yarn的web:http://bigdata-pro01.kfk.com:8088/cluster/
看见调度success标志。
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzol6uy6oaj30of09hjsa.jpg)
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzom68klrwj315f0l23zq.jpg)

10、如果运行失败怎么办？看日志
有一个比较好的入口上图圈中的logs：
先配置yarn-site.xml
```
	<property>
         <name>yarn.log.server.url</name>
         <value>http://bigdata-pro01.kfk.com:19888/jobhistory/logs</value>
	</property>
```
需要重启yarn，
并在你配置节点启动历史服务器./mr-jobhistory-daemon.sh start historyserver
点击：http://bigdata-pro01.kfk.com:8088/cluster
![](http://ww1.sinaimg.cn/large/005BOtkIly1fzom51hwo5j30pa0npaci.jpg)