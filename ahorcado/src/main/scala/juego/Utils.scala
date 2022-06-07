package juegos

import org.apache.spark.sql.{DataFrame, SparkSession}


object Utils {

  /*

   */
  def ingestCSV(ruteInput: String, spark: SparkSession, delimiter: String): DataFrame = {
    val df: DataFrame = spark.read
      .format("csv")
      .option("delimiter", delimiter)
      .option("header", "true")
      .option("inferSchema", "true")
      .load(ruteInput);
    df;
  }

  def columnToList(df: DataFrame, columna: String): List[Any] = {
    df.select(columna).rdd.map(row => row(0)).collect().toList
  }
}
