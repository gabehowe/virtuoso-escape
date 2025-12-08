import kotlinx.browser.document
import kotlinx.browser.window
import org.virtuoso.escape.model.GameProjection
import org.w3c.dom.HTMLDivElement
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
                        document.getElementById("skip")!!.innerHTML = "" +
                                "Press Q to continue..."
                    }
                }
            }
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
            if (it.key != "q") return@kp Unit
            switchTo(View.GameView, projection)
        }
    }
}