import Main.{C, Cerberus, G, K, Malina, Modeus, P, Pandemonica, R, S, W, Zdrada}

/**
 * Représente un niveau du jeu.
 *
 * @param grid Grille du niveau, sous forme de tableau 2D d'entiers (chaque entier représente un type d'entité).
 * @param maxMoves Nombre maximum de déplacements autorisés dans ce niveau.
 */
class Level(val grid: Array[Array[Int]], val trapsGrid: Array[Array[Int]], val maxMoves: Int, val movableSpikes: Boolean = false, val offsetX:Int, val offsetY:Int, val Demon: EntityRender, val backgroundPath: String = "") {
  val gridWidth: Int = grid.length
  val gridHeight: Int = grid(0).length
  var currentMoves: Int = 0
  var hasKey: Boolean = false
}

/**
 * Companion object for the Level class, providing a method to initialize levels.
 */
object Level {
  def initializeLevels(screenWidth: Int, gridWidth: Int, tileSize: Int): List[Level] = {
    List(
      // Level 1
      new Level(
        Array( // Un level du vrai jeu, vraiment chiant à recopier si on veut en faire plus
          Array(W, W, W, W, W, W, W, W),
          Array(W, W, W, W, 0, 0, 0, W),
          Array(W, W, 0, 0, 0, R, R, W),
          Array(W, W, 0, S, W, 0, 0, W),
          Array(W, W, S, 0, W, 0, R, W),
          Array(W, 0, 0, S, W, R, 0, W),
          Array(W, P, 0, W, W, 0, 0, W),
          Array(W, W, W, W, W, W, G, W),
          Array(W, W, W, W, W, W, W, W)

        ),
        Array(
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0)
        )
        ,22 ,true,(screenWidth - (gridWidth * tileSize)) / 2 - 90,75,Pandemonica, "/res/level 1.png",
      ),

      // Level 2
      new Level(
        Array(
          Array(W, W, W, W, W, W, W, W),
          Array(W, W, W, 0, 0, P, W, W),
          Array(W, 0, S, 0, 0, 0, W, W),
          Array(W, 0, W, W, W, W, W, W),
          Array(W, 0, 0, W, W, W, W, W),
          Array(W, 0, 0, R, 0, 0, G, W),
          Array(W, W, 0, R, 0, S, 0, W),
          Array(W, W, 0, R, 0, 0, S, W),
          Array(W, W, W, W, W, W, W, W)



        ),
        Array(
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0,-1, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0,-1, 0, 0, 0, 0, 0),
          Array(0, 0,-1,-1, 0, 0, 0, 0),
          Array(0, 0, 0,-1,-1, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0)
        ), 22 ,false,(screenWidth - (gridWidth * tileSize)) / 2 - 95,85,Modeus, "/res/level 2.png"
      ),

      // Level 3
      new Level(
        Array(
          Array(W, W, W, W, W, W, W, W, W),
          Array(W, W, W, W, W, W, K, 0, W),
          Array(W, W, W, W, W, W, W, 0, W),
          Array(W, W, W, 0, 0, 0, 0, 0, W),
          Array(W, G, W, 0, W, 0, W, 0, W),
          Array(W, G, W, 0, 0, S, 0, 0, W),
          Array(W, G, W, 0, W, 0, W, S, W),
          Array(W, 0, C, 0, 0, 0, 0, 0, W),
          Array(W, W, W, P, 0, W, W, W, W),
          Array(W, W, W, W, W, W, W, W, W)
        ),
        Array(

          Array(0, 0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0,-1, 0,-1, 0, 0),
          Array(0, 0, 0,-1, 0, 0, 0, 0, 0),
          Array(0, 0, 0,-1,-1, 0,-1, 0, 0),
          Array(0, 0, 0, 0, 0, 0,-1, 0, 0),
          Array(0, 0, 0, 0, 0, 0,-1, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0, 0)

        ), 31 ,false,(screenWidth - (gridWidth * tileSize)) / 2 - 117,65,Cerberus, "/res/level 3.png"
      ) ,

      // Level 4
      new Level(
        Array(
          Array(W, W, W, W, W, W, W),
          Array(W, P, 0, R, 0, W, W),
          Array(W, W, R, 0, R, 0, W),
          Array(W, K, 0, R, 0, R, W),
          Array(W, 0, R, 0, R, 0, W),
          Array(W, R, 0, R, 0, R, W),
          Array(W, W, C, R, R, 0, W),
          Array(W, W, 0, 0, R, W, W),
          Array(W, W, W, G, 0, W, W),
          Array(W, W, W, W, W, W, W)
        ),
        Array(
          Array(0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0),
          Array(0, 0,-1, 0, 0, 0, 0),
          Array(0, 0,-1, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0)

        ), 23 ,false,(screenWidth - (gridWidth * tileSize)) / 2 - 117,110,Malina, "/res/level 4.png"
      ),

      // Level 5
      new Level(
        Array(
          Array(W, W, W, W, W, W, W, W, W, W),
          Array(W, W, W, W, P, 0, S, 0, W, W),
          Array(W, W, W, W, W, W, W, 0, W, W),
          Array(W, W, W, 0, 0, 0, R, 0, W, W),
          Array(W, W, 0, C, 0, 0, R, 0, W, W),
          Array(W, W, G, R, R, 0, R, 0, W, W),
          Array(W, W, W, 0, 0, 0, R, 0, K, W),
          Array(W, W, W, W, W, W, W, W, W, W)
        ),
        Array(
          Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0,-1, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0,-1, 0, 0,-1, 0, 0),
          Array(0, 0, 0, 0, 0,-1, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
          Array(0, 0, 0, 0, 0,-1, 0,-1, 0, 0),
          Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        ), 23 ,true,(screenWidth - (gridWidth * tileSize)) / 2 - 70 , 10,Zdrada, "/res/level 5.png",
      ),

      // Credits
      new Level(

        Array(
          Array(W,W,W,W,W),
          Array(W,0,W,0,W),
          Array(W,W,P,W,W),
          Array(W,0,W,0,W),
          Array(W,W,W,W,W)

        ),
        Array(
          Array(0,0,0,0,0),
          Array(0,0,0,0,0),
          Array(0,0,0,0,0),
          Array(0,0,0,0,0),
          Array(0,0,0,0,0)

        )

        , 43, false, (screenWidth - (gridWidth * tileSize)) / 2 , 200, Zdrada, "/res/EndScreen.png"


      )
    )
  }
}

