import hevs.graphics.FunGraphics

import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Color, Font}
import javax.sound.sampled.{AudioSystem, Clip}
import javax.swing.{SwingConstants, Timer}

/**
 * Représente un niveau du jeu.
 *
 * @param grid Grille du niveau, sous forme de tableau 2D d'entiers (chaque entier représente un type d'entité).
 * @param maxMoves Nombre maximum de déplacements autorisés dans ce niveau.
 */
class Level(val grid: Array[Array[Int]], val trapsGrid: Array[Array[Int]], val maxMoves: Int, val movableSpikes: Boolean = false, val offsetX:Int, val offsetY:Int, val demonPath: String, val backgroundPath: String = "") {
  val gridWidth: Int = grid.length
  val gridHeight: Int = grid(0).length
  var currentMoves: Int = 0
  var hasKey: Boolean = false
}

object Main {
  private var currentLevelIndex: Int = 0
  private var levels: List[Level] = _
  private var world: Array[Array[Int]] = _
  private var trapWorld: Array[Array[Int]] = _
  private var gridWidth: Int = 5
  private var gridHeight: Int = 5

  // New arrays for storing the previous frame’s state
  private var oldWorld: Array[Array[Int]] = _
  private var oldTrapWorld: Array[Array[Int]] = _

  // Keep track of the previous frame’s “moves left” to redraw only if changed
  private var oldMovesLeft: Int = -1

  private val playerImagePath = "/res/Mudry.png"
  private val skeletonPath = "/res/skeleton.png"
  private val rockPath = "/res/rock.png"

  private val keyPath = "/res/key.png"
  private val chestPath = "/res/chest.png"
  private val spikeUpPath = "/res/spikeup.png"
  private val spikeDownPath = "/res/spikedown.png"

  private val screenWidth = 900
  private val screenHeight = 563

  private val tileSize = 47


  // Création de la fenêtre graphique
  private val fg = new FunGraphics(screenWidth, screenHeight, "ISC TAKER")


  // Codes associés aux différentes entités du jeu
  private val P = 1  // Joueur
  private val W = 2  // Mur
  private val G = 3  // But (Goal)
  private val S = 4  // Squelette
  private val R = 5  // Rocher
  private val C = 6  // Coffre
  private val K = 8  // Clé


  private var timer : Timer = _

  // Position initiale du joueur
  private var playerPos: (Int, Int) = (0, 0)

