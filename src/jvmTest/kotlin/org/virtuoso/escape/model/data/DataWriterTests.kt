@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model.data

import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import org.virtuoso.escape.TestHelper
import org.virtuoso.escape.model.GameProjection

class DataWriterTests {
  private lateinit var proj: GameProjection

  @BeforeTest
  fun pre() {
    proj = GameProjection(TestHelper.FILE_READER(this::class), { _, _ -> })
  }

  @Test
  fun testWriteAccount() {
    var writtenPath: String? = null
    var writtenData: String? = null

    DataWriter.FILE_WRITER = { path, data ->
      writtenPath = path
      writtenData = data
    }

    proj.createAccount("dummy", "dummy")

    DataWriter.writeAccount(proj.account, proj.accountManager)

    assertEquals(DataWriter.ACCOUNTS_PATH, writtenPath)
    assertNotNull(writtenData)
    assertTrue(writtenData!!.contains("dummy"))
  }

  @Test
  fun testWriteGameState() {
    var writtenPath: String? = null
    var writtenData: String? = null

    DataWriter.FILE_WRITER = { path, data ->
      writtenPath = path
      writtenData = data
    }

    proj.createAccount("dummy", "dummy")
    DataWriter.writeGameState(proj.state, proj.account, proj.accountManager)

    assertEquals(DataWriter.GAMESTATES_PATH, writtenPath)
    assertNotNull(writtenData)
    assertTrue(writtenData!!.contains("difficulty"))
  }
}
