package dev.wavelang.intellij.wave.ir

import com.intellij.openapi.fileTypes.LanguageFileType
import dev.wavelang.intellij.wave.WaveBundle
import dev.wavelang.intellij.wave.WaveIcons
import javax.swing.Icon

class WhaleIrFileType : LanguageFileType(WhaleIrLanguage) {
  override fun getName(): String = "Whale IR"

  override fun getDescription(): String = WaveBundle.message("filetype.whale.ir.description")

  override fun getDefaultExtension(): String = "wir"

  override fun getIcon(): Icon = WaveIcons.FILE
}
