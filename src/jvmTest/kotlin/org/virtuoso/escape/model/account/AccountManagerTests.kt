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
    proj.account.logout(proj.accountManager.accounts)
  }

  @Test
  fun testBadLogin() {
    assertFailsWith<Account.AccountError> {
      Account.login("fake", "fake", proj.accountManager.accounts)
    }
  }

  @Test
  fun testBadLogout() {
    val dummy = Account("fake", "fake")
    dummy.logout(proj.accountManager.accounts)
  }

  @Test
  fun testNewAccount() {
    val account = Account.newAccount("novel", "novel", proj.accountManager.accounts)
    assertNotNull(account)
  }

  @Test
  fun testTryCreateExistingAccount() {
    val account = Account.newAccount("dummy", "dummy", proj.accountManager.accounts)
    assertNotNull(account)
  }

  @Test
  fun testTryCreateCollidingAccount() {
    assertFailsWith<Account.AccountError> {
      Account.newAccount("dummy", "novel", proj.accountManager.accounts)
    }
  }

  @Test
  fun testTryCreateLargeUsernameAccount() {
    assertFailsWith<Account.AccountError> {
      Account.newAccount("a".repeat(33), "novel", proj.accountManager.accounts)
    }
  }

  @Test
  fun testTryCreateLargePasswordAccount() {
    assertFailsWith<Account.AccountError> {
      Account.newAccount("novel", "a".repeat(33), proj.accountManager.accounts)
    }
  }

  @Test
  fun testInvalidLoginInfo() {
    val cases =
        listOf(
            Triple("dummy", "wrong", "Username or password is invalid."),
            Triple("wrong", "wrong", "Username or password is invalid."),
            Triple("wrong", "dummy", "Username or password is invalid."),
        )

    for ((u, p, expected) in cases) assertEquals(
        assertFailsWith<Account.AccountError> { Account.login(u, p, proj.accountManager.accounts) }
            .message,
        expected,
    )
  }

  @Test
  fun testInvalidAccountData() {
    assertFailsWith<Account.AccountError> { Account.login("a", "a", proj.accountManager.accounts) }
  }

  @Test
  fun testData() {
    login()
    assertTrue(proj.currentItems().isEmpty())
    proj.account.logout(proj.accountManager.accounts)
  }

  @Test
  fun testGameStates() {
    assertNotNull(manager.gameStates)
  }
}
