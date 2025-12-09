@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model.account

import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import org.virtuoso.escape.TestHelper
import org.virtuoso.escape.model.GameProjection

class AccountManagerTests {
  private lateinit var proj: GameProjection
  private lateinit var manager: AccountManager

  @BeforeTest
  fun pre() {
    proj = GameProjection(TestHelper.FILE_READER(this::class)) { _, _ -> }
    manager = proj.accountManager
  }

  private fun login(): Boolean {
    return proj.login("dummy", "dummy")
  }

  @Test
  fun testLogin() {
    assertTrue(login())
    manager.logout(proj.account)
  }

  @Test
  fun testBadLogin() {
    val account = manager.login("fake", "fake")
    assertNull(account)
  }

  @Test
  fun testBadLogout() {
    val dummy = Account("fake", "fake")
    manager.logout(dummy)
  }

  @Test
  fun testNewAccount() {
    val account = manager.newAccount("novel", "novel")
    assertNotNull(account)
  }

  @Test
  fun testTryCreateExistingAccount() {
    val account = manager.newAccount("dummy", "dummy")
    assertNotNull(account)
  }

  @Test
  fun testTryCreateCollidingAccount() {
    val account = manager.newAccount("dummy", "novel")
    assertNull(account)
  }

  @Test
  fun testTryCreateLargeUsernameAccount() {
    val account = manager.newAccount("a".repeat(33), "novel")
    assertNull(account)
  }

  @Test
  fun testTryCreateLargePasswordAccount() {
    val account = manager.newAccount("novel", "a".repeat(33))
    assertNull(account)
  }

  @Test
  fun testInvalidLoginInfo() {
    val cases =
        listOf(
            Triple("dummy", "wrong", "Password input is invalid."),
            Triple("wrong", "wrong", "Both username and password input is invalid."),
            Triple("wrong", "dummy", "Username input is invalid."),
        )

    for ((u, p, expected) in cases) {
      assertEquals(expected, manager.invalidLoginInfo(u, p))
    }
  }

  @Test
  fun testInvalidAccountData() {
    assertNull(manager.accountExists("a", "a"))
  }

  @Test
  fun testData() {
    login()
    assertTrue(proj.currentItems().isEmpty())
    manager.logout(proj.account)
  }

  @Test
  fun testGameStates() {
    assertNotNull(manager.gameStates)
  }
}
