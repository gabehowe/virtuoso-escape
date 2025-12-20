@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model.account

import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import org.virtuoso.escape.TestHelper
import org.virtuoso.escape.model.GameProjection

class AccountManagerTests {
  private lateinit var proj: GameProjection

  @BeforeTest
  fun pre() {
    proj = GameProjection(TestHelper.FILE_READER(this::class)) { _, _ -> }
  }

  private fun login(): Boolean {
    return proj.login("dummy", "dummy")
  }

  @Test
  fun testLogin() {
    assertTrue(login())
    proj.account.logout(proj.accounts)
  }

  @Test
  fun testBadLogin() {
    assertFailsWith<Account.AccountError> {
      Account.login("fake", "fake", proj.accounts)
    }
  }

  @Test
  fun testBadLogout() {
    val dummy = Account("fake", "fake")
    dummy.logout(proj.accounts)
  }

  @Test
  fun testNewAccount() {
    val account = Account.newAccount("novel", "novel", proj.accounts)
    assertNotNull(account)
  }

  @Test
  fun testTryCreateExistingAccount() {
    val account = Account.newAccount("dummy", "dummy", proj.accounts)
    assertNotNull(account)
  }

  @Test
  fun testTryCreateCollidingAccount() {
    assertFailsWith<Account.AccountError> {
      Account.newAccount("dummy", "novel", proj.accounts)
    }
  }

  @Test
  fun testTryCreateLargeUsernameAccount() {
    assertFailsWith<Account.AccountError> {
      Account.newAccount("a".repeat(33), "novel", proj.accounts)
    }
  }

  @Test
  fun testTryCreateLargePasswordAccount() {
    assertFailsWith<Account.AccountError> {
      Account.newAccount("novel", "a".repeat(33), proj.accounts)
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
        assertFailsWith<Account.AccountError> { Account.login(u, p, proj.accounts) }
            .message,
        expected,
    )
  }

  @Test
  fun testInvalidAccountData() {
    assertFailsWith<Account.AccountError> { Account.login("a", "a", proj.accounts) }
  }

  @Test
  fun testData() {
    login()
    assertTrue(proj.currentItems().isEmpty())
    proj.account.logout(proj.accounts)
  }

  @Test
  fun testGameStates() {
    assertNotNull(proj.gamestates)
  }
}