  def main(args: Array[String]): Unit = {
    try {
      val audioInputStream = AudioSystem.getAudioInputStream(
        getClass.getResourceAsStream("/res/Mittsies-Vitality.wav")
      )
      val clip = AudioSystem.getClip
      clip.open(audioInputStream)
      clip.loop(Clip.LOOP_CONTINUOUSLY)
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        println("Could not load or play the background music.")
    }

    // Initialisation de la liste des niveaux
    levels = List(

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
        ,23 ,true,(screenWidth - (gridWidth * tileSize)) / 2 - 90,75,"/res/Pandemonia.gif", "/res/level 1.png",
      ),
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
        ), 24 ,false,(screenWidth - (gridWidth * tileSize)) / 2 - 95,85,"/res/Modeus.gif", "/res/level 2.png"
      ),
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

        ), 32 ,false,(screenWidth - (gridWidth * tileSize)) / 2 - 117,65,"/res/Cerberus.gif", "/res/level 3.png"
      ) ,
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

        ), 23 ,false,(screenWidth - (gridWidth * tileSize)) / 2 - 117,110,"/res/Malina.gif", "/res/level 4.png"
      ),
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
        ), 23 ,true,(screenWidth - (gridWidth * tileSize)) / 2 - 70 , 10,"/res/Zdrada.gif", "/res/level 5.png"
      )
    )

    // Chargement du premier niveau
    loadLevel(currentLevelIndex)

    // Démarre un timer pour redessiner le monde périodiquement
    timer = new Timer(10, _ => renderWorld(false))
    timer.start()

    // Gestion des événements clavier
    fg.setKeyManager(new KeyAdapter() {
      override def keyPressed(e: KeyEvent): Unit = {
        e.getKeyCode match {
          case KeyEvent.VK_UP    => handlePlayerInput(0, -1)
            renderWorld(false)
          case KeyEvent.VK_DOWN   => handlePlayerInput(0, 1)
            renderWorld(false)
          case KeyEvent.VK_LEFT   => handlePlayerInput(-1, 0)
            renderWorld(false)
          case KeyEvent.VK_RIGHT  => handlePlayerInput(1, 0)
            renderWorld(false)
          case KeyEvent.VK_R      => loadLevel(currentLevelIndex) // Réinitialise le niveau
            renderWorld(false)
          case KeyEvent.VK_L      => renderAdvice()
          case KeyEvent.VK_ESCAPE => renderWorld(true)
          case _ => // Aucune action pour les autres touches
        }
      }
    })
  }

  /**
   * Charge un niveau à partir d'un index donné.
   *
   * @param levelIndex L'index du niveau à charger dans la liste des niveaux.
   */
  private def loadLevel(levelIndex: Int): Unit = {
    val level = levels(levelIndex)
    gridWidth = level.gridWidth
    gridHeight = level.gridHeight

    world = level.grid.map(_.clone())

    // 2) Copy the traps layer
    trapWorld = level.trapsGrid.map(_.clone())

    // 3) oldWorld / oldTrapWorld for comparison
    oldWorld    = Array.fill(gridWidth, gridHeight)(-999)
    oldTrapWorld= Array.fill(gridWidth, gridHeight)(-999)

    // Find player and reset moves, key, etc.
    playerPos = findPlayer(world)
    level.currentMoves = 0
    level.hasKey = false

    // Force re-drawing of moves text next time
    oldMovesLeft = -1
  }

  /**
   * Parcourt la grille pour trouver la position (x, y) du joueur.
   *
   * @param grid Grille de jeu.
   * @return Tuple (x, y) représentant la position du joueur.
   */
  private def findPlayer(grid: Array[Array[Int]]): (Int, Int) = {
    for (x <- grid.indices; y <- grid(x).indices) {
      if (grid(x)(y) == P) return (x, y)
    }
    throw new Exception("Joueur non trouvé dans la grille !")
  }

  /**
   * Gère les déplacements du joueur et les interactions avec l'environnement.
   *
   * @param dx Déplacement en X (gauche/droite).
   * @param dy Déplacement en Y (haut/bas).
   */
  private def handlePlayerInput(dx: Int, dy: Int): Unit = {
    val level = levels(currentLevelIndex)

    // Vérifie si le joueur a déjà épuisé tous ses déplacements
    if (level.currentMoves == level.maxMoves) {
      println("Mouvements épuisés ! Redémarrage du niveau.")
      loadLevel(currentLevelIndex)
      return
    }

    val (x, y) = playerPos
    val newX = x + dx
    val newY = y + dy

    // Vérifie si la case ciblée est accessible ou non (ni mur, ni squelette, etc.)
    if (isValidMove(newX, newY)) {
      // Si le joueur récupère une clé
      if (world(newX)(newY) == K) {
        level.hasKey = true
        println("Clé récupérée !")
      }
      // Vérifie si on marche sur un piège "vivant" (1)
      if (trapWorld(newX)(newY) == -1) {
        println("Ouch! Piège actif : vous perdez un déplacement supplémentaire.")
        level.currentMoves += 1 // Pénalité
      }

      // Effectue le mouvement du joueur
      movePlayer(newX, newY)
      level.currentMoves += 1

      // Inverse l'état des pièges après chaque déplacement
      toggleTraps(level)

    } else if (world(newX)(newY) == C && level.hasKey) {
      // Ouvrir le coffre si on a la clé
      println("Coffre ouvert !")
      destroyEntity(newX, newY)

    } else if (world(newX)(newY) == S) {
      // S'il y a un squelette, on tente de le pousser
      val entityNewX = newX + dx
      val entityNewY = newY + dy

      // Vérifie si la position cible est dans les limites du tableau
      if (entityNewX >= 0 && entityNewX < gridWidth && entityNewY >= 0 && entityNewY < gridHeight) {
        // S'il y a un mur ou un rocher à l'endroit où on veut pousser le squelette, on le détruit.
        if (world(entityNewX)(entityNewY) == W || world(entityNewX)(entityNewY) == R) {
          println("Squelette détruit !")
          destroyEntity(newX, newY)
        }
        // Sinon, s'il n'y a rien ou bien un piège, on déplace le squelette
        else if (world(entityNewX)(entityNewY) == 0 || trapWorld(entityNewX)(entityNewY) != 0) {
          moveEntity(newX, newY, entityNewX, entityNewY)
          level.currentMoves += 1
          toggleTraps(level)
        }
      }

    } else if (world(newX)(newY) == R) {
      // Gestion du déplacement des rochers
      val entityNewX = newX + dx
      val entityNewY = newY + dy

      // Vérifie si le rocher peut être poussé
      if (entityNewX < 0 || entityNewX >= gridWidth || entityNewY < 0 || entityNewY >= gridHeight) {
        // Destination hors limites → on ne pousse pas
      } else {
        // On regarde ce qui se trouve à l'endroit où on souhaite pousser le rocher
        world(entityNewX)(entityNewY) match {
          // Si la destination est un mur, un rocher ou un coffre, on ne peut pas pousser
          case W | R | C | S =>
            println("Rocher bloqué par un mur/rocher !")
            level.currentMoves += 1
            toggleTraps(level)

          // S'il n'y a rien ou un piège, on peut pousser
          case _ =>
            moveEntity(newX, newY, entityNewX, entityNewY)
            level.currentMoves += 1
            toggleTraps(level)
        }
      }
    }
    // Vérifie si on a terminé le niveau
    checkLevelCompletion()
  }

  /**
   * Change l'état de chaque piège : d'actif (-1) à inactif (1) et vice-versa.
   */
  private def toggleTraps(level: Level ): Unit = {
    for (x <- 0 until gridWidth; y <- 0 until gridHeight) {
      if (trapWorld(x)(y) == 1 && level.movableSpikes) trapWorld(x)(y) = -1
      else if (trapWorld(x)(y) == -1 && level.movableSpikes) trapWorld(x)(y) = 1
    }
  }

  /**
   * Déplace le joueur vers de nouvelles coordonnées (newX, newY).
   */
  private def movePlayer(newX: Int, newY: Int): Unit = {
    val (x, y) = playerPos
    world(x)(y) = 0
    world(newX)(newY) = P
    playerPos = (newX, newY)
  }

  /**
   * Déplace une entité quelconque (squelette, rocher, etc.) vers de nouvelles coordonnées.
   */
  private def moveEntity(oldX: Int, oldY: Int, newX: Int, newY: Int): Unit = {
    val entity = world(oldX)(oldY)
    world(oldX)(oldY) = 0
    world(newX)(newY) = entity
  }

  /**
   * Détruit (supprime) une entité sur la grille.
   */
  private def destroyEntity(x: Int, y: Int): Unit = {
    world(x)(y) = 0
  }

  /**
   * Vérifie si le joueur peut se déplacer sur la case (x, y).
   */
  private def isValidMove(x: Int, y: Int): Boolean = {
    x >= 0 && x < gridWidth &&
      y >= 0 && y < gridHeight &&
      world(x)(y) != W &&  // Bloqué par un mur
      world(x)(y) != S &&  // Bloqué par un squelette
      world(x)(y) != R &&  // Bloqué par un rocher
      world(x)(y) != C     // Bloqué par un coffre fermé
  }


  private def arraysEqual2D(a: Array[Array[Int]], b: Array[Array[Int]]): Boolean = {
    if (a.length != b.length) return false
    for (i <- a.indices) {
      if (a(i).length != b(i).length) return false
      if (!a(i).sameElements(b(i))) return false
    }
    true
  }

  /**
   * Affiche la grille, ses entités et l'état des pièges à l'écran.
   */
  def renderWorld(forceRender: Boolean): Unit = {
    val level = levels(currentLevelIndex)
    val movesLeft = level.maxMoves - level.currentMoves

    if(!arraysEqual2D(world, oldWorld) || !arraysEqual2D(trapWorld, oldTrapWorld) || forceRender) {
      fg.drawTransformedPicture(
        posX   = screenWidth / 2,
        posY   = screenHeight / 2,
        angle  = 0.0,
        scale  = 1,
        imageName = level.backgroundPath
      )

      for (i <- 0 until gridWidth; j <- 0 until gridHeight) {

        // 2) Re-draw the trap if needed
        if (trapWorld(i)(j) == 1) {
          if(!level.movableSpikes){
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.1,
              imageName = spikeDownPath
            )
          }
          else{
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.1,
              imageName = spikeUpPath
            )
          }
        }
        else if (trapWorld(i)(j) == -1) {
          if(!level.movableSpikes) {
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.1,
              imageName = spikeUpPath
            )
          }
          else{
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.1,
              imageName = spikeDownPath
            )
          }
        }


        // 3) Re-draw the actual entity
        world(i)(j) match {
          case P =>
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.7,
              imageName = playerImagePath
            )
          case W =>

          case G =>
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.8,
              imageName = level.demonPath
            )
          case S =>
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.6,
              imageName = skeletonPath
            )
          case R =>
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.11,
              imageName = rockPath
            )
          case C =>
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.1,
              imageName = chestPath
            )
          case K =>
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.1,
              imageName = keyPath
            )
          case _ =>
          // 0 means empty, so we do nothing because the tile is already white
        }
        // 5) Update oldWorld and oldTrapWorld
        oldWorld(i)(j) = world(i)(j)
        oldTrapWorld(i)(j) = trapWorld(i)(j)
      }

    }
    // Loop through the grid


    // Finally, update the number of moves left on screen only if it changed
    if (movesLeft != oldMovesLeft) {
      // Clear the old text region. For example, assume we show it near top-left:
      fg.setColor(Color.BLACK)
      fg.drawFillRect(50,380,100,60)

      // Now draw the new text
      fg.setColor(Color.WHITE)
      fg.drawFancyString(
        posX    = 70,
        posY    = 425,
        str = s"$movesLeft",
        fontFamily = "Arial",
        fontStyle = Font.BOLD,
        fontSize = 50,
        color = Color.WHITE,
        halign =  SwingConstants.LEFT,
        valign = SwingConstants.BOTTOM,
        shadowX = 0,
        shadowY = 0,
        shadowColor = Color.BLACK,
        shadowThickness = 10,
        outlineColor = Color.BLACK,
        outlineThickness = 20





      )

      // Store the new movesLeft
      oldMovesLeft = movesLeft
    }
  }

  /**
   * Vérifie si le joueur a atteint le but (G) en étant adjacent à celui-ci.
   * Si oui, charge le niveau suivant ou termine la partie si tous les niveaux sont finis.
   */
  private def checkLevelCompletion(): Unit = {
    val (px, py) = playerPos

    // Vérifie si le joueur est adjacent à la case G
    val goalAdjacent =
      (px > 0 && world(px - 1)(py) == G) ||
        (px < gridWidth - 1 && world(px + 1)(py) == G) ||
        (py > 0 && world(px)(py - 1) == G) ||
        (py < gridHeight - 1 && world(px)(py + 1) == G)

    if (goalAdjacent) {
      currentLevelIndex += 1
      if (currentLevelIndex < levels.length) {
        loadLevel(currentLevelIndex)
      } else {
        println("Félicitations ! Vous avez terminé tous les niveaux !")
        timer.stop()

        // Ici, on pourrait stopper le jeu ou lancer une autre séquence.
      }
    }
  }

  def renderAdvice(): Unit = {
    fg.setColor(Color.BLACK)
    fg.drawFillRect(50,50,800,400)
    fg.setColor(Color.WHITE)
    fg.drawString(70,100,"ISC TAKER",Color.WHITE,50)
    fg.drawString(70,150,"Déplacez-vous avec les touches fléchées.",Color.WHITE,20)
    fg.drawString(70,200,"Récupérez la clé pour ouvrir le coffre.",Color.WHITE,20)
    fg.drawString(70,250,"Poussez les rochers pour les déplacer.",Color.WHITE,20)
    fg.drawString(70,300,"Evitez les squelettes et les pièges.",Color.WHITE,20)
    fg.drawString(70,350,"Atteignez la case G pour terminer le niveau.",Color.WHITE,20)
  }
}
