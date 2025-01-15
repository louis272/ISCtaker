import hevs.graphics.FunGraphics
import java.awt.{Color, Font}
import java.awt.event.{KeyAdapter, KeyEvent}
import javax.swing.SwingConstants


/**
 * Main object for the ISC Taker game.
 */
object Main extends App {
  private var currentLevelIndex: Int = 0 // Index of the current level
  private var levels: List[Level] = _ // List of levels
  private var world: Array[Array[Int]] = _ // Current world grid
  private var trapWorld: Array[Array[Int]] = _ // Current traps grid
  private var gridWidth: Int = 5 // Default grid width
  private var gridHeight: Int = 5 // Default grid height

  private var frameCount: Int = 0 // Frame counter for animations

  private var AnimationIndex: Int = 0 // Index of the current animation frame
  private var Kicking: Boolean = false // Flag to indicate if the player is kicking

  private val Mudry: EntityRender = new EntityRender( // EntityRender for Mudry
    List(
      "/res/Mudry - Idle.png",
      "/res/Mudry - Left.png",
      "/res/Mudry - Idle.png",
      "/res/Mudry - Right.png",
    ),
    0.7
  )

  private val MudryFlipped: EntityRender = new EntityRender( // EntityRender for Mudry (flipped)
    List(
      "/res/Mudry Idle F.png",
      "/res/Mudry Left F.png",
      "/res/Mudry Idle F.png",
      "/res/Mudry Right F.png",
    ),
    0.7
  )

  private val MudryKick : EntityRender = new EntityRender( // EntityRender for Mudry (kicking)
    List(
      "/res/Mudry Kick - frame 1.png",
      "/res/Mudry Kick - frame 2.png",
      "/res/Mudry Kick - frame 3.png",
      "/res/Mudry Kick - frame 4.png",
    ),
    0.7
  )

  private val MudryKickFlipped : EntityRender = new EntityRender( // EntityRender for Mudry (kicking, flipped)
    List(
      "/res/Mudry Kick frame 1 F.png",
      "/res/Mudry Kick frame 2 F.png",
      "/res/Mudry Kick frame 3 F.png",
      "/res/Mudry Kick frame 4 F.png",
    ),
    0.7
  )

  var Pandemonica : EntityRender = new EntityRender( // EntityRender for Pandemonica
    List(
      "/res/Pandemonica - Idle.gif",
      "/res/Pandemonica - Left.gif",
      "/res/Pandemonica - Idle.gif",
      "/res/Pandemonica - Right.gif",
    ),
    0.6
  )

  var Modeus : EntityRender = new EntityRender( // EntityRender for Modeus
    List(
      "/res/Modeus Idle.gif",
      "/res/Modeus Left.gif",
      "/res/Modeus Idle.gif",
      "/res/Modeus Right.gif",
    ),
    0.6
  )

  var Cerberus : EntityRender = new EntityRender( // EntityRender for Cerberus
    List(
      "/res/Cerberus Idle.gif",
      "/res/Cerberus Left.gif",
      "/res/Cerberus Idle.gif",
      "/res/Cerberus Right.gif",
    ),
    0.6
  )
//Existing Sprites for more levels if needed
//  var Justice : EntityRender = new EntityRender(
//    List(
//      "/res/Justice Idle.gif",
//      "/res/Justice Left.gif",
//      "/res/Justice Idle.gif",
//      "/res/Justice Right.gif",
//    ),
//    0.6
//  )
//
//  var Azazel : EntityRender = new EntityRender(
//    List(
//      "/res/Azazel Idle.gif",
//      "/res/Azazel Left.gif",
//      "/res/Azazel Idle.gif",
//      "/res/Azazel Right.gif",
//    ),
//    0.6
//  )

  var Zdrada : EntityRender = new EntityRender(  // EntityRender for Zdrada
    List(
      "/res/Zdrada Idle.gif",
      "/res/Zdrada Left.gif",
      "/res/Zdrada Idle.gif",
      "/res/Zdrada Right.gif",
    ),
    0.6
  )

  var Malina : EntityRender = new EntityRender( // EntityRender for Malina
    List(
      "/res/Malina Idle.gif",
      "/res/Malina Left.gif",
      "/res/Malina Idle.gif",
      "/res/Malina Right.gif",
    ),
    0.6
  )

