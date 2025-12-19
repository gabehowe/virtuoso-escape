package org.virtuoso.escape.terminal

/**
 * Holder for decorated strings for terminal output.
 *
 * @param content The child nodes to output, FunString or char
 * @param styleCodes The console style codes to prefix to the string.
 * @param resetCodes The console style reset codes to suffix to the string.
 * @author gabri
 */
data class FunString(
    val content: MutableList<Any>,
    val styleCodes: MutableList<String>,
    val resetCodes: MutableList<String>,
) {

  /**
   * Create an [FunString] from a [String].
   *
   * @param content the string to create this with.
   */
  constructor(content: String) : this(stringChars(content), mutableListOf(), mutableListOf())

  /**
   * Clone another [FunString].
   *
   * @param funString the [FunString] to clone.
   */
  constructor(
      funString: FunString
  ) : this(funString.content.toMutableList(), funString.styleCodes, funString.resetCodes)

  /**
   * Create a [FunString] from a [List] of [FunString] and char.
   *
   * @param strs a [List] of [FunString] and `char`.
   */
  constructor(strs: MutableList<Any>) : this(strs, mutableListOf(), mutableListOf())

  operator fun plus(other: String): FunString {
    return FunString(mutableListOf(this, other))
  }

  operator fun plus(other: FunString): FunString {
    return FunString(mutableListOf(this, other))
  }

  /**
   * Convert this to a string with escape codes.
   *
   * @return A string with escape codes.
   */
  override fun toString(): String {
    val result = StringBuilder()
    val substring = this.content.joinToString("") { it.toString() }
    result.append(substring) // will automatically call toString on everything
    result.insert(0, styleCodes.joinToString(""))
    result.append(resetCodes.joinToString(""))
    return result.toString()
  }

  /**
   * Returns the length of the string without escape codes.
   *
   * @return the visual length.
   */
  fun length(): Int {
    return this.rawText().length
  }

  /**
   * The text content without escape codes.
   *
   * @return A string with no escape codes.
   */
  fun rawText(): String {
    val result = StringBuilder()
    for (i in this.content) {
      // JEP 394
      if (i is FunString) {
        result.append(i.rawText())
      } else {
        result.append(i.toString())
      }
    }
    return result.toString()
  }

  /**
   * Add another [FunString] to the end of this.
   *
   * @param fs the object to append.
   */
  fun add(fs: FunString?): FunString {
    this.content.add(fs!!)
    return this
  }

  /**
   * Add another [Character] to this.
   *
   * @param s The object to append.
   */
  fun add(s: Char?): FunString {
    this.content.add(s!!)
    return this
  }

  /**
   * Add another [String] to this.
   *
   * @param s The object to append.
   */
  fun add(s: String): FunString {
    this.content.addAll(stringChars(s))
    return this
  }

  /**
   * Replaces part of this object with `toReplace`
   *
   * @param start The index to start replacing at.
   * @param end The index to stop replacing before.
   * @param toReplace The [FunString] to replace the substring with. @apiNote The behavior when
   *   [FunString]s collide is unknown.
   */
  // doesn't support FunString collisions
  fun replaceSubstring(start: Int, end: Int, toReplace: FunString?) {
    for (i in end - 1 downTo start) {
      this.content.removeAt(i)
    }
    this.content.add(start, toReplace!!)
  }

  /**
   * Add an underline decoration to this.
   *
   * @return This with an underline decoration.
   */
  fun underline(): FunString {
    this.styleCodes.add(UNDERLINE)
    this.resetCodes.add(UNDERLINE_OFF)
    return this
  }

  /**
   * Add an italic decoration to this.
   *
   * @return This with an italic decoration.
   */
  fun italic(): FunString {
    this.styleCodes.add(ITALIC)
    this.resetCodes.add(ITALIC_OFF)
    return this
  }

  /**
   * Add a bold decoration to this.
   *
   * @return This with a bold decoration.
   */
  fun bold(): FunString {
    this.styleCodes.add(BOLD)
    this.resetCodes.add(BOLD_OFF)
    return this
  }

  /**
   * Make this red.
   *
   * @return This, but red.
   */
  fun red(): FunString {
    this.styleCodes.add(RED_FG)
    this.resetCodes.add(DEFAULT_FG)
    return this
  }

  /**
   * Make this blue.
   *
   * @return This, but blue.
   */
  fun blue(): FunString {
    this.styleCodes.add(BLUE_FG)
    this.resetCodes.add(DEFAULT_FG)
    return this
  }

  /**
   * Make this green.
   *
   * @return This, but green.
   */
  fun green(): FunString {
    this.styleCodes.add(GREEN_FG)
    this.resetCodes.add(DEFAULT_FG)
    return this
  }

  /**
   * Make this purple.
   *
   * @return This, but purple.
   */
  fun purple(): FunString {
    this.styleCodes.add(PURPLE_FG)
    this.resetCodes.add(DEFAULT_FG)
    return this
  }

  /**
   * Color this with an 8-bit terminal color.
   *
   * @param color A color index from 0-256.
   * @return This, but colored.
   */
  fun terminalColor(color: Int): FunString {
    assert(color in 1..<256)
    this.styleCodes.add(escape("38;5;" + color + "m"))
    this.resetCodes.add(DEFAULT_FG)
    return this
  }

  companion object {
    private val RED_FG: String = escape("31m")
    private val BLUE_FG: String = escape("38;5;44m")
    private val GREEN_FG: String = escape("38;5;76m")
    private val PURPLE_FG: String = escape("38;5;201m")
    private val DEFAULT_FG: String = escape("39m")
    private val BOLD: String = escape("1m") // for controls
    private val BOLD_OFF: String = escape("22m")
    private val UNDERLINE: String = escape("4m") // for controls
    private val UNDERLINE_OFF: String = escape("24m")
    private val RESET: String = escape("0m")
    private val ITALIC: String = escape("3m") // for entities
    private val ITALIC_OFF: String = escape("23m")

    /**
     * Create a terminal escape code.
     *
     * @param innerCode the inner part of the code.
     * @return An escaped ANSI terminal code.
     */
    fun escape(innerCode: String?): String {
      return String.format("\u001b[%s", innerCode)
    }

    /**
     * Join each string by a delimiter. Similar to [joinToString]
     *
     * @param delimiter The string to delimit by.
     * @param funStrings The FunStrings to join
     * @return the joined object.
     */
    fun join(delimiter: String, funStrings: Iterable<FunString>): FunString {
      val iterator: MutableIterator<FunString> = funStrings.iterator() as MutableIterator<FunString>
      assert(iterator.hasNext())
      val accumulator = FunString(mutableListOf(iterator.next()))
      iterator.forEachRemaining { i: FunString? ->
        accumulator.add(delimiter)
        accumulator.add(i)
      }
      return accumulator
    }

    /**
     * Convert a string into a list of characters.
     *
     * @param s the string to convert.
     * @return a list of characters.
     */
    private fun stringChars(s: String): MutableList<Any> {
      return s.asSequence().map { it.toChar() }.toMutableList()
    }
  }
}
