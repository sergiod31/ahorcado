package juego

import juegos.Utils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import scala.util.matching.Regex
import scala.io.StdIn.readLine


object juego {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val spark: SparkSession = SparkSession.builder()
      .master("local")
      .appName("prueba")
      .getOrCreate();

    val direccionCSV = "C:\\Users\\sdominso\\Documents\\GitHub\\ahorcado\\src\\main\\resources\\words.csv"


    val listaPalabras = Utils.ingestCSV(direccionCSV, spark, ";")
      .select("ID", "PALABRA").rdd.map(row => (row(0), row(1)))
      .collect().toList.asInstanceOf[List[(Int, String)]];

    var volverAJugar = true
    do {
      // es la 1a vez que juega o repite:
      var partida = inicializarPartida(listaPalabras)
      if (siguienteTurno(partida, dibujos, listaPalabras, 0)) {
        // gano
        println("Ganaste!!")
      } else {
        // perdio
        print(s"Fallaste!! La palabra era '${partida.palabraString}''")
      }
      println("¿Echamos otra? Y/N")

      var respuesta = readLine()
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

  def inicializarPartida(listaPalabras: List[(Int, String)]): Partida = {
    var partida = new Partida

    // inicializo las variables base para nueva partida
    val intentosMax = dibujos.length - 1
    val simboloIncognita = '_'
    //
    val palabraNum = scala.util.Random.nextInt(listaPalabras.length - 1)
    val palabra = listaPalabras(palabraNum)._2.toLowerCase
    var estadoRespuesta = new Array[Char](palabra.length)
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

  /*
   * se que no es el algoritmo mas eficiente,
   * pero tampoco vamos a ejecutar esto 1 millon de veces/segundo
   *
   * mas eficiente seria con un arbol rojonegro
   *
   * despues de rehacer tantas veces este bloque porque no me deja modificar
   * el valor del iterador de un for, me he decidido hacerlo a lo burro
   */
  def ordenarRespuestas(historialRespuestas: Array[Char], respuesta: Char): Array[Char] = {
    val respuestasOrdenadas:Array[Char] = new Array[Char](historialRespuestas.length + 1)

    // si historialRespuestas es un array de length 0
    if (historialRespuestas.length <= 0) {
      respuestasOrdenadas(0) = respuesta
      return respuestasOrdenadas
    }

    respuestasOrdenadas(respuestasOrdenadas.length - 1) = respuesta

    var haCambiado = true
    while (haCambiado) {
      haCambiado = false
      for (i <- 0 to respuestasOrdenadas.length - 2) {
          if(respuestasOrdenadas(i) > respuestasOrdenadas(i + 1)){
            haCambiado = true
            val aux:Char = respuestasOrdenadas(i)
            respuestasOrdenadas(i) = respuestasOrdenadas(i + 1)
            respuestasOrdenadas(i + 1) = aux
          }
      }
    }
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
    var respuesta: String = leerEntrada()
    while (comprobarRespuestaEnHistorial(partida.historialRespuestas, respuesta.toLowerCase.charAt(0))) {
      println("Ya ha introducido esa letra")
      respuesta = leerEntrada()
    }

    val letra = respuesta.toLowerCase.charAt(0)
    //actualizo la partida
    var acerto = false
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
    var respuesta = ""
    var inputOk = true
    do {
      inputOk = true
      respuesta = readLine()
      val patternLetra: Regex = "[a-zA-ZñÑ]".r
      if (respuesta.length > 1 || respuesta.length <= 0 ||
        !patternLetra.pattern.matcher(respuesta).find()) {
        println("mas de una letra")
        inputOk = false
      }
    } while (!inputOk)
    respuesta
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
      return true
    }
    false
  }
}
