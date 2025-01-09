import hevs.graphics.FunGraphics
import java.awt.{Color, Font}
import java.awt.event.{KeyAdapter, KeyEvent}
import javax.swing.SwingConstants


/**
 * Main object for the ISC Taker game.
 */
object Main extends App {
  private var currentLevelIndex: Int = 0
  private var levels: List[Level] = _
  private var world: Array[Array[Int]] = _
  private var trapWorld: Array[Array[Int]] = _
  private var gridWidth: Int = 5
  private var gridHeight: Int = 5

  private var frameCount: Int = 0

  private var AnimationIndex: Int = 0
  var Kicking: Boolean = false

  var Mudry: EntityRender = new EntityRender(
    List(
      "/res/Mudry - Idle.png",
      "/res/Mudry - Left.png",
      "/res/Mudry - Idle.png",
      "/res/Mudry - Right.png",
    ),
    0.7
  )

  var MudryFlipped: EntityRender = new EntityRender(
    List(
      "/res/Mudry Idle F.png",
      "/res/Mudry Left F.png",
      "/res/Mudry Idle F.png",
      "/res/Mudry Right F.png",
    ),
    0.7
  )



  var MudryKick : EntityRender = new EntityRender(
    List(
      "/res/Mudry Kick - frame 1.png",
      "/res/Mudry Kick - frame 2.png",
      "/res/Mudry Kick - frame 3.png",
      "/res/Mudry Kick - frame 4.png",
    ),
    0.7
  )

  var MudryKickFlipped : EntityRender = new EntityRender(
    List(
      "/res/Mudry Kick frame 1 F.png",
      "/res/Mudry Kick frame 2 F.png",
      "/res/Mudry Kick frame 3 F.png",
      "/res/Mudry Kick frame 4 F.png",
    ),
    0.7
  )

  var Pandemonica : EntityRender = new EntityRender(
    List(
      "/res/Pandemonica - Idle.gif",
      "/res/Pandemonica - Left.gif",
      "/res/Pandemonica - Idle.gif",
      "/res/Pandemonica - Right.gif",
    ),
    0.6
  )



  var Modeus : EntityRender = new EntityRender(
    List(
      "/res/Modeus Idle.gif",
      "/res/Modeus Left.gif",
      "/res/Modeus Idle.gif",
      "/res/Modeus Right.gif",
    ),
    0.6
)

  var Cerberus : EntityRender = new EntityRender(
    List(
      "/res/Cerberus Idle.gif",
      "/res/Cerberus Left.gif",
      "/res/Cerberus Idle.gif",
      "/res/Cerberus Right.gif",
    ),
    0.6
  )

  var Justice : EntityRender = new EntityRender(
    List(
      "/res/Justice Idle.gif",
      "/res/Justice Left.gif",
      "/res/Justice Idle.gif",
      "/res/Justice Right.gif",
    ),
    0.6
  )

  var Azazel : EntityRender = new EntityRender(
    List(
      "/res/Azazel Idle.gif",
      "/res/Azazel Left.gif",
      "/res/Azazel Idle.gif",
      "/res/Azazel Right.gif",
    ),
    0.6
  )

  var Zdrada : EntityRender = new EntityRender(
    List(
      "/res/Zdrada Idle.gif",
      "/res/Zdrada Left.gif",
      "/res/Zdrada Idle.gif",
      "/res/Zdrada Right.gif",
    ),
    0.6
  )

  var Malina : EntityRender = new EntityRender(
    List(
      "/res/Malina Idle.gif",
      "/res/Malina Left.gif",
      "/res/Malina Idle.gif",
      "/res/Malina Right.gif",
    ),
    0.6
  )

  var Skeleton : EntityRender = new EntityRender(
    List(
      "/res/skeleton - Idle.gif",
      "/res/skeleton - Left.gif",
      "/res/skeleton - Idle.gif",
      "/res/skeleton - Right.gif",
    ),
    0.6
  )




  // New arrays for storing the previous frame’s state
  private var oldWorld: Array[Array[Int]] = _
  private var oldTrapWorld: Array[Array[Int]] = _

  // Keep track of the previous frame’s “moves left” to redraw only if changed
  private var oldMovesLeft: Int = -1

  private val playerImagePath = "/res/Mudry.png"
  private val playerImagePathFlipped = "/res/MudryFlipped.png"

  private val skeletonPath = "/res/skeleton.png"
  private val rockPath = "/res/rock.png"

  private val keyPath = "/res/key.png"
  private val chestPath = "/res/chest.png"
  private val spikeUpPath = "/res/spikeup.png"
  private val spikeDownPath = "/res/spikedown.png"

