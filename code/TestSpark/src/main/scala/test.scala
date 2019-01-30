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
