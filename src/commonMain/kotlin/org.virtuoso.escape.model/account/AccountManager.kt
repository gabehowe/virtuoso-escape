@file:OptIn(ExperimentalUuidApi::class)

package org.virtuoso.escape.model.account

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import org.virtuoso.escape.model.Data
import org.virtuoso.escape.model.GameState

/**
 * Manages user accounts.
 *
 * @author Treasure
 */
@Serializable
data class AccountManager(val accounts: Map<Uuid, Account>, val gameStates: Map<Uuid, GameState>) {
  /** Loads all user accounts and gamestates. */
  constructor() : this(Data.loadAccounts(), Data.loadGameStates())
}
