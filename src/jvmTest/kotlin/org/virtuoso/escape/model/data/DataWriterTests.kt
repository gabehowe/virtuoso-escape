@file:OptIn(ExperimentalUuidApi::class)
package org.virtuoso.escape.model.data

import org.virtuoso.escape.model.GameProjection
import org.virtuoso.escape.model.account.AccountManager
import org.virtuoso.escape.TestHelper
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi

class DataWriterTests {
    private lateinit var proj: GameProjection

    @BeforeTest
    fun pre() {
         proj = GameProjection({""},{_,_->})
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
