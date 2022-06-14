package juego

import juegos.Utils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.matching.Regex
import scala.io.StdIn.readLine


object juego {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val spark: SparkSession = SparkSession.builder()
      .master("local")
      .appName("prueba")
      .getOrCreate();

    val direccionCSV: String = getClass.getClassLoader.getResource("words.csv").getPath

    val listaPalabras: List[(Int, String)] = Utils.ingestCSV(direccionCSV, spark, ";")
      .select("ID", "PALABRA").rdd.map(row => (row(0), row(1)))
      .collect().toList.asInstanceOf[List[(Int, String)]];

    var volverAJugar: Boolean = true
    do {
      // es la 1a vez que juega o repite:
      val partida: Partida = inicializarPartida(listaPalabras)
      if (siguienteTurno(partida, dibujos, listaPalabras, 0)) {
        // gano
        println("Ganaste!!")
      } else {
        // perdio
        print(s"Fallaste!! La palabra era '${partida.palabraString}''")
      }
      println("¿Echamos otra? Y/N")

      var respuesta: String = readLine()
      while (!respuesta.equals("Y") &&
        !respuesta.equals("y") &&
        !respuesta.equals("N") &&
        !respuesta.equals("n")) {
        print("Escriba 'Y' o 'N' por favor")
        respuesta = readLine()
      }
      if (respuesta.equals("Y") ||
        respuesta.equals("y")) {
        volverAJugar = true
      } else {
        volverAJugar = false
      }
    } while (volverAJugar)
    // ha acabado y no quiere volver a jugar
    System.exit(0)
  }


  var dibujos: Array[String] = new Array[String](11)

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
    "__________\n" +
      "   |    ||\n" +
      "   |    ||\n" +
      "   ◯    ||\n" +
      "   |    ||\n" +
      "   |    ||\n" +
      "        ||\n" +
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


  class Partida {
    var palabraString: String = ""
    var palabraArray: Array[Char] = null
    var intentos: Int = 0
    var intentosMax: Int = 0
    var estadoRespuesta: Array[Char] = null
    var historialRespuestas: Array[Char] = new Array[Char](0)

    def inicializarPartida(palabra: String, intentosMax: Int, estadoRespuesta: Array[Char]): Unit = {
      this.palabraString = palabra
      this.palabraArray = palabra.toCharArray
      this.intentosMax = intentosMax
      this.estadoRespuesta = estadoRespuesta
    }
  }


  final case class IllegalInputException(cause: Int) extends Exception() {
  }

  def inicializarPartida(listaPalabras: List[(Int, String)]): Partida = {
    val partida: Partida = new Partida

    // inicializo las variables base para nueva partida
    val intentosMax: Int = dibujos.length - 1
    val simboloIncognita: Char = '_'
    //
    val palabraNum: Int = scala.util.Random.nextInt(listaPalabras.length - 1)
    val palabra: String = listaPalabras(palabraNum)._2.toLowerCase
    val estadoRespuesta: Array[Char] = new Array[Char](palabra.length)
    for (i <- 0 to estadoRespuesta.indices.length - 1) {
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
    print("\n")
    estadoRespuesta.foreach(e => {
      print(e)
    })
    println("\n")
    println(s"Nº de errores: ${intentosRestantes}/${maximoIntentos}\n")

    if (historialRespuestas.length > 0) {
      print("Letras utilizadas: [")
      for (i <- 0 to historialRespuestas.length - 2) { // pinto todas las letras menos la ultima
        print(s"${historialRespuestas(i)}, ")
      }
      print(historialRespuestas(historialRespuestas.length - 1)) // pinto la ultima letra
      println("]")
    }

    println("Introduce letra")
  }

  /*
   * si la respuesta ya habia sido metida previamente, retorna true
   */
  def comprobarRespuestaEnHistorial(historialRespuestas: Array[Char], respuesta: Char): Boolean = {
    historialRespuestas.foreach(e => {
      if (e.equals(respuesta)) {
        return true
      }
    })
    false
  }


  def ordenarRespuestas(historialRespuestas: Array[Char], respuesta: Char): Array[Char] = {
    var respuestasOrdenadas: Array[Char] = new Array[Char](historialRespuestas.length + 1)
    for (i <- 0 to historialRespuestas.length - 1) {
      respuestasOrdenadas(i) = historialRespuestas(i)
    }
    respuestasOrdenadas(respuestasOrdenadas.length - 1) = respuesta
    respuestasOrdenadas = respuestasOrdenadas.sorted
    respuestasOrdenadas
  }

  /*
   * si gana, retorna true
   * si pierde, retorna false
   * si aun queda por jugar, caso recursivo
   */
  def siguienteTurno(partida: Partida, dibujos: Array[String], listaPalabras: List[(Int, String)], turno: Int): Boolean = {
    // escribo
    imprimirPartida(dibujos(partida.intentos), partida.estadoRespuesta,
      partida.historialRespuestas, partida.intentos, partida.intentosMax)

    // leo entrada
    var respuesta = ""
    try {
      respuesta = leerEntrada()
    } catch {
      case e: IllegalInputException =>
        if (e.cause == 1) {
          println("Solo un digito")
        }
        if (e.cause == 2) {
          println("Solo letras ")
        }
    }
    while (comprobarRespuestaEnHistorial(partida.historialRespuestas, respuesta.toLowerCase.charAt(0))) {
      println("Ya ha introducido esa letra")
      try {
        respuesta = leerEntrada()
      } catch {
        case e: IllegalInputException =>
          if (e.cause == 1) {
            println("Solo un digito")
          }
          if (e.cause == 2) {
            println("Solo letras ")
          }
      }

    }

    val letra: Char = respuesta.toLowerCase.charAt(0)
    //actualizo la partida
    var acerto: Boolean = false
    for (i <- 0 to partida.palabraArray.length - 1) {
      if (partida.palabraArray(i).equals(letra)) {
        acerto = true
        partida.estadoRespuesta(i) = letra
      }
    }
    if (!acerto) {
      partida.intentos += 1
    }
    partida.historialRespuestas = ordenarRespuestas(partida.historialRespuestas, letra)

    if (partida.intentos >= partida.intentosMax) {
      // perdio :(
      return false
    }
    //continua
    if (comprobarVictoria(partida.estadoRespuesta)) {
      // gano :)
      true
    } else {
      siguienteTurno(partida, dibujos, listaPalabras, turno + 1)
    }
  }

  def comprobarVictoria(estadoRespuesta: Array[Char]): Boolean = {
    for (i <- 0 to estadoRespuesta.length - 1) {
      if (estadoRespuesta(i).equals('_')) {
        return false
      }
    }
    true
  }

  def leerEntrada(): String = {
    var respuesta: String = ""
    var inputOk: Boolean = true
    do {
      inputOk = true
      respuesta = readLine()
      val patternLetra: Regex = "[a-zA-ZñÑ]".r
      if (respuesta.length > 1 || respuesta.length <= 0) {
        throw IllegalInputException(1)
      }
      if (!patternLetra.pattern.matcher(respuesta).find()) {
        throw IllegalInputException(2)
      }
    } while (!inputOk)
    respuesta
  }

  def repetir(): Boolean = {
    println("¿Echamos otra? Y/N")
    var respuesta: String = ""
    do {
      respuesta = readLine()
    } while (!respuesta.equals("y") &&
      !respuesta.equals("Y") &&
      !respuesta.equals("n") &&
      !respuesta.equals("N"))
    if (respuesta.equals("Y") || respuesta.equals("y")) {
      return true
    }
    false
  }
}
