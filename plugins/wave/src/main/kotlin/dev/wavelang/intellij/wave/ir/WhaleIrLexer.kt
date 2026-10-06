package dev.wavelang.intellij.wave.ir

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class WhaleIrLexer : LexerBase() {
  private var buffer: CharSequence = ""
  private var endOffset: Int = 0
  private var position: Int = 0
  private var tokenStart: Int = 0
  private var tokenEnd: Int = 0
  private var tokenType: IElementType? = null

  private val keywords = setOf(
    // Module header & metadata
    "module", "format_version", "semantics_version", "target", "datalayout",
    "endian", "little", "big", "global", "align", "id", "init_expr",
    "declare", "linkage", "internal", "external", "link_name",

    // Functions and calls
    "fn", "call", "indirect", "whale", "sysv64", "function_addr", "null_function",

    // Terminators
    "ret", "br", "cbr", "switch", "trap",

    // Instructions
    "alloca", "load", "store", "const", "const_decl", "undef", "mov", "not",
    "cmp", "icmp", "fcmp", "select", "phi", "extract", "gep", "memcpy", "memset",
    "trap_if", "reason",

    // Arithmetic & Bitwise
    "add", "sub", "mul", "udiv", "sdiv", "urem", "srem",
    "fadd", "fsub", "fmul", "fdiv", "frem",
    "and", "or", "xor", "shl", "lshr", "ashr",
    "uadd", "usub", "umul", "sadd", "ssub", "smul",
    "uadd_chk", "usub_chk", "umul_chk", "sadd_chk", "ssub_chk", "smul_chk",

    // Comparison predicates
    "eq", "ne", "slt", "sle", "sgt", "sge", "ult", "ule", "ugt", "uge",
    "oeq", "one", "olt", "ole", "ogt", "oge", "ord", "uno", "ueq", "une",
    "feq", "fne", "flt", "fle", "fgt", "fge",

    // Casts
    "to", "zext", "sext", "trunc", "fext", "ftrunc",
    "itof_s", "itof_u", "ftoi_s", "ftoi_u", "bitcast", "ptrtoint", "inttoptr",

    // Literals / Constants
    "true", "false", "null", "none"
  )

  private val types = setOf(
    "void", "bool", "ptr", "fnptr", "label", "array", "struct", "tuple",
    "f16", "f32", "f64"
  )

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    this.buffer = buffer
    this.position = startOffset
    this.endOffset = endOffset
    this.tokenStart = startOffset
    this.tokenEnd = startOffset
    this.tokenType = null
    advance()
  }

  override fun getState(): Int = 0
  override fun getTokenStart(): Int = tokenStart
  override fun getTokenEnd(): Int = tokenEnd
  override fun getTokenType(): IElementType? = tokenType
  override fun getBufferSequence(): CharSequence = buffer
  override fun getBufferEnd(): Int = endOffset

  override fun advance() {
    if (position >= endOffset) {
      tokenType = null
      return
    }

    tokenStart = position

    if (scanWhitespace()) return
    if (scanComment()) return
    if (scanString()) return
    if (scanValueId()) return
    if (scanLabel()) return
    if (scanNumber()) return
    if (scanWordLike()) return
    if (scanPunctuationOrOperator()) return

    tokenType = TokenType.BAD_CHARACTER
    tokenEnd = position + 1
    position = tokenEnd
  }

  private fun scanWhitespace(): Boolean {
    if (!buffer[position].isWhitespace()) return false
    var i = position + 1
    while (i < endOffset && buffer[i].isWhitespace()) i++
    emit(TokenType.WHITE_SPACE, i)
    return true
  }

  private fun scanComment(): Boolean {
    if (buffer[position] == ';') {
      var i = position + 1
      while (i < endOffset && buffer[i] != '\n') i++
      emit(WhaleIrTokens.COMMENT, i)
      return true
    }
    if (position + 1 < endOffset && buffer[position] == '/' && buffer[position + 1] == '/') {
      var i = position + 2
      while (i < endOffset && buffer[i] != '\n') i++
      emit(WhaleIrTokens.COMMENT, i)
      return true
    }
    return false
  }

  private fun scanString(): Boolean {
    val quote = buffer[position]
    if (quote != '"') return false

    var i = position + 1
    while (i < endOffset) {
      val ch = buffer[i]
      if (ch == '\\') {
        i += if (i + 1 < endOffset) 2 else 1
        continue
      }
      if (ch == quote) {
        i++
        break
      }
      if (ch == '\n') break
      i++
    }
    emit(WhaleIrTokens.STRING, i.coerceAtMost(endOffset))
    return true
  }

  private fun scanValueId(): Boolean {
    val ch = buffer[position]
    if (ch != '%' && ch != '@' && ch != '!') return false
    var i = position + 1
    if (i < endOffset && buffer[i] == '"') {
      i++
      while (i < endOffset) {
        if (buffer[i] == '"') {
          i++
          break
        }
        if (buffer[i] == '\\' && i + 1 < endOffset) i++
        i++
      }
    } else {
      while (i < endOffset && isIdentifierPart(buffer[i])) i++
    }
    if (i > position + 1) {
      emit(WhaleIrTokens.VALUE_ID, i)
      return true
    }
    return false
  }

  private fun scanLabel(): Boolean {
    var i = position
    if (buffer[i] == '^') i++
    if (i < endOffset && (buffer[i].isLetterOrDigit() || buffer[i] == '_' || buffer[i] == '.')) {
      var j = i + 1
      while (j < endOffset && isIdentifierPart(buffer[j])) j++
      if (j < endOffset && buffer[j] == ':') {
        if (j + 1 < endOffset && buffer[j + 1] == ':') return false
        emit(WhaleIrTokens.LABEL, j + 1)
        return true
      }
    }
    return false
  }

  private fun scanNumber(): Boolean {
    if (!buffer[position].isDigit() && buffer[position] != '-') return false
    var i = position
    if (buffer[i] == '-') {
      if (i + 1 >= endOffset || !buffer[i + 1].isDigit()) return false
      i++
    }

    if (buffer[i] == '0' && i + 1 < endOffset && (buffer[i + 1] == 'x' || buffer[i + 1] == 'X')) {
      i += 2
      while (i < endOffset && (buffer[i].isDigit() || buffer[i].lowercaseChar() in 'a'..'f')) i++
      emit(WhaleIrTokens.NUMBER, i)
      return true
    }

    while (i < endOffset && (buffer[i].isDigit() || buffer[i] == '.')) i++
    if (i < endOffset && (buffer[i] == 'e' || buffer[i] == 'E')) {
      i++
      if (i < endOffset && (buffer[i] == '+' || buffer[i] == '-')) i++
      while (i < endOffset && buffer[i].isDigit()) i++
    }
    emit(WhaleIrTokens.NUMBER, i)
    return true
  }

  private fun scanWordLike(): Boolean {
    val c = buffer[position]
    if (!(c.isLetter() || c == '_' || c == '.')) return false
    var i = position + 1
    while (i < endOffset && isIdentifierPart(buffer[i])) i++

    val text = buffer.subSequence(position, i).toString().lowercase()
    val type = when {
      keywords.contains(text) -> WhaleIrTokens.KEYWORD
      types.contains(text) || text.matches(INT_TYPE_REGEX) -> WhaleIrTokens.TYPE
      else -> WhaleIrTokens.IDENT
    }
    emit(type, i)
    return true
  }

  private fun scanPunctuationOrOperator(): Boolean {
    return when (buffer[position]) {
      ',' -> { emit(WhaleIrTokens.COMMA, position + 1); true }
      ':' -> { emit(WhaleIrTokens.COLON, position + 1); true }
      '(', ')', '[', ']', '{', '}', '<', '>' -> { emit(WhaleIrTokens.BRACKET, position + 1); true }
      '+', '-', '*', '/', '%', '=', '!', '&', '|', '^' -> { emit(WhaleIrTokens.OPERATOR, position + 1); true }
      else -> false
    }
  }

  private fun isIdentifierPart(ch: Char): Boolean {
    return ch.isLetterOrDigit() || ch == '_' || ch == '.' || ch == '$' || ch == '-'
  }

  private fun emit(type: IElementType, end: Int) {
    tokenType = type
    tokenEnd = end
    position = end
  }

  companion object {
    private val INT_TYPE_REGEX = Regex("[iu][0-9]+")
  }
}
