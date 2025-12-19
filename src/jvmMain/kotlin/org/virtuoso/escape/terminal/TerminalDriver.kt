@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.terminal

import org.virtuoso.escape.model.*
import org.virtuoso.escape.model.account.AccountManager
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.math.max
import kotlin.uuid.ExperimentalUuidApi

/**
 * Creates and runs the game.
 *
 * @author gabri
 */
class TerminalDriver {
  //    private final Leaderboard leaderboard = new Leaderboard();
  private val DEBUG = true

  /**
   * Create a list of pairings.
   *
   * @param input Pairings.
   * @return A list of pairings.
   */
  fun makeTuiActionMap(
    vararg input: Pair<FunString, () -> Unit>
  ): MutableList<Pair<FunString, () -> Unit>> {
    return input.toMutableList()
  }

  /**
   * Create a terminal with unique keys for action name: action pairings.
   *
   * @param scanner The scanner to request input on.
   * @param tuiAction A list of name, action pairings. Note this is not an injective mapping.
   * @param status A prompt to display before the actions.
   */
  // Sequenced retains order.
  fun createActionInterface(tuiAction: MutableList<Pair<FunString, () -> Unit>>, status: Any) {
    check(status is FunString || status is String)
    if (tuiAction.isEmpty()) return
    val keyMap: MutableMap<String, Pair<FunString, () -> Unit>> = LinkedHashMap()
    // Create a unique key (or group of keys) to press for each action.
    for (pair in tuiAction) {
      var key: String
      val sourceKey = pair.first.rawText()
      var index = 0
      var width = 1
      while (true) { // Try to get a unique key.
        key = sourceKey.substring(index, index + width)
        if (key.matches(Regex("^\\w+$")) && !keyMap.containsKey(key.lowercase())) break
        if (index == sourceKey.length - width) {
          index = 0
          width++ // Try to get a wider key if no unique key can be found.
        }
        index++
      }

      val name = FunString(pair.first)
      val small = FunString(key).underline().bold()

      name.replaceSubstring(index, index + width, small)
      keyMap[key.lowercase()] = Pair(name, pair.second)
    }
    val validResponses = keyMap.keys
    val prompts = keyMap.values.map { it.first }
    val delim = " ■ "
    var prompt = FunString.join(delim, prompts)
    // If long, cut into two lines for readability -- We can only assume terminal width because of
    // java's cross-platform nature.
    if (prompt.length() > 125) {
      prompt = FunString.join(delim, prompts.subList(0, prompts.size / 2))
      prompt.add("\n")
      prompt.add(FunString.join(delim, prompts.subList(prompts.size / 2, prompts.size)))
    }

    clearScreen()
    display(status.toString())
    val response = validateInput(prompt.toString()) { o: String -> validResponses.contains(o) }
    keyMap[response]?.second?.invoke()
  }

  /**
   * Request user input until they provide valid input.
   *
   * @param scanner The scanner to request input on.
   * @param prompt The thing to ask the user.
   * @param predicate The function that validates the string.
   * @return Valid input.
   */
  fun validateInput(prompt: String?, predicate: (String) -> Boolean): String {
    var scanAttempt: String
    while (true) {
      display(prompt)
      scanAttempt = readlnOrNull()?.trim()?.lowercase() ?: continue
      print(FunString.escape(MOVE_LINE) + FunString.escape(CLEAR_BELOW))
      if (predicate(scanAttempt)) break
      print(FunString.escape("1A") + FunString.escape(CLEAR_BELOW))
    }
    return scanAttempt
  }

  /**
   * Request a valid username and password from the user.
   *
   * @param authenticator The function to try to authenticate on, usually create account or login.
   */
  fun tryLogin(
    authenticator: (String, String) -> Boolean,
  ): Boolean {
    val username: String
    val password: String
    val flag: Boolean
    username = validateInput("Enter your username:") { i: String -> i.isNotBlank() }
    password = validateInput("Enter your password:") { i: String -> i.isNotBlank() }
    try {
      authenticator(username.trim(), password.trim())
      // Move to the second console line
      print(FunString.escape("2;1H") + FunString.escape(CLEAR_BELOW))
      return true
    } catch (e: AccountManager.AccountError) {
      display((FunString(e.message)).red().toString())
      return false
    }
  }

