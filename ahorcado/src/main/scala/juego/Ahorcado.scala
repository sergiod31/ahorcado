package juego

import juegos.Utils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import scala.io.StdIn.readLine


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


      val listaPalabras = Utils.ingestCSV(direccionCSV, spark, ";")
        .select("ID", "PALABRA").rdd.map(row => (row(0), row(1)))
        .collect().toList.asInstanceOf[List[(Int, String)]];

      var volverAJugar = true
      do {
        // es la 1a vez que juega o repite:
        var partida = inicializarPartida(listaPalabras)
        if (siguienteTurno(partida, dibujos, listaPalabras)) {
          // gano
          println("Ganaste!!")
        } else {
          // perdio
          print(s"Fallaste!! La palabra era '${partida.palabraString}''")
        }
        println("¿Echamos otra? Y/N")

        var respuesta = readLine()
        while (!respuesta.equals("Y") ||
          !respuesta.equals("y") ||
          !respuesta.equals("N") ||
          !respuesta.equals("n")) {
          print("Escriba 'Y' o 'N' por favor")
        }
        if (respuesta.equals("Y") ||
          respuesta.equals("y")) {
          volverAJugar = true
        }else{
          volverAJugar = false
        }
      } while (volverAJugar)
      // ha acabado y no quiere volver a jugar
      System.exit(0)
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

  def inicializarPartida(listaPalabras: List[(Int, String)]): Partida = {
    var partida = new Partida

    // inicializo las variables base para nueva partida
    val intentosMax = 5
    val simboloIncognita = '_'
    //
    val palabraNum = scala.util.Random.nextInt(listaPalabras.length - 1)
    val palabra = listaPalabras(palabraNum)._2
    var estadoRespuesta = new Array[Char](palabra.length)
    for (i <- estadoRespuesta.indices) {
      estadoRespuesta(i) = simboloIncognita
    }
    partida.inicializarPartida(palabra, intentosMax, estadoRespuesta)
    // ya tengo el objeto "partida" inicializado
    partida

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
  def comprobarRespuestaEnHistorial(historialRespuestas: Array[Char], respuesta: Char): Boolean = {
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

  /*
   * si gana, retorna true
   * si pierde, retorna false
   * si aun queda por jugar, caso recursivo
   */
  def siguienteTurno(partida: Partida, dibujos: Array[String], listaPalabras: List[(Int, String)]): Boolean = {
    // escribo
    imprimirPartida(dibujos(partida.intentos), partida.estadoRespuesta,
      partida.historialRespuestas, partida.intentos, partida.intentosMax)

    // leo
    var respuesta = readLine()
    var inputOk = true
    val letra = respuesta.charAt(0)
    do {
      var inputOk = true
      if (respuesta.length > 1) {
        println("Solo una letra, por favor")
        respuesta = readLine()
        inputOk = false
      }
      if ((letra < 'A' || letra > 'Z') &&
        (letra < 'a' || letra > 'z')) {
        println("Introduzca una letra, no un símbolo, por favor")
        respuesta = readLine()
        inputOk = false
      }
    } while (!inputOk)


    //actualizo la partida
    var acerto = false
    for (i <- partida.palabraArray) {
      if (partida.palabraArray(i).equals(letra)) {
        acerto = true
        partida.estadoRespuesta(i) = letra
      }
      if (!acerto) {
        partida.intentos += 1
      }
      partida.historialRespuestas = ordenarRespuestas(partida.historialRespuestas, letra)
    }
    if (partida.intentos >= partida.intentosMax) {
      // perdio :(
      return false
    }
    //continua
    if (comprobarVictoria(partida.estadoRespuesta)) {
      // gano :)
      true
    } else {
      siguienteTurno(partida, dibujos, listaPalabras)
    }
  }

  def comprobarVictoria(estadoRespuesta: Array[Char]): Boolean = {
    for (i <- estadoRespuesta) {
      if (estadoRespuesta(i).equals('_')) {
        return false
      }
    }
    true
  }

  def repetir(): Boolean = {
    println("¿Echamos otra? Y/N")
    var respuesta = ""
    do {
      respuesta = readLine()
    } while (!respuesta.equals("y") &&
      !respuesta.equals("Y") &&
      !respuesta.equals("n") &&
      !respuesta.equals("N"))
    if (respuesta.equals("Y") || respuesta.equals("y")) {
      true
    }
    false
  }
}
