package org.virtuoso.escape.model

import org.virtuoso.escape.model.Floor.Initializers.floor2
import org.virtuoso.escape.model.Floor.Initializers.floor3
import org.virtuoso.escape.model.Floor.Initializers.floor4
import org.virtuoso.escape.model.action.ActionType
import org.virtuoso.escape.model.action.Actions.addPenalty
import org.virtuoso.escape.model.action.Actions.chain
import org.virtuoso.escape.model.action.Actions.completePuzzle
import org.virtuoso.escape.model.action.Actions.conditional
import org.virtuoso.escape.model.action.Actions.giveItem
import org.virtuoso.escape.model.action.Actions.setFloor
import org.virtuoso.escape.model.action.Actions.setMessage
import org.virtuoso.escape.model.action.Actions.swapEntities
import org.virtuoso.escape.model.action.Actions.takeInput
import org.virtuoso.escape.model.action.InputActionType
import org.virtuoso.escape.model.action.Severity
import kotlin.math.pow
import kotlin.time.Duration

fun Duration.toMicrowaveTime(): String {
  return toComponents { minutes, seconds, _ ->
    "${minutes.toString().padStart(2, '0')}:${
      seconds.toString().padStart(2, '0')
    }"
  }
}

data class Language(private val data: Map<String, Map<String, String>>) {
  fun string(namespace: String, resource: String): String {
    return data[namespace]?.get(resource) ?: return "<$namespace/$resource>"
  }

  fun searchString(resource: String, vararg namespaces: String): String? {
    for (i in namespaces) data[i]?.get(resource)?.let {
      return it
    }
    return null
  }

  operator fun get(resource: String, vararg namespaces: String?): String? {
    for (i in namespaces) i?.let { i ->
      data[i]?.get(resource)?.let {
        return it
      }
    }
    return null
  }
}

/**
 * Enumeration for game difficulty.
 *
 * @author gabri
 */
enum class Difficulty {
  TRIVIAL,
  SUBSTANTIAL,
  VIRTUOSIC,
}

/**
 * A grouping of rooms with an id.
 *
 * @author gabri
 */
enum class Floor(val rooms: List<Room>) {

  AcornGrove(Initializers.acornGrove()),
  StoreyI(Initializers.floor1()),
  StoreyII(floor2()),
  StoreyIII(floor3()),
  StoreyIV(floor4());

  private object Initializers {
    fun acornGrove(): List<Room> {
      val intro_squirrel = Entity("intro_squirrel", {}, {}, {}, null)
      val portal_squirrel =
        Entity(
          "portal_squirrel",
          {},
          {},
          chain(completePuzzle("portal"), { setFloor(Floor.StoreyI)(it) }),
          null,
        )
      val narrator = makeNarrator("acorn_grove")
      val acornGrove_0 =
        Room("acorn_grove_0", mutableListOf(intro_squirrel, portal_squirrel, narrator))
      return listOf(acornGrove_0)
    }

    /**
     * Making the narrator.
     *
     * @param floorId, unique id per floor
     * @return the narrator
     */
    private fun makeNarrator(floorId: String): Entity {
      // Utility function to easily create the message setter
      val narratorMsg: (String) -> ActionType = { it: String ->
        { state: GameState -> setMessage(state.language, "narrator", it)(state) }
      }

      // No left
      val hintsUsedUp =
        EntityState(
          "narrator_hint_2",
          narratorMsg("attack"),
          narratorMsg("inspect"),
          narratorMsg("hints_exhausted"),
          null,
        )

      // 1 hint given
      val hint2Id = floorId + "_hint_2"
      val giveHint2 =
        chain(
          narratorMsg(hint2Id), // Give the specific hint text
          { it.hintsUsed[floorId] = 2 }, // Record the hint
          swapEntities("narrator", "narrator_hint_2"), // Swap to final state
        )
      val hint1Given =
        EntityState(
          "narrator_hint_1",
          narratorMsg("attack"),
          narratorMsg("inspect"),
          giveHint2, // Interact action: Give Hint 2 and move to final state
          null,
        )

      // No hints given
      val hint1Id = floorId + "_hint_1"
      val giveHint1 =
        chain(
          narratorMsg(hint1Id), // Give the specific hint text
          { it.hintsUsed[floorId] = 1 }, // Record the hint
          swapEntities("narrator", "narrator_hint_1"), // Swap to hint1Given state
        )
      val start =
        EntityState(
          "narrator_start",
          narratorMsg("attack"),
          narratorMsg("inspect"),
          giveHint1, // Interact action: Give Hint 1 and move to next state
          null,
        )
      return Entity("narrator", start, hint1Given, hintsUsedUp)
    }

