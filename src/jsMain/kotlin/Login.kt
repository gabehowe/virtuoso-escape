import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import org.virtuoso.escape.model.GameProjection
import org.virtuoso.escape.model.account.Account
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement

object Login {
  val projection: GameProjection = window.asDynamic().projection

  fun setupListeners() {
    (document.getElementById("auth-change") as? HTMLSpanElement)?.onclick = {
      this.toggleAuthMode()
    }
    document.onsubmit = { ev ->
      ev.preventDefault()
      tryAuth()
    }
    (document.getElementById("enter-box") as? HTMLElement)?.onclick = document.onsubmit
  }

  fun run(projection: GameProjection?) =
    MainScope().promise {
      setupListeners()
      toggleAuthMode()
    }

  fun toggleAuthMode() {
    (document.getElementById("auth-prompt") as? HTMLSpanElement)
      ?.innerText = projection.language.string("ui", "switch_create")

    (document.getElementById("welcome-text") as? HTMLSpanElement)
      ?.innerText =
      projection.language.string(
        "ui", "prompt_create",
      )

  }

  fun tryAuth() {
    val user = (document.getElementById("username") as? HTMLInputElement)!!.value
    val pass = (document.getElementById("password") as? HTMLInputElement)?.value ?: "dummy_pwd"
    try {
      window.asDynamic().projection = projection
      projection.createAccount(user, pass)
      switchTo(View.IntroView, projection)
    } catch (e: Account.AccountError) {
      (document.getElementById("auth-error") as? HTMLSpanElement)?.innerText = e.message
    }
  }
}
