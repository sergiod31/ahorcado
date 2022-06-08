package juego

import juegos.Utils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession


class Ahorcado {
  class Partida {
    var palabraString: String = ""
    var palabraArray: Array[Char] = null
    var intentos: Int = 0
    var intentosMax: Int = 0
    var estadoRespuesta: Array[Char] = null
    var historialRespuestas: Array[Char] = new Array[Char](0)

    def inicializarPartida(palabra: String, intentosMax: Int, estadoRespuesta: Array[Char]): Unit = {
      this.palabraString = palabra
      var palabraArray = palabra.toCharArray
      this.intentosMax = intentosMax
      this.estadoRespuesta = estadoRespuesta
    }
  }

  object juego {
    def main(args: Array[String]): Unit = {
      Logger.getLogger("org").setLevel(Level.ERROR)
      val spark: SparkSession = SparkSession.builder()
        .master("local")
        .appName("prueba")
        .getOrCreate();

      val direccionCSV = ""


      var listaPalabras = Utils.ingestCSV(direccionCSV, spark, ";")
        .select("ID", "PALABRA").rdd.map(row => (row(0), row(1)))
        .collect().toList.asInstanceOf[List[(Int, String)]];

      var volverAJugar = true
      do {
        // es la 1a vez que juega o repite:

        // inicializo las variables base para nueva partida
        val palabraNum = scala.util.Random.nextInt(listaPalabras.length - 1)
        val palabra = listaPalabras(palabraNum)._2
        val intentosMax = 5
        var partida = new Partida
        var estadoRespuesta = new Array[Char](palabra.length)
        for (i <- estadoRespuesta.indices) {
          estadoRespuesta(i) = '_'
        }
        partida.inicializarPartida(palabra, intentosMax, estadoRespuesta)
        // ya tengo el objeto "partida" inicializado

        siguienteTurno()


      } while (volverAJugar)
      // ha acabado y no quiere volver a jugar

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
   *
   * mas eficiente seria con un arbol rojonegro
   */
  def ordenarRespuestas(historialRespuestas: Array[Char], respuesta: Char): Array[Char] = {
    val respuestasOrdenadas = new Array[Char](historialRespuestas.length + 1)

    // en caso de que el array historial este vacio
    if (historialRespuestas.length == 0) {
      respuestasOrdenadas(0) = respuesta
      return respuestasOrdenadas
    }

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

  def siguienteTurno(partida: Partida, dibujos: Array[String]): Boolean = {
    // escribo
    imprimirPartida(dibujos(partida.intentos), partida.estadoRespuesta, partida.)
    // leo

    //compruebo


  }
}