    fun floor1(): List<Room> {

      val door = Entity("first_door", {}, {}, { setFloor(Floor.StoreyII)(it) }, null)
      val narrator = makeNarrator("storey_i")
      val hummus_trash_can =
        EntityState(
          "trash_can",
          chain(
            giveItem(Item.SealedCleanFoodSafeHummus),
            completePuzzle("trash"),
            swapEntities("trash_can", "sans_hummus_trash_can"),
          ),
          {},
          {},
          null,
        )
      val sans_hummus_trash_can = EntityState("sans_hummus_trash_can", {}, {}, {}, null)
      val trash_can = Entity("trash_can", hummus_trash_can, sans_hummus_trash_can)

      val joeHardy = joeHardy()

      val hummus_elephant =
        EntityState(
          "elephant_in_the_room",
          {},
          {},
          chain(
            giveItem(Item.SunflowerSeedButter),
            completePuzzle("elephant"),
            swapEntities("elephant_in_the_room", "sans_butter_elephant"),
          ),
          null,
        )
      val sans_butter_elephant = EntityState("sans_butter_elephant", {}, {}, {}, null)
      val elephant = Entity("elephant_in_the_room", hummus_elephant, sans_butter_elephant)

      val room_1400 =
        Room("storey_i_0", mutableListOf(joeHardy, trash_can, elephant, door, narrator))

      val almanac = makeAlmanacs(5)
      val janitor_closet = Room("storey_i_1", almanac)

      val securityBread =
        Entity(
          "security",
          {},
          {},
          {},
          takeInput(
            listOf(
              ".*(?<!w)right.*" to
                      chain(
                        setMessage("security", "right_answer"),
                        giveItem(Item.RightBread),
                        completePuzzle("right"),
                      ),
              ".*" to
                      chain(
                        addPenalty(Severity.LOW),
                        setMessage("security", "non_right_answer"),
                      ),
            )
          ),
        )
      val hallway = Room("storey_i_2", securityBread)

      return listOf(room_1400, janitor_closet, hallway)
    }

    /**
     * Create Joe Hardy for floor 1.
     *
     * @return Joe Hardy.
     */
    private fun joeHardy(): Entity {
      val hasItems: (List<Item>) -> (GameState) -> Boolean = { items ->
        { state -> items.map { state.hasItem(it) }.reduce { a, b -> a && b } }
      }
      val sandwichJoe = EntityState("sandwich_joe", {}, {}, {}, null)
      val sansSandwichJoe =
        EntityState(
          "sans_sandwich_joe",
          {},
          {},
          conditional(
            hasItems(
              listOf(
                Item.LeftBread,
                Item.RightBread,
                Item.SunflowerSeedButter,
                Item.SealedCleanFoodSafeHummus,
              )
            ),
            chain(
              setMessage("sans_sandwich_joe", "interact_sandwich"),
              swapEntities("joe_hardy", "sandwich_joe"),
              { it.items.clear() },
            ),
          ),
          null,
        )
      val introJoe =
        EntityState(
          "intro_joe",
          {},
          {},
          chain(completePuzzle("sandwich"), swapEntities("joe_hardy", "sans_sandwich_joe")),
          null,
        )
      return Entity("joe_hardy", introJoe, sansSandwichJoe, sandwichJoe)
    }

