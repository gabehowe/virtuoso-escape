@file:OptIn(ExperimentalUuidApi::class)
package org.virtuoso.escape.model.data

import org.virtuoso.escape.TestHelper
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi

class DataLoaderTests {

    @BeforeTest
    fun pre() {
        // We set FILE_READER in each test or here
        DataLoader.FILE_READER = TestHelper.FILE_READER(this::class)
    }

    @Test
    fun testLoadAccounts() {

        val accounts = DataLoader.loadAccounts()
        assertNotNull(accounts)
        // Check if dummy exists
        // Uuid string: "7766f361-af7a-4da5-b741-6867d1768d45"
        val key = accounts.keys.firstOrNull { it.toString() == "7766f361-af7a-4da5-b741-6867d1768d45" }
        assertNotNull(key)
        val dummy = accounts[key]
        assertNotNull(dummy)
        assertEquals("dummy", dummy.username)
    }

    @Test
    fun testLoadGameStates() {

        val states = DataLoader.loadGameStates()
        assertNotNull(states)
        val key = states.keys.firstOrNull { it.toString() == "7766f361-af7a-4da5-b741-6867d1768d45" }
        assertNotNull(key)
        val state = states[key]
        assertNotNull(state)
        assertEquals("narrator_start", state.room.entities.flatMap { e -> e.states.values }.firstOrNull { it.id == "narrator_start" }?.id)
        assertTrue(states.isNotEmpty())
    }


}