  /**
   * Display something to the user.
   *
   * @param display The format string to display.
   * @param args Variables for the format string.
   */
  fun display(display: String?, vararg args: Any?) {
    // wrapper function allows for flair
    if (args.isEmpty()) println(display) else System.out.printf(display + "\n", *args)
  }

  /**
   * Display something to the user and wait for carriage return.
   *
   * @param str The format string to display
   * @param args Variables for the format string.
   */
  fun pauseDisplay(str: String?, vararg args: Any?) {
    display(str, *args)
    readln()
  }

  /**
   * Animate a display to the user by showing each character printed sequentially, then wait.
   *
   * @param str The string to typewrite.
   */
  fun typewriterDisplay(str: String) {
    val rate: Long = 60
    for (s in str.toCharArray()) {
      print(s)
      try {
        Thread.sleep(1000 / rate)
      } catch (e: InterruptedException) {
        throw RuntimeException(e)
      }
    }
    println()
    readlnOrNull()
  }

  /** Clear the terminal window. */
  fun clearScreen() {
    print(FunString.escape(CLEAR_RESET) + FunString.escape(CLEAR))
  }

  /**
   * Display all user items.
   *
   * @param projection The source for the items.
   */
  fun displayItems(projection: GameProjection) {
    val names = projection.currentItems().map { it.display }
    if (names.isEmpty()) {
      pauseDisplay("You have no items.")
      return
    }
    val lines = ArrayList<String>()
    val padwidth = (names.maxOfOrNull { it.length } ?: 0) + 1
    val formatString = { str: String -> String.format("1✖ %-" + padwidth + "s", str) }
    var i = 0
    while (i < names.size) {
      val left = formatString(names[i] + (if (i + 1 == names.size) "" else ","))
      val right = if (i + 1 == names.size) "" else formatString(names[i + 1])
      lines.add(left + " " + right)
      i += 2
    }
    pauseDisplay("You have: \n" + lines.joinToString("\n"))
  }

  /**
   * Provides progress bar for user to track their progress through the game
   *
   * @param projection
   * @return
   */
  private fun getProgressBar(projection: GameProjection): String {
    val totalFloors: Int = Floor.entries.size

    // finds floor
    val currentFloorIndex =
      (0 until totalFloors).firstOrNull { i ->
        Floor.entries[i].name == projection.currentFloor().name
      } ?: 0

    // Progress is a simple fraction of floors completed
    val progressPercentage = ((currentFloorIndex + 1).toFloat() / totalFloors * 100).toInt()

    // Define bar length (here it is arbitrarily 70)
    val barWidth = 70
    val progressChars = ((currentFloorIndex + 1).toFloat() / totalFloors * barWidth).toInt()

    val completed = "=".repeat(max(0, progressChars))
    val remaining = ".".repeat(max(0, barWidth - progressChars))

    // FunString!!
    val bar =
      FunString("[")
        .add(FunString(completed).green())
        .add(FunString(remaining).terminalColor(240)) // Light Gray
        .add(FunString("]"))

    val progressText =
      String.format(
        "Floor %d of %d (%d%%)",
        currentFloorIndex + 1,
        totalFloors,
        progressPercentage,
      )

    return progressText + "\n" + bar
  }