    /**
     * Returns almanacs for a binary search puzzle
     *
     * @param length The number of guesses
     * @return An entity with states for each almanac guess
     */
    private fun makeAlmanacs(length: Int): Entity {
      val PAGES = 2.0.pow(length.toDouble()).toInt()
      val CORRECT_PAGE = (0 until PAGES).random()

      val almanacStates = mutableListOf<EntityState>()
      for (i in 0..<length) {
        val map =
          (1..PAGES)
            .associate { it.toString() to turnPage(length - i, it, length, CORRECT_PAGE) }
            .toList()
        val alm = { stringId: String -> setMessage("almanac", stringId) }
        almanacStates.add(
          EntityState(
            "almanac_${length - i}",
            alm("attack"),
            alm("inspect"),
            alm("interact"),
            takeInput(map),
          )
        )
      }
      almanacStates.add(EntityState("found_almanac"))
      return Entity("almanac", *almanacStates.toTypedArray())
    }

    /**
     * Test the user input page.
     *
     * @param flips Attempts remaining
     * @param currentPage The page attempted.
     * @param maxFlips The total number of attemptable flips.
     * @param correctPage The correct, desired page
     * @return An Action to be run later.
     */
    private fun turnPage(
      flips: Int,
      currentPage: Int,
      maxFlips: Int,
      correctPage: Int,
    ): ActionType {
      val swap =
        if (flips > 1) swapEntities("almanac", "almanac_" + (flips - 1))
        else swapEntities("almanac", "almanac_$maxFlips")

      val caseBreak = chain(addPenalty(Severity.MEDIUM), setMessage("almanac", "break"))
      val guessesRemaining: (Language) -> String = { language ->
        language.string("almanac", "guesses_remaining").split("%s").let {
          it[0] + (flips - 1) + it[1] + flips + it[2]
        }
      }
      val caseOvershoot: ActionType = {
        setMessage(it.language.string("almanac", "too_high") + " " + guessesRemaining(it.language))(
          it
        )
      }
      val caseUndershoot: ActionType = {
        setMessage(it.language.string("almanac", "too_low") + " " + guessesRemaining(it.language))(
          it
        )
      }
      val caseFound =
        chain(
          setMessage("almanac", "correct_page"),
          giveItem(Item.LeftBread),
          completePuzzle("almanac"),
          swapEntities("almanac", "found_almanac"),
        )
      val evaluatePage: ActionType =
        when {
          flips - 1 == 0 -> caseBreak
          currentPage > correctPage -> caseOvershoot
          currentPage < correctPage -> caseUndershoot
          else -> caseFound
        }

      return chain(swap, evaluatePage)
    }

    // Floor Two//
    /**
     * Build M. Bert Storey floor 2.
     *
     * @return M. Bert Storey floor 2.
     */
    fun floor2(): List<Room> {
      val doorRoom = Room("storey_ii_1", mutableListOf())
      val narrator = makeNarrator("storey_ii")
      val shuffle = { doorRoom.entities.shuffle() }
      val setDoors = { doorNum: Int ->
        { state: GameState ->
          require(doorNum <= 3)
          swapEntities("door2", "door2_$doorNum")(state)
          swapEntities("door3", "door3_$doorNum")(state)
        }
      }
      val failDoor =
        chain(
          addPenalty(Severity.MEDIUM),
          swapEntities("door1", "door1_2"),
          setMessage("door2", "interact"),
          { shuffle() },
          { it.leaveEntity() },
          setDoors(2),
        )

      val createDialogueDoorchain = { id: String ->
        val states = arrayOfNulls<EntityState>(3)
        val setMsg: (String, String) -> ActionType = { resource: String, state: String ->
          { setMessage(it.language.searchString(resource, state, id).orEmpty())(it) }
        }

        for (i in 0..2) {
          val stateId = id + "_" + i
          states[i] =
            EntityState(
              stateId,
              setMsg("attack", stateId),
              setMsg("inspect", stateId),
              failDoor,
              null,
            )
        }
        Entity(id, *states.map { it!! }.reversed().toTypedArray())
      }
      val door2 = createDialogueDoorchain("door2")
      val door3 = createDialogueDoorchain("door3")
      val door1 = createDoorchain(3, shuffle, setDoors)
      doorRoom.entities.addAll(listOf(door1, door2, door3, narrator))
      shuffle()

      val pitcherPlant = Entity("pitcher_plant")
      val plantOffice = Room("storey_ii_0", pitcherPlant)
      return listOf(plantOffice, doorRoom)
    }

