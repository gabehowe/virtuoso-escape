@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model

import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import org.virtuoso.escape.TestHelper

/** @author gabri */
class GameInfoTests {
  private lateinit var proj: GameProjection

  @BeforeTest
  fun pre() {
    // Setup mock DataLoader
    proj = GameProjection(TestHelper.FILE_READER(this::class), TestHelper.DUMMY_WRITER)
    proj.login("dummy", "dummy")
  }

  @AfterTest
  fun post() {
    if (::proj.isInitialized) proj.logout()
  }

  @Test
  fun testWrongFloorStateChange() {
    proj.state.floor = Floor.StoreyI
    testJoeHardyNoItems()
  }

  @Test
  fun testMoveToFloor1() {
    val introRoom = Floor.AcornGrove.rooms.first()
    val portalSquirrel = introRoom.entities.first { it.id == "portal_squirrel" }

    proj.state.floor = Floor.AcornGrove
    proj.state.room = introRoom
    proj.pickEntity(portalSquirrel)

    proj.interact()

    assertEquals(Floor.StoreyI, proj.state.floor)
  }

  @Test
  fun testJoeHardyNoItems() {
    proj.state.items.clear()
    val floor = Floor.StoreyI
    val hardyRoom = floor.rooms.first() // storey_i_0

    proj.state.floor = floor
    proj.pickRoom(hardyRoom)

    val hardy = hardyRoom.entities.first { it.id == "joe_hardy" }

    hardy.swapState("sans_sandwich_joe")
    assertEquals("sans_sandwich_joe", hardy.state()!!.id)

    proj.pickEntity(hardy)
    proj.interact()

    assertEquals("sans_sandwich_joe", hardy.state()!!.id)
  }

  @Test
  fun testJoeHardyWithItems() {
    proj.state.items.add(Item.LeftBread)
    proj.state.items.add(Item.RightBread)
    proj.state.items.add(Item.SunflowerSeedButter)
    proj.state.items.add(Item.SealedCleanFoodSafeHummus)

    val floor = Floor.StoreyI
    val hardyRoom = floor.rooms.first()
    proj.state.floor = floor
    proj.pickRoom(hardyRoom)

    val hardy = hardyRoom.entities.first { it.id == "joe_hardy" }

    hardy.swapState("sans_sandwich_joe")
    assertEquals("sans_sandwich_joe", hardy.state()!!.id)

    proj.pickEntity(hardy)
    proj.interact()

    assertEquals("sandwich_joe", hardy.state()!!.id)
  }

  private fun narratorTest(expectedResource: String, stateName: String) {
    val floor = Floor.StoreyI
    val room = floor.rooms.first()
    val narrator = room.entities.first { it.id.contains("narrator") }

    proj.state.floor = floor
    proj.pickRoom(room)

    narrator.swapState(stateName)
    proj.pickEntity(narrator)
    proj.interact()

    val msg = proj.currentMessage()
    assertNotNull(msg)
    assertEquals(proj.language.string("narrator", expectedResource), msg)
  }

  @Test
  fun testFirstNarrator() {
    narratorTest("storey_i_hint_1", "narrator_start")
  }

  @Test
  fun testSecondNarrator() {
    narratorTest("storey_i_hint_2", "narrator_hint_1")
  }

  @Test
  fun testThirdNarrator() {
    narratorTest("hints_exhausted", "narrator_hint_2")
  }

  @Test
  fun testEnding() {
    val floor4 = Floor.StoreyIV
    val finfoyer = floor4.rooms.first()
    val microwave = finfoyer.entities.first { it.id == "microwave" }

    proj.state.floor = floor4
    proj.pickRoom(finfoyer)

    microwave.swapState("microwave_unblocked")
    proj.pickEntity(microwave)
    proj.interact()

    assertTrue(proj.isEnded)
  }

  @Test
  fun testInvalidLanguageKey() {
    val inputId = "impossibly-never-ever-possible-exact-language-string"
    val result = proj.language.string(inputId, "")
    assertTrue(result.contains(inputId))
  }

  @Test
  fun floor3DoorKeyCheck() {
    for (has in listOf(true, false)) {
      proj.state.items.clear()
      if (has) proj.state.items.add(Item.Keys)

      val floor3 = Floor.StoreyIII
      proj.state.floor = floor3
      val room = floor3.rooms.first()
      proj.pickRoom(room)

      val exitDoor = room.entities.first { it.id == "exit_door" }

      exitDoor.swapState("exit_door_locked")

      proj.pickEntity(exitDoor)
      proj.interact()

      val msg = proj.currentMessage()
      val expected = if (has) "unlocked_msg" else "locked_msg"
      assertEquals(proj.language.string("exit_door", expected), msg)
    }
  }

  @Test
  fun testAlmanacFind() {
    setupAlmanac()
    val almanac = proj.currentEntity()!!

    var low = 0
    var high = 32
    var correct: Int? = null

    for (i in 0..4) {
      val guess = (high + low) / 2
      proj.input(guess.toString())
      val msg = proj.currentMessage() ?: ""
      println(msg)

      if (msg.contains("later")) {
        low = guess
        continue
      }
      if (msg.contains("earlier")) {
        high = guess
        continue
      }
      if (msg.contains("the right page")) {
        correct = guess
        break
      }
      fail("Failed to find correct almanac page with binary search.")
    }
    assertNotNull(correct)
  }

  @Test
  fun testAlmanacWrong() {
    setupAlmanac()
    val almanac = proj.currentEntity()!!

    proj.input("31")
    proj.input("30")

    val msg = proj.currentMessage() ?: ""
    assertTrue(msg.contains("earlier") || msg.contains("later"), "Invalid message: $msg")
  }

  @Test
  fun testAlmanacBreak() {
    setupAlmanac()
    val almanac = proj.currentEntity()!!

    var guess = "31"
    proj.input(guess)
    if (proj.currentMessage()?.contains("correct_page") == true) {
      guess = "30"
    }

    almanac.swapState("almanac_5")
    for (i in 0..4) {
      proj.input(guess)
    }
    val msg = proj.currentMessage() ?: ""
    assertEquals(msg, proj.language.string("almanac", "break"))
  }

  private fun setupAlmanac() {
    val floor = Floor.StoreyI
    val almanacRoom = floor.rooms[1]
    proj.state.floor = floor
    proj.pickRoom(almanacRoom)
    val almanac = almanacRoom.entities.first()
    proj.pickEntity(almanac)
    almanac.swapState("almanac_5")
  }
}