  /**
   * Present the end of the game, record score, and display the leaderboard.
   *
   * @param projection The source for data.
   */
  fun menu_ending(projection: GameProjection) {
    // leaderboard + logout

    val currentAccount = projection.account

    val usernameToRecord = currentAccount.username

    //        leaderboard.recordSession(usernameToRecord);

    // hint data
    val hintsUsedMap: MutableMap<String, Int> = projection.state.hintsUsed
    val totalHintsUsed: Int = hintsUsedMap.values.sum()

    val hintsListDisplay =
      if (totalHintsUsed > 0)
        hintsUsedMap.entries.joinToString(
          "\n",
          transform = { entry -> entry.key + ": " + entry.value },
        )
      else "None"

    var contributors =
      (0 until 4)
        .map { i -> projection.state.language.string("credits", "contributor_$i") }
        .map { it: String ->
          val j = it.split(Regex("<")).dropLastWhile { it.isEmpty() }.toTypedArray()
          j[0] + FunString("<" + j[1]).italic().terminalColor(50)
        }
        .shuffled()

    val formattedTime = String.format("%02d:%02d", projection.time(), projection.time())
    val scoremsg =
      String.format(
        projection.state.language.string("credits", "score"),
        formattedTime,
        projection.state.difficulty,
        projection.state.score,
      )

    // num hints
    val hintmsg =
      String.format(projection.state.language.string("credits", "hints"), totalHintsUsed)

    // Each of the hints used
    val specificHintsMsg = "Specific hints used: $hintsListDisplay"

    val msg = ArrayList<String>()
    msg.add(FunString(scoremsg).purple().toString())
    msg.add(FunString(hintmsg).purple().toString())

    // list of the strings
    msg.add(FunString(specificHintsMsg).purple().toString())

    // main credits
    msg.addAll(
      projection.state.language.string("credits", "message").split(Regex("\n")).dropLastWhile {
        it.isEmpty()
      }
    )
    msg.add("Credits:")

    // contributors (that's us lets gooo)
    msg.addAll(contributors)

    // Loop to display the credits line by line
    for (s in msg) {
      println(s)
      try {
        Thread.sleep(750)
      } catch (e: InterruptedException) {
        throw RuntimeException(e)
      }
    }
    // Show
    pauseDisplay("Press enter to logout")
    projection.logout()
  }

  /**
   * Provide the user with a summary of their current progress when the sign into a preexisting
   * account.
   *
   * @param projection The source for data.
   */
  fun menu_summary(projection: GameProjection) {
    val resumeAction = makeTuiActionMap(FunString("Resume Game") to {})
    createActionInterface(
      resumeAction,
      String.format(
        projection.state.language.string("welcome", "welcome_back"),
        projection.account.username,
        getProgressBar(projection),
        projection.state.completedPuzzles.joinToString(", "),
        projection.state.hintsUsed.entries.joinToString("\n") { entry ->
          entry.key + ": " + entry.value
        },
      ),
    )
  }

  /**
   * Ask the user to change rooms.
   *
   * @param projection The source for data.
   */
  fun menu_changeRoom(projection: GameProjection) {
    val actions = makeTuiActionMap()
    for (room in projection.currentFloor().rooms) {
      actions.add(
        FunString(projection.language.string(room.id, "name")).bold() to
                {
                  projection.pickRoom(room)
                }
      )
    }
    actions.add(FunString("Nevermind") to {})
    createActionInterface(actions, "Change room")
  }

  /**
   * Ask the user to perform room-specific actions.
   *
   * @param projection The source for data.
   */
  fun menu_roomActions(projection: GameProjection) {
    val actions = makeTuiActionMap()
    // It makes no sense to change rooms if there are no rooms to change to!
    if (projection.currentFloor().rooms.size > 1) {
      actions.add(FunString("Change room") to { this.menu_changeRoom(projection) })
    }
    for (e in projection.currentRoom().entities) {
      actions.add(
        FunString(e.string(projection.language, "name")).italic() to { projection.pickEntity(e) }
      )
    }
    actions.add(FunString("Exit game") to { exit(projection) })
    actions.add(FunString("Options") to { menu_options(projection) })
    val prompt =
      projection.time().toMicrowaveTime() +
              "\n" +
              projection.language["introduce", projection.currentRoom().id]
    createActionInterface(actions, prompt)
  }

  /**
   * Ask the user to perform entity-specific actions.
   *
   * @param projection The source for data.
   */
  fun menu_entityAction(projection: GameProjection) {
    val prompt =
      projection.currentEntity()!!::string
        .let { { j: String -> it(projection.language, j) } }
        .let { FunString(it("name")).italic() + "\n" + it("introduce") }
    val actions = makeTuiActionMap()
    val capabilities = projection.capabilities()
    if (capabilities.interact)
      actions.add(FunString("Interact").blue() to { projection.interact() })
    if (capabilities.inspect) actions.add(FunString("Inspect").green() to { projection.inspect() })
    if (capabilities.attack) actions.add(FunString("Attack").red() to { projection.attack() })
    if (capabilities.input)
      actions.add(
        FunString("Speak").purple() to
                {
                  projection.input(validateInput("What would you like to say? ") { true })
                }
      )
    if (projection.currentItems().isNotEmpty())
      actions.add(FunString("Items") to { this.displayItems(projection) })

    actions.add(FunString("Leave") to { projection.leaveEntity() })

    val itemsCache = projection.currentItems()
    createActionInterface(actions, prompt)
    val newItems = ArrayList<Item>(projection.currentItems())
    projection.currentMessage()?.let { i -> typewriterDisplay(i) }
    if (newItems.size > itemsCache.size) {
      newItems.removeIf { o -> itemsCache.contains(o) }
      print("\u0007")
      pauseDisplay("You received " + newItems.first().display + ".")
    }
  }