    /**
     * Create a linked list of proper doors.
     *
     * @param length The number of doors in the linked list.
     * @param shuffle The function to shuffle doors.
     * @return A single door with an internal link to the next door.
     */
    private fun createDoorchain(
      length: Int,
      shuffle: () -> Unit,
      setDoors: (Int) -> ActionType,
    ): Entity {
      val door1 = arrayOfNulls<EntityState>(length)
      val sm: (String, String) -> ActionType = { stringId: String, state: String ->
        {
          setMessage(it.language.searchString(stringId, state, "door1") ?: "<$stringId/$state")(it)
        }
      }
      val door1_final =
        EntityState(
          "door1_0",
          sm("attack", "door1_0"),
          sm("inspect", "door1_0"),
          chain(
            completePuzzle("doors"),
            setMessage("door1", "final_door"),
            { setFloor(StoreyIII)(it) },
          ),
          null,
        )
      door1[length - 1] = door1_final
      for (i in 1..<length) {
        val id = "door1_$i"
        val next =
          EntityState(
            id,
            sm("attack", id),
            sm("inspect", id),
            chain(
              swapEntities("door1", "door1_" + (i - 1)),
              { it.leaveEntity() },
              { shuffle() },
              sm("interact", id),
              setDoors(i - 1),
            ),
            null,
          )
        door1[length - (i + 1)] = next
      }
      return Entity("door1", *door1.map { it!! }.toTypedArray())
    }

    // Floor Three//
    /**
     * Build M. Bert Storey floor 3.
     *
     * @return M. Bert Storey floor 3.
     */
    fun floor3(): List<Room> {
      val narrator = makeNarrator("storey_iii")
      // hi treasure
      val sparrowAmbassador = Entity("sparrow_ambassador", {}, {}, {}, null)

      val puzzleMsg = { string: String -> setMessage("box_riddle", string) }

      val box_open =
        EntityState(
          "box_open",
          {},
          {},
          chain(puzzleMsg("solved"), giveItem(Item.Keys), completePuzzle("boxes")),
          null,
        )

      val boxLogicSuccess =
        takeInput(
          listOf(
            "(?:box )?open" to
                    chain(swapEntities("box_riddle", "box_open"), puzzleMsg("step_success")),
            ".*" to chain(puzzleMsg("step_wrong"), swapEntities("box_riddle", "box_start")),
          )
        )
      val box_success = EntityState("box_success", {}, {}, {}, boxLogicSuccess)

      val boxLogicFollow =
        takeInput(
          listOf(
            "(?:box )?follow" to
                    chain(swapEntities("box_riddle", "box_success"), puzzleMsg("step_follow")),
            ".*" to chain(puzzleMsg("step_wrong"), swapEntities("box_riddle", "box_start")),
          )
        )
      val box_step1 = EntityState("box_step1", {}, {}, {}, boxLogicFollow)

      val boxLogicStart =
        takeInput(
          listOf(
            "(?:box )?start" to
                    chain(swapEntities("box_riddle", "box_step1"), puzzleMsg("step_start")),
            ".*" to puzzleMsg("step_wrong"),
          )
        )
      val box_start =
        EntityState(
          "box_start",
          puzzleMsg("attack"),
          puzzleMsg("inspect"),
          puzzleMsg("interact"),
          boxLogicStart,
        )

      val puzzleBox = Entity("box_riddle", box_start, box_step1, box_success, box_open)
      val doorMsg = { string: String -> setMessage("exit_door", string) }

      val goToFloor4 = chain(doorMsg("unlocked_success"), { setFloor(StoreyIV)(it) })
      val doorUnlocked =
        EntityState("exit_door_unlocked", {}, doorMsg("inspect_unlocked"), goToFloor4, null)

      val checkKeysAndSwap =
        conditional(
          { it.hasItem(Item.Keys) },
          chain(doorMsg("unlocked_msg"), swapEntities("exit_door", "exit_door_unlocked")),
          doorMsg("locked_msg"),
        )
      val doorLocked =
        EntityState("exit_door_locked", {}, doorMsg("inspect_locked"), checkKeysAndSwap, null)

      val exitDoor = Entity("exit_door", doorLocked, doorUnlocked)

      val boxRoom =
        Room("storey_iii_0", mutableListOf(sparrowAmbassador, puzzleBox, exitDoor, narrator))
      return listOf(boxRoom)
    }

