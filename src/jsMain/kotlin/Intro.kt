import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.b
import kotlinx.html.js.span
import org.virtuoso.escape.model.GameProjection
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement

object Intro {
  fun run(projection: GameProjection) {
    console.log(projection.state)
    val beaver = (document.getElementById("beaver") as? HTMLImageElement)!!
    val barn = (document.getElementById("barn") as? HTMLImageElement)!!
    beaver.apply {
      style.opacity = "0"
      style.visibility = "hidden"
      onclick = {
        (document.getElementById("overlay") as? HTMLDivElement)?.apply {
          style.visibility = "visible"
          style.opacity = "1"
          Game.setDialogue(Game.sanitizeForJS(projection.language.string("ui", "intro")), "overlay")
          this.lastElementChild!!.lastElementChild.asDynamic().onanimationend = {
            (document.getElementById("skip")!! as HTMLElement).apply {
              innerHTML = ""
              append {
                span {
                  +"["
                }
                b {
                  +"P"
                }.apply {
                  style.textDecoration = "underline"
                }
                span {
                  +"ress anywhere to continue]"
                }
              }
            }
            "Press anywhere to continue..."
            document.addEventListener("click", { switchTo(View.GameView, projection) })
          }
        }
      }
    }
    (document.getElementById("skip")!! as HTMLElement).apply {
      addEventListener("click", {
        switchTo(View.GameView, projection)
      })
    }
    barn.apply {
      onclick = {
        src = "images/opened-beaver-barn.png"
        beaver.style.opacity = "1"
        beaver.style.visibility = "visible"
        classList.remove("closed")
      }
    }
    document.onkeypress = kp@{
      if (it.key != "p") return@kp Unit
      switchTo(View.GameView, projection)
    }
  }
}