  private val transitionScreenPath = "/res/transition.png"

  private val screenWidth = 900
  private val screenHeight = 563

  private val tileSize = 47

  // Audio
  private val gameMusic = new Audio("res/sounds/Mittsies-Vitality.wav")
  private val playerMove = new Audio("res/sounds/character_move.wav")
  private val doorClosedKick = new Audio("res/sounds/door_closed_kick.wav")
  private val doorOpening = new Audio("res/sounds/door_opening.wav")
  private val enemyDie = new Audio("res/sounds/enemy_die.wav")
  private val enemyKick = new Audio("res/sounds/enemy_kick.wav")
  private val keyPickUp = new Audio("res/sounds/key_pick_up.wav")
  private val playerDeath = new Audio("res/sounds/player_death.wav")
  private val screenChanger = new Audio("res/sounds/screen_changer.wav")
  private val spikesDamage = new Audio("res/sounds/spikes_damage.wav")
  private val stoneKick = new Audio("res/sounds/stone_kick.wav")
  private val stoneMove = new Audio("res/sounds/stone_move.wav")


  // Création de la fenêtre graphique
  private val fg = new FunGraphics(screenWidth, screenHeight, "ISC TAKER")

  // Codes associés aux différentes entités du jeu
  val P = 1  // Joueur
  val W = 2  // Mur
  val G = 3  // But (Goal)
  val S = 4  // Squelette
  val R = 5  // Rocher
  val C = 6  // Coffre
  val K = 8  // Clé

  // Position initiale du joueur
  private var playerPos: (Int, Int) = (0, 0)

  private var playerDirection: Boolean = false
  private var shouldRenderAdvice: Boolean = false
  private var shouldRenderTransition: Boolean = false


  // Initialisation de la liste des niveaux
  levels = Level.initializeLevels(screenWidth, gridWidth, tileSize)

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
    if (level.currentMoves >= level.maxMoves) {
      println("Mouvements épuisés ! Redémarrage du niveau.")
      playerDeath.play()
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
        keyPickUp.play()
      }
      // Vérifie si on marche sur un piège "vivant" (1)
      if (trapWorld(newX)(newY) == -1) {
        println("Ouch! Piège actif : vous perdez un déplacement supplémentaire.")
        spikesDamage.play()
        level.currentMoves += 1 // Pénalité
      }

      // Effectue le mouvement du joueur
      movePlayer(newX, newY)
      level.currentMoves += 1