    // Floor Four//
    fun floor4(): List<Room> {
      // Basic info entity -- provide logic by adding dialogue in language.json
      val man = Entity(
      "man",
      {},
      {},
      {},
      { i, s -> manHandler(i.removePrefix("man").trim(), s) }

    )
      val narrator = makeNarrator("storey_iv")

      val computty = makeComputtyLogic()
      val sock_squirrel =
        Entity(
          "sock_squirrel",
          chain(completePuzzle("unblock"), swapEntities("computty", "computty_unblocked")),
          {},
          {},
          null,
        )
      val microwave_blocked = EntityState("microwave_blocked", {}, {}, {}, null)
      // Whoops! JEP 126!
      val microwaveUnblocked =
        EntityState("microwave_unblocked", GameState::end, {}, GameState::end, null)
      val microwave = Entity("microwave", microwave_blocked, microwaveUnblocked)
      val floor4 =
        Room("storey_iv_0", mutableListOf(man, sock_squirrel, computty, microwave, narrator))
      return listOf(floor4)
    }

    /**
     * Create the man entity.
     *
     * @return the man entity.
     */

    val posix =
      listOf("admin", "alias", "ar", "asa", "at", "awk", "basename", "batch", "bc", "bg", "c99", "cal", "cflow", "chgrp", "chmod", "chown", "cksum", "cmp", "comm", "command", "compress", "cp", "crontab", "csplit", "ctags", "cut", "cxref", "date", "dd", "delta", "df", "diff", "dirname", "du", "echo", "ed", "env", "ex", "expand", "expr", "false", "fc", "fg", "file", "find", "fold", "fort77", "fuser", "gencat", "get", "getconf", "getopts", "grep", "hash", "head", "iconv", "id", "ipcrm", "ipcs", "jobs", "join", "kill", "lex", "link", "ln", "locale", "localedef", "logger", "logname", "lp", "m4", "mailx", "make", "mesg", "mkdir", "mkfifo", "more", "mv", "newgrp", "nice", "nl", "nm", "nohup", "od", "paste", "patch", "pathchk", "pax", "pr", "printf", "prs", "ps", "pwd", "qalter", "qdel", "qhold", "qmove", "qmsg", "qrerun", "qrls", "qselect", "qsig", "qstat", "qsub", "read", "renice", "rm", "rmdel", "rmdir", "sact", "sccs", "sed", "sh", "sleep", "sort", "split", "strings", "strip", "stty", "tabs", "tail", "talk", "tee", "test", "time", "touch", "tput", "tr", "true", "tsort", "tty", "type", "ulimit", "umask", "unalias", "uname", "uncompress", "unexpand", "unget", "uniq", "unlink", "uucp", "uudecode", "uuencode", "uustat", "uux", "val", "vi", "vim", "wait", "wc", "what", "who", "write", "xargs", "yacc", "zcat")
    private fun manHandler(page: String, state: GameState) {
      // @formatter:off
      // NOTE: Could be faster with trie
      val supported = listOf("help", "ls", "cd", "tar", "rotx", "cat", "man")
      // @formatter:on
      when (page) {
        in supported -> setMessage("man", "input_${page.lowercase()}")(state)
        in posix -> setMessage("man", "input_posix")(state)
      }
    }

