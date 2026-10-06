package dev.wavelang.intellij.wave.ir

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.TokenType

/**
 * Whale IR Syntax Highlighter.
 * 
 * Note: Syntax coloring provides bounded lexical highlighting only. 
 * It does not validate IR, build a CFG, resolve references, or make the IR fully parseable. 
 * `.wir` is not guaranteed to be a stable editable or round-trip format.
 */
class WhaleIrSyntaxHighlighter : SyntaxHighlighterBase() {
  override fun getHighlightingLexer(): Lexer = WhaleIrLexer()

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
    WhaleIrTokens.COMMENT -> pack(COMMENT)
    WhaleIrTokens.KEYWORD -> pack(KEYWORD)
    WhaleIrTokens.VALUE_ID -> pack(VALUE_ID)
    WhaleIrTokens.TYPE -> pack(TYPE)
    WhaleIrTokens.LABEL -> pack(LABEL)
    WhaleIrTokens.NUMBER -> pack(NUMBER)
    WhaleIrTokens.STRING -> pack(STRING)
    WhaleIrTokens.OPERATOR -> pack(OPERATOR)
    WhaleIrTokens.COMMA -> pack(COMMA)
    WhaleIrTokens.COLON -> pack(COLON)
    WhaleIrTokens.BRACKET -> pack(BRACKET)
    TokenType.BAD_CHARACTER -> pack(BAD_CHAR)
    else -> emptyArray()
  }

  companion object {
    val COMMENT: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val KEYWORD: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val VALUE_ID: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_VALUE_ID", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
    val TYPE: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_TYPE", DefaultLanguageHighlighterColors.CLASS_REFERENCE)
    val LABEL: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_LABEL", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    val NUMBER: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    val STRING: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_STRING", DefaultLanguageHighlighterColors.STRING)
    val OPERATOR: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val COMMA: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_COMMA", DefaultLanguageHighlighterColors.COMMA)
    val COLON: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_COLON", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val BRACKET: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_BRACKET", DefaultLanguageHighlighterColors.BRACKETS)
    val BAD_CHAR: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey("WIR_BAD_CHAR", HighlighterColors.BAD_CHARACTER)
  }
}