      // Inverse l'état des pièges après chaque déplacement
      toggleTraps(level)

    } else if (world(newX)(newY) == C && !level.hasKey) {
      // Si on tente d'ouvrir une porte sans clé
      println("Porte fermée !")
      doorClosedKick.play()

    } else if (world(newX)(newY) == C && level.hasKey) {
      // Ouvrir la porte si on a la clé
      println("Porte ouverte !")
      doorOpening.play()
      destroyEntity(newX, newY)

    } else if (world(newX)(newY) == S) {
      // S'il y a un squelette, on tente de le pousser
      val entityNewX = newX + dx
      val entityNewY = newY + dy

      enemyKick.play()
      Kicking = true

      // Vérifie si la position cible est dans les limites du tableau
      if (entityNewX >= 0 && entityNewX < gridWidth && entityNewY >= 0 && entityNewY < gridHeight) {
        // S'il y a un mur ou un rocher à l'endroit où on veut pousser le squelette, on le détruit.
        if (world(entityNewX)(entityNewY) == W || world(entityNewX)(entityNewY) == R) {
          println("Squelette détruit !")
          enemyDie.play()
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

      stoneKick.play()
      Kicking = true

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
            stoneMove.play()
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
    playerMove.play()
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
  def renderWorld(direction:Boolean,AnimationIndex:Int): Unit = {
    val level = levels(currentLevelIndex)
    val movesLeft = math.max(0, level.maxMoves - level.currentMoves)

    fg.displayFPS(true)

    try {
      fg.drawTransformedPicture(
        posX   = screenWidth / 2,
        posY   = screenHeight / 2,
        angle  = 0.0,
        scale  = 1,
        imageName = level.backgroundPath
      )

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
        outlineThickness = 20)

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
            if(direction){
              if(Kicking){
                fg.drawTransformedPicture(
                  posX   = level.offsetX + i * tileSize + tileSize / 2,
                  posY   = level.offsetY + j * tileSize + tileSize / 2,
                  angle  = 0.0,
                  scale  = MudryKickFlipped.scale,
                  imageName = MudryKickFlipped.frames(AnimationIndex)
                )
                if(AnimationIndex == 3){
                  Kicking = false
                }
              }
              else{
                fg.drawTransformedPicture(
                  posX   = level.offsetX + i * tileSize + tileSize / 2,
                  posY   = level.offsetY + j * tileSize + tileSize / 2,
                  angle  = 0.0,
                  scale  = MudryFlipped.scale,
                  imageName = MudryFlipped.frames(AnimationIndex)
                )
              }
            }
            else{
              if(Kicking){
                fg.drawTransformedPicture(
                  posX   = level.offsetX + i * tileSize + tileSize / 2,
                  posY   = level.offsetY + j * tileSize + tileSize / 2,
                  angle  = 0.0,
                  scale  = MudryKick.scale,
                  imageName = MudryKick.frames(AnimationIndex)
                )
                if(AnimationIndex == 3){
                  Kicking = false
                }
              }
              else{
                fg.drawTransformedPicture(
                  posX   = level.offsetX + i * tileSize + tileSize / 2,
                  posY   = level.offsetY + j * tileSize + tileSize / 2,
                  angle  = 0.0,
                  scale  = Mudry.scale,
                  imageName = Mudry.frames(AnimationIndex)
                )
              }
            }

          case W =>

          case G =>
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.8,
              imageName = level.Demon.frames(AnimationIndex)
            )
          case S =>
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = Skeleton.scale,
              imageName = Skeleton.frames(AnimationIndex)
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

    } catch {
      case e: Exception => // Ignore
    }
    // Loop through the grid


    // Finally, update the number of moves left on screen only if it changed

      // Clear the old text region. For example, assume we show it near top-left:


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

        // Affiche l'écran de transition
        screenChanger.play()
        shouldRenderTransition = true
        // Attendre 2 secondes avant de charger le prochain niveau
        Thread.sleep(2000)
        shouldRenderTransition = false

        loadLevel(currentLevelIndex)
      } else {
        println("Félicitations ! Vous avez terminé tous les niveaux !")

        // Ici, on pourrait stopper le jeu ou lancer une autre séquence.
      }
    }
  }

  /**
   * Renders the advice screen with game instructions.
   */
  private def renderAdvice(): Unit = {
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

  private def renderTransition(): Unit = {
    fg.drawTransformedPicture(
      posX   = screenWidth / 2,
      posY   = screenHeight / 2,
      angle  = 0.0,
      scale  = 1,
      imageName = transitionScreenPath
    )
  }


  // Gestion des événements clavier
  fg.setKeyManager(new KeyAdapter() {
    override def keyPressed(e: KeyEvent): Unit = {
      e.getKeyCode match {
        case KeyEvent.VK_UP     => handlePlayerInput(0, -1)
          playerDirection = false
        case KeyEvent.VK_DOWN   => handlePlayerInput(0, 1)
          playerDirection = false
        case KeyEvent.VK_LEFT   => handlePlayerInput(-1, 0)
          playerDirection = true
        case KeyEvent.VK_RIGHT  => handlePlayerInput(1, 0)
          playerDirection = false
        case KeyEvent.VK_R      => loadLevel(currentLevelIndex) // Réinitialise le niveau
          playerDirection = false; playerDeath.play()
        case KeyEvent.VK_L      => shouldRenderAdvice = true // Affiche les conseils
        case KeyEvent.VK_ESCAPE => shouldRenderAdvice = false // Cache les conseils
        case _                  => // Aucune action pour les autres touches
      }
    }
  })

  // Lancement de la musique de fond
  gameMusic.play()

  // Chargement du premier niveau
  loadLevel(currentLevelIndex)

  // Boucle principale du jeu
  while (true) {

    // 1) Increment the frameCount each loop (which is ~60 times/sec if fg.syncGameLogic(60))
    frameCount += 1

    // 2) Every 15 frames, advance Mudry's animation
    if (frameCount % 2 == 0) {
      // Cycle through all frames in Mudry.frames
      AnimationIndex = (AnimationIndex + 1) % Mudry.frames.length
    }

    if (shouldRenderTransition) {
      fg.frontBuffer.synchronized {
        renderTransition()
      }
    } else if (!shouldRenderAdvice) {
      fg.frontBuffer.synchronized {
        // 3) Pass the current AnimationIndex to renderWorld
        renderWorld(playerDirection, AnimationIndex)
      }
    } else {
      fg.frontBuffer.synchronized {
        renderAdvice()
      }
    }


    // Sync the game logic ~60 times a second
    fg.syncGameLogic(10)
  }

}