  private val Skeleton : EntityRender = new EntityRender( // EntityRender for Skeleton
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

  // Paths to the images
  private val rockPath             = "/res/rock.png"
  private val keyPath              = "/res/key.png"
  private val chestPath            = "/res/chest.png"
  private val spikeUpPath          = "/res/spikeup.png"
  private val spikeDownPath        = "/res/spikedown.png"
  private val transitionScreenPath = "/res/transition.png"
  private val tutoScreenPath       = "/res/Tuto.png"

  // Dimensions
  private val screenWidth  = 900
  private val screenHeight = 563
  private val tileSize     = 47

  // Audio
  private val gameMusic      = new Audio("res/sounds/Mittsies-Vitality.wav")
  private val playerMove     = new Audio("res/sounds/character_move.wav")
  private val doorClosedKick = new Audio("res/sounds/door_closed_kick.wav")
  private val doorOpening    = new Audio("res/sounds/door_opening.wav")
  private val enemyDie       = new Audio("res/sounds/enemy_die.wav")
  private val enemyKick      = new Audio("res/sounds/enemy_kick.wav")
  private val keyPickUp      = new Audio("res/sounds/key_pick_up.wav")
  private val playerDeath    = new Audio("res/sounds/player_death.wav")
  private val screenChanger  = new Audio("res/sounds/screen_changer.wav")
  private val spikesDamage   = new Audio("res/sounds/spikes_damage.wav")
  private val stoneKick      = new Audio("res/sounds/stone_kick.wav")
  private val stoneMove      = new Audio("res/sounds/stone_move.wav")


  // Create a new FunGraphics object
  private val fg = new FunGraphics(screenWidth, screenHeight, "ISC TAKER")

  // Constants for the entities
  val P = 1  // Player
  val W = 2  // Wall
  val G = 3  // Goal
  val S = 4  // Skeleton
  val R = 5  // Rock
  val C = 6  // Door
  val K = 8  // Key

  // Variables for the player
  private var playerPos: (Int, Int) = (0, 0)
  private var playerDirection: Boolean = false // false = right, true = left
  // Variables for the game state
  private var shouldRenderAdvice: Boolean     = false
  private var shouldRenderTransition: Boolean = false


  // Initialize the levels
  levels = Level.initializeLevels(screenWidth, gridWidth, tileSize)

  /**
   * Load a level from the list of levels.
   *
   * @param levelIndex The index of the level to load.
   */
  private def loadLevel(levelIndex: Int): Unit = {
    val level = levels(levelIndex)
    gridWidth = level.gridWidth
    gridHeight = level.gridHeight
    // Copy the grid and trapsGrid to the world and trapWorld arrays
    world = level.grid.map(_.clone())
    trapWorld = level.trapsGrid.map(_.clone())
    // Initialize the oldWorld and oldTrapWorld arrays
    oldWorld    = Array.fill(gridWidth, gridHeight)(-999)
    oldTrapWorld= Array.fill(gridWidth, gridHeight)(-999)
    // Set the player position
    playerPos = findPlayer(world)
    level.currentMoves = 0
    level.hasKey = false
    // Reset the previous moves left
    oldMovesLeft = -1
  }

  /**
   * Find the player in the grid.
   *
   * @param grid The grid to search in.
   * @return The position of the player.
   */
  private def findPlayer(grid: Array[Array[Int]]): (Int, Int) = {
    for (x <- grid.indices; y <- grid(x).indices) {
      if (grid(x)(y) == P) return (x, y)
    }
    throw new Exception("Joueur non trouvé dans la grille !")
  }

  /**
   * Handle the player input.
   *
   * @param dx The change in x.
   * @param dy The change in y.
   */
  private def handlePlayerInput(dx: Int, dy: Int): Unit = {
    val level = levels(currentLevelIndex)

    // Check if the player has any moves left
    if (level.currentMoves >= level.maxMoves) {
      playerDeath.play()
      transitionScreen()
      loadLevel(currentLevelIndex)
      return
    }
    // Get the player's current position
    val (x, y) = playerPos
    val newX = x + dx
    val newY = y + dy

    // Check if the move is valid
    if (isValidMove(newX, newY)) {
      // If the player is moving to a key, pick it up
      if (world(newX)(newY) == K) {
        level.hasKey = true
        keyPickUp.play()
      }
      // Check if the player is moving to a trap
      if (trapWorld(newX)(newY) == -1) {
        spikesDamage.play()
        level.currentMoves += 1 // Player loses an extra move
      }

      // Move the player
      movePlayer(newX, newY)
      level.currentMoves += 1

      // toggle the traps
      toggleTraps(level)

    } else if (world(newX)(newY) == C && !level.hasKey) {
      // If the player is moving to a closed door without a key
      doorClosedKick.play()

    } else if (world(newX)(newY) == C && level.hasKey) {
      // Open the door if the player has the key
      doorOpening.play()
      destroyEntity(newX, newY)

    } else if (world(newX)(newY) == S) {
      // If the player is moving to a skeleton, try to kick it
      val entityNewX = newX + dx
      val entityNewY = newY + dy

      enemyKick.play()
      Kicking = true

      // Check if the skeleton can be pushed
      if (entityNewX >= 0 && entityNewX < gridWidth && entityNewY >= 0 && entityNewY < gridHeight) {
        // If the destination is a wall or a rock, the skeleton is destroyed
        if (world(entityNewX)(entityNewY) == W || world(entityNewX)(entityNewY) == R) {
          enemyDie.play()
          destroyEntity(newX, newY)
        }
        // Else, if the destination is empty or a trap, move the player and the skeleton
        else if (world(entityNewX)(entityNewY) == 0 || trapWorld(entityNewX)(entityNewY) != 0) {
          moveEntity(newX, newY, entityNewX, entityNewY)
          level.currentMoves += 1
          toggleTraps(level)
        }
      }

    } else if (world(newX)(newY) == R) {
      // If the player is moving to a rock, try to kick it
      val entityNewX = newX + dx
      val entityNewY = newY + dy

      stoneKick.play()
      Kicking = true

      // Check if the rock can be pushed
      if (entityNewX < 0 || entityNewX >= gridWidth || entityNewY < 0 || entityNewY >= gridHeight) {
        // If the destination is out of bounds, the rock is destroyed
      } else {
        // Check if the rock can be pushed
        world(entityNewX)(entityNewY) match {
          // If the destination is a wall or a rock, the rock is destroyed
          case W | R | C | S =>
            level.currentMoves += 1
            toggleTraps(level)

          // Else, if the destination is empty, move the player and the rock
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
   * Inverts the state of the traps.
   *
   * @param level The current level.
   */
  private def toggleTraps(level: Level): Unit = {
    for (x <- 0 until gridWidth; y <- 0 until gridHeight) {
      if (trapWorld(x)(y) == 1 && level.movableSpikes) trapWorld(x)(y) = -1
      else if (trapWorld(x)(y) == -1 && level.movableSpikes) trapWorld(x)(y) = 1
    }
  }

  /**
   * Move the player to new coordinates.
   *
   * @param newX New X position.
   * @param newY New Y position.
   */
  private def movePlayer(newX: Int, newY: Int): Unit = {
    val (x, y) = playerPos
    world(x)(y) = 0
    world(newX)(newY) = P
    playerPos = (newX, newY)
    playerMove.play()
  }

  /**
   * Move an entity to new coordinates.
   *
   * @param oldX Old X position.
   * @param oldY Old Y position.
   * @param newX New X position.
   * @param newY New Y position.
   */
  private def moveEntity(oldX: Int, oldY: Int, newX: Int, newY: Int): Unit = {
    val entity = world(oldX)(oldY)
    world(oldX)(oldY) = 0
    world(newX)(newY) = entity
  }

  /**
   * Destroy an entity at the given coordinates.
   *
   * @param x X position.
   * @param y Y position.
   */
  private def destroyEntity(x: Int, y: Int): Unit = {
    world(x)(y) = 0
  }

  /**
   * Check if the move is valid.
   *
   * @param x X position.
   * @param y Y position.
   * @return True if the move is valid, false otherwise.
   */
  private def isValidMove(x: Int, y: Int): Boolean = {
    x >= 0 && x < gridWidth &&
      y >= 0 && y < gridHeight &&
      world(x)(y) != W &&  // Bloqué par un mur
      world(x)(y) != S &&  // Bloqué par un squelette
      world(x)(y) != R &&  // Bloqué par un rocher
      world(x)(y) != C     // Bloqué par une porte fermée
  }

  /**
   * Render the world.
   *
   * @param direction The direction of the player.
   * @param AnimationIndex The index of the current animation frame.
   */
  private def renderWorld(direction:Boolean, AnimationIndex:Int): Unit = {
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

      if (currentLevelIndex!= levels.length -1) {
        fg.setColor(Color.BLACK)
        fg.drawFillRect(50,380,100,60)
        // Now draw the new text
        fg.setColor(Color.WHITE)
        fg.drawFancyString( // Draw the moves left
          posX = 70,
          posY = 425,
          str = s"$movesLeft",
          fontFamily = "Arial",
          fontStyle = Font.BOLD,
          fontSize = 50,
          color = Color.WHITE,
          halign = SwingConstants.LEFT,
          valign = SwingConstants.BOTTOM,
          shadowX = 0,
          shadowY = 0,
          shadowColor = Color.BLACK,
          shadowThickness = 10,
          outlineColor = Color.BLACK,
          outlineThickness = 20)
      }

      for (i <- 0 until gridWidth; j <- 0 until gridHeight) {
        // Re-draw the trap if needed
        if (trapWorld(i)(j) == 1) {
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.1,
              imageName = if (!level.movableSpikes) {spikeDownPath} else spikeUpPath
            )

        } else if (trapWorld(i)(j) == -1) {
            fg.drawTransformedPicture(
              posX   = level.offsetX + i * tileSize + tileSize / 2,
              posY   = level.offsetY + j * tileSize + tileSize / 2,
              angle  = 0.0,
              scale  = 0.1,
              imageName = if (!level.movableSpikes) {spikeUpPath} else spikeDownPath
            )
        }

        // 3) Re-draw the actual entity
        world(i)(j) match {
          case P =>
            if(currentLevelIndex==levels.length - 1){
              Mudry.scale = 1
              MudryFlipped.scale = 1

              Mudry.frames = List(
                "/res/Mudry Idle HQ.png",
                "/res/Mudry Left HQ.png",
                "/res/Mudry Idle HQ.png",
                "/res/Mudry Right HQ.png",
              )

              MudryFlipped.frames = List(
                "/res/Mudry Idle HQ F.png",
                "/res/Mudry Left HQ F.png",
                "/res/Mudry Idle HQ F.png",
                "/res/Mudry Right HQ F.png",
              )

            }

            if (direction) {
              if (Kicking) {
                fg.drawTransformedPicture(
                  posX   = level.offsetX + i * tileSize + tileSize / 2,
                  posY   = level.offsetY + j * tileSize + tileSize / 2,
                  angle  = 0.0,
                  scale  = MudryKickFlipped.scale,
                  imageName = MudryKickFlipped.frames(AnimationIndex)
                )
                if (AnimationIndex == 3) {
                  Kicking = false
                }
              } else {
                fg.drawTransformedPicture(
                  posX   = level.offsetX + i * tileSize + tileSize / 2,
                  posY   = level.offsetY + j * tileSize + tileSize / 2,
                  angle  = 0.0,
                  scale  = MudryFlipped.scale,
                  imageName = MudryFlipped.frames(AnimationIndex)
                )
              }
            } else {
              if (Kicking) {
                fg.drawTransformedPicture(
                  posX   = level.offsetX + i * tileSize + tileSize / 2,
                  posY   = level.offsetY + j * tileSize + tileSize / 2,
                  angle  = 0.0,
                  scale  = MudryKick.scale,
                  imageName = MudryKick.frames(AnimationIndex)
                )
                if (AnimationIndex == 3) {
                  Kicking = false
                }
              } else {
                fg.drawTransformedPicture(
                  posX   = level.offsetX + i * tileSize + tileSize / 2,
                  posY   = level.offsetY + j * tileSize + tileSize / 2,
                  angle  = 0.0,
                  scale  = Mudry.scale,
                  imageName = Mudry.frames(AnimationIndex)
                )
              }
            }
          case W => // Nothing to draw for walls
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
        // Update oldWorld and oldTrapWorld
        oldWorld(i)(j) = world(i)(j)
        oldTrapWorld(i)(j) = trapWorld(i)(j)
      }

    } catch {
      case _: Exception => // Ignore
    }
  }

  /**
   * Check if the level is completed.
   */
  private def checkLevelCompletion(): Unit = {
    val (px, py) = playerPos

    // Check if the goal is adjacent to the player
    val goalAdjacent =
      (px > 0 && world(px - 1)(py) == G) ||
        (px < gridWidth - 1 && world(px + 1)(py) == G) ||
        (py > 0 && world(px)(py - 1) == G) ||
        (py < gridHeight - 1 && world(px)(py + 1) == G)

    if (goalAdjacent) {
      currentLevelIndex += 1
      if (currentLevelIndex < levels.length) {

        transitionScreen()

        loadLevel(currentLevelIndex)
      }
    }
  }

  /**
   * Display the advice screen.
   */
  private def transitionScreen(): Unit = {
    // Play the screen changer sound
    screenChanger.play()
    shouldRenderTransition = true
    // Wait for 2 seconds
    Thread.sleep(2000)
    shouldRenderTransition = false
  }

  /**
   * Display the advice screen.
   */
  private def renderAdvice(): Unit = {
    fg.drawTransformedPicture(
      posX   = screenWidth / 2,
      posY   = screenHeight / 2,
      angle  = 0.0,
      scale  = 1,
      imageName = tutoScreenPath
    )
  }

  /**
   * Display the transition screen.
   */
  private def renderTransition(): Unit = {
    fg.drawTransformedPicture(
      posX   = screenWidth / 2,
      posY   = screenHeight / 2,
      angle  = 0.0,
      scale  = 1,
      imageName = transitionScreenPath
    )
  }
  // Set the key manager
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
        case KeyEvent.VK_R      => loadLevel(currentLevelIndex) // Restart the level
          playerDirection = false; playerDeath.play(); transitionScreen()
        case KeyEvent.VK_L      => shouldRenderAdvice = true // Display the advice
        case KeyEvent.VK_ESCAPE => shouldRenderAdvice = false // Dismiss the advice
        case KeyEvent.VK_0      => currentLevelIndex = 0; loadLevel(currentLevelIndex) // Load level 0
        case KeyEvent.VK_1      => currentLevelIndex = 1; loadLevel(currentLevelIndex) // Load level 1
        case KeyEvent.VK_2      => currentLevelIndex = 2; loadLevel(currentLevelIndex) // Load level 2
        case KeyEvent.VK_3      => currentLevelIndex = 3; loadLevel(currentLevelIndex) // Load level 3
        case KeyEvent.VK_4      => currentLevelIndex = 4; loadLevel(currentLevelIndex) // Load level 4
        case KeyEvent.VK_5      => currentLevelIndex = 5; loadLevel(currentLevelIndex) // Load level 5
        case KeyEvent.VK_6      => currentLevelIndex = 6; loadLevel(currentLevelIndex) // Load level 6
        case _                  => // Ignore
      }
    }
  })

  // Start the game music
  gameMusic.play()

  // Load the first level
  loadLevel(currentLevelIndex)

  // Main game loop
  while (true) {

    // Increment the frame counter
    frameCount += 1

    // If the frame count is even, cycle through the frames
    if (frameCount % 2 == 0) {
      // Increment the AnimationIndex
      AnimationIndex = (AnimationIndex + 1) % Mudry.frames.length
    }

    if (shouldRenderTransition) { // If we should render the transition screen
      fg.frontBuffer.synchronized {
        renderTransition()
      }
    } else if (!shouldRenderAdvice) { // If we should render the game
      fg.frontBuffer.synchronized {
        // Pass the current AnimationIndex to renderWorld
        renderWorld(playerDirection, AnimationIndex)
      }
    } else {
      fg.frontBuffer.synchronized { // If we should render the advice screen
        renderAdvice()
      }
    }

    if (!gameMusic.audioClip.isRunning) { // If the game music has stopped, restart it
      gameMusic.play()
    }

    // Sync the game logic ~10 times a second
    fg.syncGameLogic(10)
  }
}