    /**
     * Create CompuTTY -- a terminal entity.
     *
     * @return CompuTTY.
     */
    private fun makeComputtyLogic(): Entity {
      fun pathProcessor(path: String) =
        "(?:(?<!\\.)\\./|~/|[^/\\n]+/\\.\\./?)?".toRegex().replace(path, "").also { println(it) }

      // Dichotomy: DRY violation or unreadable code?
      val manMsg = { stringId: String -> setMessage("man", stringId) }
      val ttyStr = { string: String -> setMessage("computty_unblocked", string) }
      val files = mutableMapOf("code/code.tar" to ttyStr("cat_tar"))
      val code = "code/code" to ttyStr("cat_code")
      var cwd = ""
      val inputHandler: InputActionType = ih@{ input: String, state: GameState ->
        val args = input.trim().split(" ").map { it.trim() }
        val improper_usage = {
          setMessage(state.language["man_x", "computty_unblocked"] + args[0])(state)
        }
        if (args.contains("--help") && args.size > 1) {
          manHandler(args[0], state)
          return@ih
        }
        when (args.firstOrNull() ?: return@ih) {
          "man" -> {
            if (args.size < 2) manHandler("man", state)
            else manHandler(args[1], state)
          }

          "cat" ->
            state.language["cat_" + pathProcessor(cwd + args[1]), "computty_unblocked"]?.let { setMessage(it)(state) }
              ?: ttyStr("no_file")(state)

          "ls" ->
            setMessage(
              files.keys
                .filter { it.startsWith(cwd + input.removePrefix("ls").trim()) }
                .joinToString(" ") { it.removePrefix(cwd).split("/")[0] }
            )(state)

          "tar" -> {
            if (args.size < 3 || pathProcessor(cwd + args[2]) != "code/code.tar") {
              return@ih
            }
            if (!args[1].matches("[xvf]{3}".toRegex())) {
              manMsg("input_posix")(state)
              return@ih
            }
            files[code.first] = code.second
            setMessage("code")(state)
          }

          "rotx" -> {
            if (args.size < 3) {
              improper_usage()
              return@ih
            }
            val file = pathProcessor(cwd + args[2])
            if (file !in files.keys) {
              ttyStr("no_file")(state)
              return@ih
            }
            val letters = ('a'..'z').toList()
            args[1].toIntOrNull().let { rot ->
              rot
                ?: improper_usage().also {
                  return@ih
                }
              setMessage(
                state.language["cat_${file}", "computty_unblocked"]!!
                  .map { if (it in letters) letters[(letters.indexOf(it) - rot).mod(letters.size)] else it }
                  .joinToString("")
              )(state)
            }
          }

          "cd" -> {
            if (args.size == 1) {
              cwd = ""
              return@ih
            }
            val path = pathProcessor(cwd + args[1])
            if (path !in listOf("code", "")) {
              ttyStr("failed_cd")(state)
              return@ih
            }
            cwd = path + (if (path.isNotEmpty()) "/" else "")
            setMessage("")(state)
            swapEntities("computty", "computty_${cwd.removeSuffix("/")}")(state)
          }

          "help" -> {
            if (args.size == 1) {
              manMsg("input_help")(state)
              return@ih
            }
            state.language["man_${args[2]}", "man"]?.let {
              setMessage(it)(state)
            }
          }

          in posix -> manMsg("input_posix")(state)
        }
      }

      val computtyBlocked = EntityState("computty_blocked")
      val computtyBuilder = { id: String, inputLogic: InputActionType ->
        EntityState(id, ttyStr("attack"), ttyStr("inspect"), ttyStr("interact"), inputLogic)
      }
      return Entity(
        "computty",
        computtyBlocked,
        computtyBuilder("computty_unblocked", inputHandler),
        computtyBuilder("computty_tar", inputHandler),
        computtyBuilder("computty_code", inputHandler),
      )
    }
  }
}

/**
 * A collection of entities and an introductory message.
 *
 * @param id The id of the room.
 * @param entities The list of entities in the room.
 */
data class Room(val id: String, val entities: MutableList<Entity>) {

  /**
   * Initialize with default introduce.
   *
   * @param id The id of the room.
   * @param entities The entities in the room.
   */
  constructor(id: String, vararg entities: Entity) : this(id, entities.toMutableList())
}

/**
 * An enumeration of items to be held in an inventory list.
 *
 * @author gabri
 */
enum class Item(val display: String) {
  LeftBread("left bread"),
  SunflowerSeedButter("sunflower seed butter"),
  RightBread("right bread"),
  SealedCleanFoodSafeHummus("sealed clean food-safe hummus"),
  Keys("keys"),
}
