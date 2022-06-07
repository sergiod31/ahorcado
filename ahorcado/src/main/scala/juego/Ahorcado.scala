package juego

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

import scala.collection.BitSet.empty.to


class Ahorcado {

  object juego {
    def main(args: Array[String]): Unit = {
      Logger.getLogger("org").setLevel(Level.ERROR)
      val spark: SparkSession = SparkSession.builder()
        .master("local")
        .appName("prueba")
        .getOrCreate();
    }

    var dibujos: Array[String] = new Array[String](10)
    dibujos(0) =
      ""
    dibujos(1) =
      "________||______"
    dibujos(2) =
      "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "________||______"
    dibujos(3) =
      "__________\n" +
        "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "________||______"
    dibujos(4) =
      "__________\n" +
        "   |    ||\n" +
        "   |    ||\n" +
        "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "________||______"
    dibujos(5) =
      "__________\n" +
        "   |    ||\n" +
        "   |    ||\n" +
        "   ◯    ||\n" +
        "        ||\n" +
        "        ||\n" +
        "        ||\n" +
        "________||______"
    dibujos(6) =
      "__________\\n\"" +
        "   |    ||\\n\"" +
        "   |    ||\\n\"" +
        "   ◯    ||\\n\"" +
        "   |    ||\\n\"" +
        "   |    ||\\n\"" +
        "        ||\\n\"" +
        "________||______"
    dibujos(7) =
      "__________\n" +
        "   |    ||\n" +
        "   |    ||\n" +
        "   ◯    ||\n" +
        "  /|    ||\n" +
        "   |    ||\n" +
        "        ||\n" +
        "________||______"
    dibujos(8) =
      "__________\n" +
        "   |    ||\n" +
        "   |    ||\n" +
        "   ◯    ||\n" +
        "  /|\\   ||\n" +
        "   |    ||\n" +
        "        ||\n" +
        "________||______"
    dibujos(9) =
      "__________\n" +
        "   |    ||\n" +
        "   |    ||\n" +
        "   ◯    ||\n" +
        "  /|\\   ||\n" +
        "   |    ||\n" +
        "  /     ||\n" +
        "________||______"
    dibujos(10) =
      "__________\n" +
        "   |    ||\n" +
        "   |    ||\n" +
        "   ◯    ||\n" +
        "  /|\\   ||\n" +
        "   |    ||\n" +
        "  / \\   ||\n" +
        "________||______"

  }

  /*
   * estadoRespuesta es un array estilo [R__A] para la palabra "RATA"
   */
  def imprimirPartida(dibujo: String,
                      estadoRespuesta: Array[Char],
                      historialRespuestas: Array[Char],
                      intentosRestantes: Int,
                      maximoIntentos: Int): Unit = {

    //
    println(dibujo)

    estadoRespuesta.foreach(e => {
      print(e)
    })

    println(s"Nº de errores: ${intentosRestantes}/${maximoIntentos}\n")

    print("\nLetras utilizadas: [")
    for (i <- 0 to historialRespuestas.length - 2) { // pinto todas las letras menos la ultima
      print("${historialRespuestas(i)}, ")
    }
    print(historialRespuestas(historialRespuestas.length - 1)) // pinto la ultima letra
    println("]")
    println("Introduce letra")
  }

  /*
   * si la respuesta ya habia sido metida previamente, retorna true
   */
  def comprobarRespuestaYaMetida(historialRespuestas: Array[Char], respuesta: Char): Boolean = {
    historialRespuestas.foreach(e => {
      if (e.equals(respuesta)) {
        true
      }
    })
    false
  }

  /*
   * se que no es el algoritmo mas eficiente,
   * pero tampoco vamos a ejecutar esto 1 millon de veces/segundo
   */
  def ordenarRespuestas(historialRespuestas: Array[Char], respuesta: Char): Array[Char] = {
    val respuestasOrdenadas = new Array[Char](historialRespuestas.length + 1)
    var ite = 0
    for (i <- 0 to historialRespuestas.length - 2) {
      if (historialRespuestas(i) < respuesta && respuesta < historialRespuestas(i + 1)) {
        // bingo
        respuestasOrdenadas(ite) = historialRespuestas(i)
        ite += 1
      }
      respuestasOrdenadas(ite) = respuesta
      ite += 1
    }

    // por si hay que meterlo al final del array o una antes
    if (historialRespuestas(historialRespuestas.length - 1) < respuesta) {
      // al final
      respuestasOrdenadas(respuestasOrdenadas.length - 1) = respuesta
      respuestasOrdenadas(respuestasOrdenadas.length - 2) = historialRespuestas(historialRespuestas.length - 1)
    } else {
      // una antes
      respuestasOrdenadas(respuestasOrdenadas.length - 2) = respuesta
      respuestasOrdenadas(respuestasOrdenadas.length - 1) = historialRespuestas(historialRespuestas.length - 1)
    }
    respuestasOrdenadas
  }



}
