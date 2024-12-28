import hevs.graphics.FunGraphics
import hevs.graphics.utils.GraphicsBitmap

import java.awt.Color
import java.awt.event.{KeyAdapter, KeyEvent}
import javax.swing.Timer

/**
 * Représente un niveau du jeu.
 *
 * @param grid Grille du niveau, sous forme de tableau 2D d'entiers (chaque entier représente un type d'entité).
 * @param maxMoves Nombre maximum de déplacements autorisés dans ce niveau.
 */
class Level(val grid: Array[Array[Int]], val maxMoves: Int) {
  val gridWidth: Int = grid.length
  val gridHeight: Int = grid(0).length
  var currentMoves: Int = 0
  var hasKey: Boolean = false
  val trapWorld: Array[Array[Int]] = Array.fill(gridWidth, gridHeight)(0) // Suivi de l'état des pièges
}

object Main {
  var currentLevelIndex: Int = 0
  var levels: List[Level] = _
  var world: Array[Array[Int]] = _
  var trapWorld: Array[Array[Int]] = _
  var gridWidth: Int = 5
  var gridHeight: Int = 5

  val screenWidth = 1440
  val screenHeight = 720

  var tileSize = 60
  // Permet de centrer la grille sur l'écran
  val offsetX = (screenWidth - (gridWidth * tileSize)) / 2
  val offsetY = 0

  // Création de la fenêtre graphique
  val fg = new FunGraphics(screenWidth, screenHeight, "ISC TAKER")

  // Codes associés aux différentes entités du jeu
  val P = 1  // Joueur
  val W = 2  // Mur
  val G = 3  // But (Goal)
  val S = 4  // Squelette
  val R = 5  // Rocher
  val C = 6  // Coffre
  val T = 7  // Piège (Trap)
  val K = 8  // Clé

  // Position initiale du joueur
  var playerPos = (0, 0)

