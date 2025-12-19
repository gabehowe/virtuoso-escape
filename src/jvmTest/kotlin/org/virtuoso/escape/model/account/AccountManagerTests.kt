@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model.account

import org.virtuoso.escape.TestHelper
import org.virtuoso.escape.model.GameProjection
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi

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
    assertFailsWith<AccountManager.AccountError> { manager.login("fake", "fake") }
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
    assertFailsWith<AccountManager.AccountError> { manager.newAccount("dummy", "novel") }
  }

  @Test
  fun testTryCreateLargeUsernameAccount() {
    assertFailsWith<AccountManager.AccountError> { manager.newAccount("a".repeat(33), "novel") }
  }

  @Test
  fun testTryCreateLargePasswordAccount() {
    assertFailsWith<AccountManager.AccountError> { manager.newAccount("novel", "a".repeat(33)) }
  }

  @Test
  fun testInvalidLoginInfo() {
    val cases =
      listOf(
        Triple("dummy", "wrong", "Username or password is invalid."),
        Triple("wrong", "wrong", "Username or password is invalid."),
        Triple("wrong", "dummy", "Username or password is invalid."),
      )

    for ((u, p, expected) in cases)
      assertEquals(assertFailsWith<AccountManager.AccountError> {
        manager.login(u, p)
      }.message, expected)
  }

  @Test
  fun testInvalidAccountData() {
    assertFailsWith<AccountManager.AccountError>{ manager.login("a", "a") }
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
