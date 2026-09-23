package dev.wavelang.intellij.wave.ld

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LinkerScriptLexerTest {
  private data class LexedToken(val type: IElementType, val text: String, val start: Int, val end: Int)

  private fun lexAll(source: String): List<LexedToken> {
    val lexer = LinkerScriptLexer()
    lexer.start(source, 0, source.length, 0)

    val out = mutableListOf<LexedToken>()
    while (true) {
      val type = lexer.tokenType ?: break
      val text = source.substring(lexer.tokenStart, lexer.tokenEnd)
      out += LexedToken(type = type,
        text = text,
        start = lexer.tokenStart,
        end = lexer.tokenEnd
      )
      lexer.advance()
    }
    return out
  }

  @Test
  fun gnuLdCoreConstructsAreHighlighted() {
    val source = """
      ENTRY(_start)
      MEMORY
      {
       ROM (rx) : ORIGIN = 0x00100000, LENGTH = 256K
      }
      SECTIONS
      {
       .text : { KEEP(*(.text .text.*)) } > ROM
       PROVIDE(end = .);
      }
      /* linker comment */
    """.trimIndent()

    val tokens = lexAll(source).filter { it.type != TokenType.WHITE_SPACE }

    assertTrue(tokens.any { it.type == LinkerScriptTokens.KEYWORD && it.text.uppercase() == "ENTRY" })
    assertTrue(tokens.any { it.type == LinkerScriptTokens.KEYWORD && it.text.uppercase() == "MEMORY" })
    assertTrue(tokens.any { it.type == LinkerScriptTokens.KEYWORD && it.text.uppercase() == "SECTIONS" })
    assertTrue(tokens.any { it.type == LinkerScriptTokens.KEYWORD && it.text.uppercase() == "KEEP" })
    assertTrue(tokens.any { it.type == LinkerScriptTokens.BUILTIN && it.text.uppercase() == "ORIGIN" })
    assertTrue(tokens.any { it.type == LinkerScriptTokens.BUILTIN && it.text.uppercase() == "LENGTH" })
    assertTrue(tokens.any { it.type == LinkerScriptTokens.SECTION && it.text == ".text" })
    assertTrue(tokens.any { it.type == LinkerScriptTokens.NUMBER && it.text == "0x00100000" })
    assertTrue(tokens.any { it.type == LinkerScriptTokens.NUMBER && it.text == "256K" })
    assertTrue(tokens.any { it.type == LinkerScriptTokens.COMMENT && it.text.startsWith("/*") })
  }

  @Test
  fun standaloneDotIsTokenizedWithCorrectTextAndOffsets() {
    val source = "."
    val tokens = lexAll(source)

    assertTrue(tokens.size == 1)
    assertTrue(tokens[0].type == LinkerScriptTokens.DOT)
    assertTrue(tokens[0].text == ".")
    assertTrue(tokens[0].start == 0)
    assertTrue(tokens[0].end == 1)
  }

  @Test
  fun standaloneDotBeforeWhitespaceAndPunctuationIsTokenized() {
    val source = ". ; .)"
    val tokens = lexAll(source).filter { it.type != TokenType.WHITE_SPACE }

    assertTrue(tokens[0].type == LinkerScriptTokens.DOT)
    assertTrue(tokens[0].text == ".")
    assertTrue(tokens[0].start == 0)
    assertTrue(tokens[0].end == 1)

    assertTrue(tokens[1].type == LinkerScriptTokens.SEMICOLON)
    assertTrue(tokens[1].text == ";")

    assertTrue(tokens[2].type == LinkerScriptTokens.DOT)
    assertTrue(tokens[2].text == ".")
    assertTrue(tokens[2].start == 4)
    assertTrue(tokens[2].end == 5)

    assertTrue(tokens[3].type == LinkerScriptTokens.PAREN)
    assertTrue(tokens[3].text == ")")
  }

  @Test
  fun sectionTokensRemainSections() {
    val source = ".text .text.*"
    val tokens = lexAll(source).filter { it.type != TokenType.WHITE_SPACE }

    assertTrue(tokens.size == 2)

    assertTrue(tokens[0].type == LinkerScriptTokens.SECTION)
    assertTrue(tokens[0].text == ".text")
    assertTrue(tokens[0].start == 0)
    assertTrue(tokens[0].end == 5)

    assertTrue(tokens[1].type == LinkerScriptTokens.SECTION)
    assertTrue(tokens[1].text == ".text.*")
    assertTrue(tokens[1].start == 6)
    assertTrue(tokens[1].end == 13)
  }

  @Test
  fun standaloneLocationCountersDoNotProduceBadCharacters() {
    val source = """
      SECTIONS {
        . = 0x1000;
        .text : { *(.text) }
        . = ALIGN(16);
        PROVIDE(end = .);
      }
    """.trimIndent()

    val tokens = lexAll(source)

    assertTrue(tokens.none { it.type == TokenType.BAD_CHARACTER })

    val dots = tokens.filter { it.type == LinkerScriptTokens.DOT }

    assertTrue(dots.size == 3)
    assertTrue(dots.all { it.text == "." })
  }
}
