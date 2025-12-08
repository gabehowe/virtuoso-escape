import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.img
import kotlinx.html.js.span
import kotlinx.html.js.td
import org.virtuoso.escape.model.Floor
import org.virtuoso.escape.model.GameProjection
import org.virtuoso.escape.model.account.Leaderboard
import org.virtuoso.escape.model.toMicrowaveTime
import org.w3c.dom.*
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.ExperimentalTime

object Credits {
    lateinit var projection: GameProjection
    var framecount: Int = 0
    fun run(projection: GameProjection) {
        this.projection = projection
        (document.getElementById("logout") as? HTMLSpanElement)!!.apply {
            onclick = { switchTo(View.LoginView, null) }
            document.onkeypress = { if (it.key == "l") click() }
        }
        (document.getElementById("entity-flow") as? HTMLDivElement)!!.also { flow ->
            Floor.entries.flatMap { it.rooms }.flatMap { it.entities }.map { it.id }.forEach {
                flow.append.div {
                    img {
                        src = "images/${it}.png"
                    }

                }.also { sp -> (sp.firstElementChild as HTMLImageElement).draggable = false }
            }
        }
        shuffleNames()
        populateRunInfo()
        populateLeaderboard()
        window.requestAnimationFrame { imageAnimation() }
    }

    fun boustrophedon(t: Double): Pair<Double, Double> {
        val width = window.innerWidth / 15.0;
        val height = window.innerHeight.toDouble() / 2.3
        val (x_v, y_v) = 100.0 to 100.0
        val time = t / 50.0 / (height / y_v)
        // nonsensical magic formula
        val y = (time % 8.0).let {
            return@let when (it) {
                in 0.0..<2.0 -> width * -sqrt(1 - (it - 1).pow(2))
                in 2.0..<4.0 -> height * (it - 2)
                in 4.0..<6.0 -> 2 * height + width * sqrt(1 - (it - 5).pow(2))
                in 6.0..<8.0 -> height * (8 - it)
                else -> throw Exception("Bad math! t (mod 8) exceeded 8! $it")
            }
        }
        val x = width * (2.0 * floor(time / 4.0) + min(time % 4.0, 2.0))
        return x to y
    }

    @OptIn(ExperimentalTime::class)
    fun imageAnimation() {
        framecount++
        (document.getElementById("entity-flow") as? HTMLDivElement)!!.children.asList().forEachIndexed { index, i ->
            val (x, y) = boustrophedon((framecount.toDouble() + index * 60) % (60 * 60))
            (i as HTMLElement).style.transform = "translate(${x + 12}px, ${y + 12}px)"
        }
        window.requestAnimationFrame { imageAnimation() }
    }

    fun populateLeaderboard() {
        (document.getElementById("leaderboard") as HTMLElement).append(
            document.createElement("tr").apply {
                Leaderboard.getLeaderboard(projection.accountManager, projection.state, projection.account).chunked(4)
                    .forEach { row ->
                        row.forEach {
                            append {
                                td {
                                    +it
                                }
                            }
                        }
                    }

            })
    }

    fun populateRunInfo() {
        with(projection) {
            Leaderboard.recordSession(state, account)
            account.updateHighScore(state)
            document.getElementById("time_remaining")!!.textContent = account.highScore.timeRemaining.toMicrowaveTime()
            document.getElementById("final_score")!!.textContent = account.highScore.totalScore.toString()
            document.getElementById("hints_used")!!.textContent = state.hintsUsed.values.sum().toString()
            document.getElementById("difficulty")!!.textContent = state.difficulty.name
        }
    }

    fun shuffleNames() {
        val names = document.getElementById("name-list")!!
        val emails = document.getElementById("email-list")!!
        names.children.asList().zip(emails.children.asList()).shuffled().also {
            names.innerHTML = ""
            emails.innerHTML = ""
        }.forEach {
            names.append(it.first)
            emails.append(it.second)
        }

    }


}