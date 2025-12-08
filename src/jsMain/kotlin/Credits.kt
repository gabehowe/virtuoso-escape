import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.span
import kotlinx.html.js.td
import org.virtuoso.escape.model.GameProjection
import org.virtuoso.escape.model.account.Leaderboard
import org.virtuoso.escape.model.toMicrowaveTime
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.asList

object Credits {
    lateinit var projection: GameProjection
    fun run(projection: GameProjection) {
        this.projection = projection
        (document.getElementById("logout") as? HTMLSpanElement)!!.apply {
            onclick = {switchTo(View.LoginView, null)}
            document.onkeypress = { if (it.key == "l") click() }
        }
        shuffleNames()
        populateRunInfo()
        populateLeaderboard()
    }

    fun populateLeaderboard() {
        (document.getElementById("leaderboard") as HTMLElement).append(
            document.createElement("tr").apply {
                Leaderboard.getLeaderboard(projection.accountManager, projection.state, projection.account).chunked(4).forEach { row->
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