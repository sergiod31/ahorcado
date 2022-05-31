package sample

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import scala.io.StdIn.readLine

object adivinanzas {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val spark: SparkSession = SparkSession.builder()
      .master("local")
      .appName("prueba")
      .getOrCreate();

    // directorios
    val file1: String = "C:/Users/sdominso/Downloads/Adivinanzas.csv"
    val file2: String = "C:\\Users\\sdominso\\Downloads\\SolucionesAdivinanzas.csv"


    val listaPreguntas: List[(Int, String)] = ingestCSV(file1, spark, ";")
      .select("ID_ADIVINANZA", "ADIVINANZA")
      .rdd.map(row => (row(0), row(1))).collect().toList.asInstanceOf[List[(Int, String)]];

    val listaRespuestas: List[(Int, String)] = ingestCSV(file2, spark, ";")
      .select("ID_ADIVINANZA", "SOLUCION")
      .rdd.map(row => (row(0), row(1))).collect().toList.asInstanceOf[List[(Int, String)]];

    var correctas: Double = 0.0
    var incorrectas: Double = 0.0

    for (i <- 0 to listaPreguntas.length - 1) {
      if (hacerPregunta(listaPreguntas(i)._2, listaRespuestas(i)._2)) {
        correctas += 1
      } else {
        incorrectas += 1
      }
    }
    println(s"Tu porcentaje de aciertos es del ${100 * correctas / (correctas + incorrectas)}%")
  }

  def hacerPregunta(pregunta: String, respuesta: String): Boolean = {
    println(pregunta)
    val respuestaUsuario: String = readLine()
    if (respuestaUsuario.toLowerCase().
      equals(respuesta.toLowerCase())) {
      println("Muy bien!!")
      print("\n")
      return true
    } else {
      println(s"Oh no!, la respuesta correcta es: ${respuesta}")
      print("\n")
      return false
    }

  }

  def ingestCSV(ruteInput: String, spark: SparkSession, delimiter: String): DataFrame = {
    val df: DataFrame = spark.read
      .format("csv")
      .option("delimiter", delimiter)
      .option("header", "true")
      .option("inferSchema", "true")
      .load(ruteInput);
    df;
  }
}