  /**
   * Ask the user to change the difficulty.
   *
   * @param projection The source for data.
   */
  fun menu_difficulty(projection: GameProjection) {
    val actions = makeTuiActionMap()

    for (diff in Difficulty.entries) {
      actions.add(
        FunString(diff.name).terminalColor(diff.ordinal + 196) to
                {
                  projection.setDifficulty(diff)
                }
      )
    }
    actions.add(FunString("Nevermind") to {})
    createActionInterface(actions, "Choose difficulty")
  }

  /**
   * Debug floor switching menu. Is inaccessible if [TerminalDriver.DEBUG] variable is `false`.
   *
   * @param projection The source for data.
   */
  fun menu_debugSwitchFloor(projection: GameProjection) {
    val actions = makeTuiActionMap()
    for (floor in Floor.entries) {
      actions.add(FunString(floor.name).green() to { projection.state.floor = floor })
    }
    actions.add(FunString("Nevermind") to {})
    createActionInterface(actions, "Pick floor")
  }

  /**
   * Debug options. Is inaccessible if [TerminalDriver.DEBUG] variable is `false`.
   *
   * @param projection The source for data.
   */
  fun menu_debug(projection: GameProjection) {
    val actions =
      makeTuiActionMap(FunString("Switch floor") to { menu_debugSwitchFloor(projection) })
    actions.forEach { k -> k.first.green() }
    actions.add(FunString("Nevermind") to {})
    createActionInterface(actions, "")
  }

  /**
   * Ask the user to select an option to change.
   *
   * @param projection The source for data.
   */
  fun menu_options(projection: GameProjection) {
    val actions =
      makeTuiActionMap(
        FunString("Set difficulty") to { menu_difficulty(projection) },
        FunString("Nevermind") to {},
      )
    if (DEBUG) {
      actions.add(0, FunString("DEBUG").green() to { menu_debug(projection) })
    }
    createActionInterface(actions, "Options")
  }

  /**
   * Exit the game.
   *
   * @param projection The source for data.
   */
  fun exit(projection: GameProjection) {
    projection.logout()
    display("Logged out.\nThanks for playing!")
    System.exit(0)
  }

  private fun menu_prelude(projection: GameProjection) {
    pauseDisplay(projection.state.language.string("welcome", "prelude"))
  }

  /**
   * Continuously ask the user for context-specific input.
   *
   * @param projection The source for data.
   */
  fun gameLoop(projection: GameProjection) {
    while (true) {
      if (projection.isEnded) {
        // If the game is over (due to win or time run out), call menu_ending.
        // menu_ending now handles: display, recording, leaderboard, pause, and logout.
        this.menu_ending(projection)
        return // Exit the game loop
      }
      if (projection.currentEntity() != null) menu_entityAction(projection)
      else menu_roomActions(projection)
    }
  }

  /** Program entrance. */
  fun main() {
    val projection =
      GameProjection(
        { path: String -> Path(path).readText() },
        { path: String, data: String -> Path(path).writeText(data) },
      )
    // Ensure these are initialized
    var flag = false
    while (!flag) {
      val actions =
        makeTuiActionMap(
          FunString("Login") to
                  {
                    flag = tryLogin { username, password -> projection.login(username, password) }
                    if (flag) menu_summary(projection)
                  },
          FunString("Create Account") to
                  {
                    flag = tryLogin { username, password -> projection.createAccount(username, password) }
                    if (flag) menu_prelude(projection)
                  },
        )
      createActionInterface(actions, "Login")
    }
    gameLoop(projection)
  }

  companion object {
    private const val CLEAR = "2J"
    private const val CLEAR_RESET = "H"
    private const val MOVE_LINE = "1A"
    private const val CLEAR_BELOW = "0J"
  }
}

fun main() {
  TerminalDriver().main()
}
