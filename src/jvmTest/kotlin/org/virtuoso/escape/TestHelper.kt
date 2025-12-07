package org.virtuoso.escape

import org.virtuoso.escape.model.data.DataLoader
import java.io.InputStreamReader
import kotlin.reflect.KClass

object TestHelper {
    val FILE_READER = { current: KClass<out Any>->
        { path: String ->
            val resourcePath = when {
                path.endsWith("accounts.json") -> current.java.getResourceAsStream("accounts.json")
                path.endsWith("gamestates.json") -> current.java.getResourceAsStream("gamestates.json")
                path.endsWith("language.json") -> current.java.getResourceAsStream("language.json")
                    ?: current.java.getResourceAsStream("json/language.json")

                else -> throw IllegalArgumentException("Unknown path request: $path")
            }

//            val stream = javaClass.getResourceAsStream(resourcePath)
//                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

            InputStreamReader(resourcePath).use { it.readText() }
        }
    }
    val DUMMY_WRITER = {_:Any, _:Any, ->}
}
