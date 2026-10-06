package dev.wavelang.intellij.wave.ir

import com.intellij.psi.tree.IElementType
import dev.wavelang.intellij.wave.ir.WhaleIrLanguage

class WhaleIrTokenType(debugName: String) : IElementType(debugName, WhaleIrLanguage) {
  override fun toString(): String = "WhaleIrTokenType." + super.toString()
}

object WhaleIrTokens {
  @JvmField val COMMENT = WhaleIrTokenType("COMMENT")
  @JvmField val KEYWORD = WhaleIrTokenType("KEYWORD")
  @JvmField val VALUE_ID = WhaleIrTokenType("VALUE_ID")
  @JvmField val TYPE = WhaleIrTokenType("TYPE")
  @JvmField val LABEL = WhaleIrTokenType("LABEL")
  @JvmField val NUMBER = WhaleIrTokenType("NUMBER")
  @JvmField val STRING = WhaleIrTokenType("STRING")
  @JvmField val OPERATOR = WhaleIrTokenType("OPERATOR")
  @JvmField val COMMA = WhaleIrTokenType("COMMA")
  @JvmField val COLON = WhaleIrTokenType("COLON")
  @JvmField val BRACKET = WhaleIrTokenType("BRACKET")
  @JvmField val IDENT = WhaleIrTokenType("IDENT")
}
