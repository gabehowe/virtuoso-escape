import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlinx.html.dom.append
import kotlinx.html.js.b
import kotlinx.html.js.span
import org.virtuoso.escape.model.GameProjection
import org.virtuoso.escape.model.account.AccountManager
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement

object Login {
  val projection: GameProjection = window.asDynamic().projection

  enum class AuthMode {
    Login,
    Create,
  }

  var authMode = AuthMode.Login

  fun setupListeners() {
    (document.getElementById("auth-change") as? HTMLSpanElement)?.onclick = {
      this.toggleAuthMode()
    }
    document.onsubmit = { ev ->
      ev.preventDefault()
      tryAuth()
    }
    (document.getElementById("enter-box") as? HTMLElement)!!.onclick = document.onsubmit
  }

  fun run(projection: GameProjection?) =
    MainScope().promise {
      updateKeyHandler("c")
      setupListeners()
      toggleAuthMode()
    }

  fun updateKeyHandler(key: String) {
    document.onkeydown = keydown@{ ev ->
      if (ev.key == "Escape") (document.activeElement as HTMLElement).blur()
      if (document.activeElement as? HTMLInputElement != null) return@keydown Unit
      if (ev.key == key) (document.getElementById("auth-change") as? HTMLElement)?.click()
    }
  }

  fun toggleAuthMode() {
    println(this.authMode)
    (document.getElementById("auth-prompt") as? HTMLSpanElement)?.apply {
      innerText =
        projection.language.string(
          "ui",
          if (authMode == AuthMode.Login) "switch_login" else "switch_create",
        )
    }

    (document.getElementById("welcome-text") as? HTMLSpanElement)?.apply {
      innerText =
        projection.language.string(
          "ui",
          if (authMode == AuthMode.Create) "prompt_login" else "prompt_create",
        )
    }

    (document.getElementById("auth-change") as? HTMLSpanElement)?.apply {
      innerHTML = ""
      projection.language
        .string("ui", if (authMode == AuthMode.Create) "prompt_create" else "prompt_login")
        .let {
          append {
            span { +"[" }
            b { +it[0].toString() }.apply { this.style.textDecoration = "underline" }
            span { +(it.substring(1) + "]") }
          }
        }
      updateKeyHandler(innerText[1].lowercase())
    }
    authMode = if (authMode == AuthMode.Create) AuthMode.Login else AuthMode.Create
  }

  fun tryAuth() {
    val user = (document.getElementById("username") as? HTMLInputElement)!!.value
    val pass = (document.getElementById("password") as? HTMLInputElement)!!.value
    try {
      window.asDynamic().projection = projection
      when (authMode) {
        AuthMode.Login -> {
          projection.login(user, pass)
          switchTo(View.GameView, projection)
        }
        AuthMode.Create -> {
          projection.createAccount(user, pass)
          switchTo(View.IntroView, projection)
        }
      }
    } catch (e: AccountManager.AccountError) {
      (document.getElementById("auth-error") as? HTMLSpanElement)!!.innerText = e.message
    }
  }
}
