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