  def main(args: Array[String]): Unit = {
    // Initialisation de la liste des niveaux
    levels = List(
      new Level(
        Array( // Un level du vrai jeu, très chiant à recopier si on veut en faire plus
          Array(W, W, W, W, W, W, W, W, W, W),
          Array(W, W, W, W, P, 0, S, T, W, W),
          Array(W, W, W, W, W, W, W, 0, W, W),
          Array(W, W, W, 0, T, 0, R, T, W, W),
          Array(W, W, 0, C, 0, T, R, 0, W, W),
          Array(W, W, G, R, R, 0, R, 0, W, W),
          Array(W, W, W, 0, 0, T, R, T, K, W),
          Array(W, W, W, W, W, W, W, W, W, W)
        ), 22
      )
    )

    // Chargement du premier niveau
    loadLevel(currentLevelIndex)

    // Démarre un timer pour redessiner le monde périodiquement
    val timer = new Timer(100, _ => renderWorld())
    timer.start()

    // Gestion des événements clavier
    fg.setKeyManager(new KeyAdapter() {
      override def keyPressed(e: KeyEvent): Unit = {
        e.getKeyCode match {
          case KeyEvent.VK_UP    => handlePlayerInput(0, -1)
          case KeyEvent.VK_DOWN  => handlePlayerInput(0, 1)
          case KeyEvent.VK_LEFT  => handlePlayerInput(-1, 0)
          case KeyEvent.VK_RIGHT => handlePlayerInput(1, 0)
          case KeyEvent.VK_R     => loadLevel(currentLevelIndex) // Réinitialise le niveau
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
  def loadLevel(levelIndex: Int): Unit = {
    val level = levels(levelIndex)
    gridWidth = level.gridWidth
    gridHeight = level.gridHeight
    // Copie profonde de la grille pour éviter toute modification directe du Level original
    world = level.grid.map(_.clone())
    trapWorld = Array.fill(gridWidth, gridHeight)(0)

    // Initialise l'état des pièges : s'il y a un T dans la grille, on marque -1 pour l'activation
    for (x <- 0 until gridWidth; y <- 0 until gridHeight) {
      if (world(x)(y) == T) trapWorld(x)(y) = -1
    }

    // Trouve la position initiale du joueur
    playerPos = findPlayer(world)
    level.currentMoves = 0
    level.hasKey = false
  }

  /**
   * Parcourt la grille pour trouver la position (x, y) du joueur.
   *
   * @param grid Grille de jeu.
   * @return Tuple (x, y) représentant la position du joueur.
   */
  def findPlayer(grid: Array[Array[Int]]): (Int, Int) = {
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
  def handlePlayerInput(dx: Int, dy: Int): Unit = {
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

    // Vérifie si la case ciblée est accessible (ni mur, ni squelette, etc.)
    if (isValidMove(newX, newY)) {
      // Si le joueur récupère une clé
      if (world(newX)(newY) == K) {
        level.hasKey = true
        println("Clé récupérée !")
      }
      // Vérifie si on marche sur un piège "vivant" (-1)
      if (trapWorld(newX)(newY) == -1) {
        println("Ouch! Piège actif : vous perdez un déplacement supplémentaire.")
        level.currentMoves += 1 // Pénalité
      }

      // Effectue le mouvement du joueur
      movePlayer(newX, newY)
      level.currentMoves += 1

      // Inverse l'état des pièges après chaque déplacement
      toggleTraps()

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
        // Si il y a un mur ou un rocher à l'endroit où on veut pousser le squelette, on le détruit
        if (world(entityNewX)(entityNewY) == W || world(entityNewX)(entityNewY) == R) {
          println("Squelette détruit !")
          destroyEntity(newX, newY)
        }
        // Sinon, s'il n'y a rien ou bien un piège, on déplace le squelette
        else if (world(entityNewX)(entityNewY) == 0 || trapWorld(entityNewX)(entityNewY) != 0) {
          moveEntity(newX, newY, entityNewX, entityNewY)
          level.currentMoves += 1
          toggleTraps()
        }
      }

    } else if (world(newX)(newY) == R) {
      // Gestion du déplacement des rochers
      val entityNewX = newX + dx
      val entityNewY = newY + dy

      // Vérifie si le rocher peut être poussé
      if (entityNewX < 0 || entityNewX >= gridWidth || entityNewY < 0 || entityNewY >= gridHeight) {
        // Destination hors limites => on ne pousse pas
      } else {
        // On regarde ce qui se trouve à l'endroit où on souhaite pousser le rocher
        world(entityNewX)(entityNewY) match {
          // Si la destination est un mur, un rocher ou un coffre, on ne peut pas pousser
          case W | R | C =>
            println("Rocher bloqué par un mur/rocher !")
            level.currentMoves += 1
            toggleTraps()

          // S'il n'y a rien ou un piège, on peut pousser
          case _ =>
            moveEntity(newX, newY, entityNewX, entityNewY)
            level.currentMoves += 1
            toggleTraps()
        }
      }
    }
    // Vérifie si on a terminé le niveau
    checkLevelCompletion()
  }

  /**
   * Change l'état de chaque piège : d'actif (-1) à inactif (1) et vice-versa.
   */
  def toggleTraps(): Unit = {
    for (x <- 0 until gridWidth; y <- 0 until gridHeight) {
      if (trapWorld(x)(y) == 1) trapWorld(x)(y) = -1
      else if (trapWorld(x)(y) == -1) trapWorld(x)(y) = 1
    }
  }

  /**
   * Déplace le joueur vers de nouvelles coordonnées (newX, newY).
   */
  def movePlayer(newX: Int, newY: Int): Unit = {
    val (x, y) = playerPos
    world(x)(y) = 0
    world(newX)(newY) = P
    playerPos = (newX, newY)
  }

  /**
   * Déplace une entité quelconque (squelette, rocher, etc.) vers de nouvelles coordonnées.
   */
  def moveEntity(oldX: Int, oldY: Int, newX: Int, newY: Int): Unit = {
    val entity = world(oldX)(oldY)
    world(oldX)(oldY) = 0
    world(newX)(newY) = entity
  }

  /**
   * Détruit (supprime) une entité sur la grille.
   */
  def destroyEntity(x: Int, y: Int): Unit = {
    world(x)(y) = 0
  }

  /**
   * Vérifie si le joueur peut se déplacer sur la case (x, y).
   */
  def isValidMove(x: Int, y: Int): Boolean = {
    x >= 0 && x < gridWidth &&
      y >= 0 && y < gridHeight &&
      world(x)(y) != W &&  // Bloqué par un mur
      world(x)(y) != S &&  // Bloqué par un squelette
      world(x)(y) != R &&  // Bloqué par un rocher
      world(x)(y) != C     // Bloqué par un coffre fermé
  }

  /**
   * Vérifie si un squelette peut se déplacer sur la case (x, y).
   * (Méthode actuellement pas utilisée, laissée pour des évolutions futures)
   */
  def isValidMoveForSkeleton(x: Int, y: Int): Boolean = {
    x >= 0 && x < gridWidth &&
      y >= 0 && y < gridHeight &&
      (world(x)(y) == 0 || trapWorld(x)(y) != 0)
  }

  /**
   * Affiche la grille, ses entités et l'état des pièges à l'écran.
   */
  def renderWorld(): Unit = {
    fg.clear(Color.WHITE)

    // Dessine les pièges en premier
    for (i <- 0 until gridWidth; j <- 0 until gridHeight) {
      if (trapWorld(i)(j) == 1) fg.setColor(Color.RED)
      else if (trapWorld(i)(j) == -1) fg.setColor(Color.ORANGE)

      if (trapWorld(i)(j) != 0) {
        fg.drawFillRect(offsetX + i * tileSize, offsetY + j * tileSize, tileSize, tileSize)
      }
    }

    // Dessine les entités par-dessus

    //L'ordre n'as pas d'importance pour cette version du jeu mais avec les sprites il faut garder ce sens je pense
    for (i <- 0 until gridWidth; j <- 0 until gridHeight) {
      world(i)(j) match {
        case P =>
          fg.setColor(Color.BLUE)
          fg.drawFillRect(offsetX + i * tileSize, offsetY + j * tileSize, tileSize, tileSize)
        case W =>
          fg.setColor(Color.BLACK)
          fg.drawFillRect(offsetX + i * tileSize, offsetY + j * tileSize, tileSize, tileSize)
        case G =>
          fg.setColor(Color.GREEN)
          fg.drawFillRect(offsetX + i * tileSize, offsetY + j * tileSize, tileSize, tileSize)
        case S =>
          fg.setColor(Color.MAGENTA)
          fg.drawFillRect(offsetX + i * tileSize, offsetY + j * tileSize, tileSize, tileSize)
        case R =>
          fg.setColor(Color.GRAY)
          fg.drawFillRect(offsetX + i * tileSize, offsetY + j * tileSize, tileSize, tileSize)
        case C =>
          fg.setColor(Color.YELLOW)
          fg.drawFillRect(offsetX + i * tileSize, offsetY + j * tileSize, tileSize, tileSize)
        case K =>
          fg.setColor(Color.PINK)
          fg.drawFillRect(offsetX + i * tileSize, offsetY + j * tileSize, tileSize, tileSize)
        case _ =>
      }

      // Dessine la grille (contour) pour délimiter les cases
      fg.setColor(Color.BLACK)
      fg.drawRect(offsetX + i * tileSize, offsetY + j * tileSize, tileSize, tileSize)
    }

    // Affiche le nombre de mouvements restants
    val level = levels(currentLevelIndex)
    fg.setColor(Color.BLACK)
    println(s"Mouvements restants : ${level.maxMoves - level.currentMoves}", 10, 20)
    // A afficher sur le GUI du jeu
  }

  /**
   * Vérifie si le joueur a atteint le but (G) en étant adjacent à celui-ci.
   * Si oui, charge le niveau suivant ou termine la partie si tous les niveaux sont finis.
   */
  def checkLevelCompletion(): Unit = {
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
        // Ici, on pourrait stopper le jeu ou lancer une autre séquence.
      }
    }
  }
}

/*
  PROCHAINES ÉTAPES DU PROJET :

  - Importer des images pour afficher chaque entité (joueur, squelette, rocher, etc.)
    au lieu de simples rectangles de couleur.
  - Mettre en place une interface utilisateur complète (GUI) avec menus, boutons,
    et éventuellement un système de score/points.
  - Importer des effets sonores et de la musique d'ambiance pour rendre le jeu
    plus immersif.
  - Améliorer les performances et l’optimisation si nécessaire (le jeu est degeu pour le moment j'espère qu'avec les images ça sera moins lourd
  , faudra plus écrire 10000 de pixesl par coup de clock mdr )





    PS : Merci GPT pour les commentaires :)
*/
