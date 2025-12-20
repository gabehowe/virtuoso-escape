import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.img
import kotlinx.html.js.canvas
import kotlinx.html.js.td
import org.virtuoso.escape.model.Floor
import org.virtuoso.escape.model.GameProjection
import org.virtuoso.escape.model.account.Leaderboard
import org.virtuoso.escape.model.toMicrowaveTime
import org.w3c.dom.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.uuid.ExperimentalUuidApi

operator fun Pair<Double, Double>.plus(other: Pair<Double, Double>): Pair<Double, Double> =
  this.x() + other.x() to this.y() + other.y()

operator fun Pair<Double, Double>.div(other: Double): Pair<Double, Double> =
  this.x() / other to this.y() / other

fun Pair<Double, Double>.x(): Double = this.first
fun Pair<Double, Double>.y(): Double = this.second

object Credits {
  var A: Double = 2.0
  var B: Double = 1.0
  var C: Double = 3.0
  lateinit var projection: GameProjection
  var framecount: Int = 0

  fun run(projection: GameProjection) {
    this.projection = projection
    (document.getElementById("logout") as? HTMLSpanElement)!!.apply {
      onclick = { switchTo(View.LoginView, null) }
      document.onkeypress = { if (it.key == "l") click() }
    }
    (document.getElementById("entity-flow") as? HTMLDivElement)!!.also { flow ->
      Floor.entries
        .flatMap { it.rooms }
        .flatMap { it.entities }
        .map { it.id }
        .forEach {
          flow.append
            .div { img { src = "images/${it}.png" } }
            .also { sp -> (sp.firstElementChild as HTMLImageElement).draggable = false }
        }
    }
    window.asDynamic().A = A
    window.asDynamic().B = B
    window.asDynamic().C = C
    shuffleNames()
    populateRunInfo()
    populateLeaderboard()
    animate()
  }
  fun boustrophedon_position(t: Double): Pair<Double, Double> {
    val totalHeight = window.innerHeight - 100
    val num_rows = 10.0
    val width = window.innerWidth / num_rows * 1.1
    val r = width/2.0// radius
    val velocity = 3.0
    val l = totalHeight - r*2.0 // length
    val a = 2.0 * PI * r + 2.0 * l // arclength
    val distance = (t * velocity).mod(a * num_rows / 2.0)
    val m = distance.mod(a)
    val q = floor(distance / a) * 4.0 * r
    return when (m) {
      in 0.0..l -> q to m + r
      in l..a/2.0 -> {
        val parameter = (m - l) / r
        q + r * (1 - cos(parameter)) to l + r * (1 + sin(parameter))
      }
      in (a/2.0)..(a/2.0 + l) -> {
        q + 2.0 * r to r+l-(m-a/2.0)
      }

      in (a / 2.0 + l)..a -> {
        val parameter = (m - 2.0 * l - PI * r) / r
        val v = q + r * (3 - cos(parameter)) to r * (1 - sin(parameter))
        v
      }

      else -> throw Error("Bad math")
    }

  }

  fun animate() {
    val objects = (document.getElementById("entity-flow") as? HTMLDivElement)!!.children.asList()

    fun imageAnimation() {
      framecount++
      objects.forEachIndexed { index, i ->
        val pos = boustrophedon_position(framecount + index * 60.0)
        (i as HTMLElement).style.transform = "translate(${pos.x() + 12}px, ${pos.y() + 12}px)"
      }
      window.requestAnimationFrame { imageAnimation() }
    }
    imageAnimation()
  }

  @OptIn(ExperimentalUuidApi::class)
  fun populateLeaderboard() {
    (document.getElementById("leaderboard") as HTMLElement).append(
      document.createElement("tr").apply {
        Leaderboard.getLeaderboard(
          projection.accounts,
          projection.account,
        )
          .chunked(4)
          .forEach { row -> row.forEach { append { td { +it } } } }
      }
    )
  }

  fun populateRunInfo() {
    with(projection) {
      Leaderboard.recordSession(state, account)
      account.updateHighScore(state)
      document.getElementById("time_remaining")!!.textContent =
        account.highScore.timeRemaining.toMicrowaveTime()
      document.getElementById("final_score")!!.textContent = account.highScore.totalScore.toString()
      document.getElementById("hints_used")!!.textContent = state.hintsUsed.values.sum().toString()
      document.getElementById("difficulty")!!.textContent = state.difficulty.name
    }
  }

  fun shuffleNames() {
    val names = document.getElementById("name-list")!!
    val emails = document.getElementById("email-list")!!
    names.children
      .asList()
      .zip(emails.children.asList())
      .shuffled()
      .also {
        names.innerHTML = ""
        emails.innerHTML = ""
      }
      .forEach {
        names.append(it.first)
        emails.append(it.second)
      }
  }
}
