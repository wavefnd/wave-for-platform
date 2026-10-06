package dev.wavelang.intellij.wave.ir

import com.intellij.psi.TokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WhaleIrLexerTest {

  @Test
  fun testKeywords() {
    val lexer = WhaleIrLexer()
    val words = listOf(
      "module", "format_version", "semantics_version", "target", "datalayout",
      "fn", "call", "ret", "br", "cbr", "switch", "trap",
      "add", "sub", "mul", "udiv", "sdiv", "urem", "srem",
      "const", "const_decl", "undef", "mov", "not",
      "icmp", "fcmp", "select", "phi", "extract", "gep",
      "memcpy", "memset", "trap_if", "function_addr", "null_function"
    )
    lexer.start(words.joinToString(" "))

    for (word in words) {
      assertEquals(WhaleIrTokens.KEYWORD, lexer.tokenType, "Expected keyword for '$word'")
      assertEquals(word, lexer.bufferSequence.substring(lexer.tokenStart, lexer.tokenEnd))
      lexer.advance()
      if (lexer.tokenType == TokenType.WHITE_SPACE) {
        lexer.advance()
      }
    }
    assertEquals(null, lexer.tokenType)
  }

  @Test
  fun testLlvmSpecificTokensAreIdentifiers() {
    val lexer = WhaleIrLexer()
    // These should NOT be parsed as keywords in Whale IR
    val nonKeywords = listOf(
      "func", "cond_br", "triple", "define", "invoke", "resume", "unreachable",
      "div", "rem", "poison", "landingpad", "catchpad", "cleanuppad", "va_arg"
    )
    lexer.start(nonKeywords.joinToString(" "))

    for (ident in nonKeywords) {
      assertEquals(WhaleIrTokens.IDENT, lexer.tokenType, "Expected ident for '$ident'")
      assertEquals(ident, lexer.bufferSequence.substring(lexer.tokenStart, lexer.tokenEnd))
      lexer.advance()
      if (lexer.tokenType == TokenType.WHITE_SPACE) {
        lexer.advance()
      }
    }
    assertEquals(null, lexer.tokenType)
  }

  @Test
  fun testTypes() {
    val lexer = WhaleIrLexer()
    val typeNames = listOf(
      "void", "bool", "ptr", "fnptr", "label",
      "i1", "i32", "i64", "i128",
      "u8", "u16", "u32", "u64", "u128",
      "f16", "f32", "f64",
      "array", "struct", "tuple"
    )
    lexer.start(typeNames.joinToString(" "))

    for (typeName in typeNames) {
      assertEquals(WhaleIrTokens.TYPE, lexer.tokenType, "Expected type for '$typeName'")
      assertEquals(typeName, lexer.bufferSequence.substring(lexer.tokenStart, lexer.tokenEnd))
      lexer.advance()
      if (lexer.tokenType == TokenType.WHITE_SPACE) {
        lexer.advance()
      }
    }
    assertEquals(null, lexer.tokenType)
  }

  @Test
  fun testValueIdsAndLabels() {
    val lexer = WhaleIrLexer()
    lexer.start("%0 @main entry: 0: 1:")

    assertEquals(WhaleIrTokens.VALUE_ID, lexer.tokenType)
    assertEquals("%0", lexer.bufferSequence.substring(lexer.tokenStart, lexer.tokenEnd))
    lexer.advance()

    lexer.advance() // space

    assertEquals(WhaleIrTokens.VALUE_ID, lexer.tokenType)
    assertEquals("@main", lexer.bufferSequence.substring(lexer.tokenStart, lexer.tokenEnd))
    lexer.advance()

    lexer.advance() // space

    assertEquals(WhaleIrTokens.LABEL, lexer.tokenType)
    assertEquals("entry:", lexer.bufferSequence.substring(lexer.tokenStart, lexer.tokenEnd))
    lexer.advance()

    lexer.advance() // space

    assertEquals(WhaleIrTokens.LABEL, lexer.tokenType)
    assertEquals("0:", lexer.bufferSequence.substring(lexer.tokenStart, lexer.tokenEnd))
    lexer.advance()

    lexer.advance() // space

    assertEquals(WhaleIrTokens.LABEL, lexer.tokenType)
    assertEquals("1:", lexer.bufferSequence.substring(lexer.tokenStart, lexer.tokenEnd))
    lexer.advance()

    assertEquals(null, lexer.tokenType)
  }
}
