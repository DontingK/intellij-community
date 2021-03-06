// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.actions

import com.intellij.application.options.editor.CheckboxDescriptor
import com.intellij.application.options.editor.checkBox
import com.intellij.codeInsight.actions.ReaderMode.*
import com.intellij.lang.LangBundle
import com.intellij.openapi.components.*
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.*
import com.intellij.util.PlatformUtils

class ReaderModeConfigurable(val project: Project) : BoundSearchableConfigurable(LangBundle.message("configurable.reader.mode"), "READER_MODE_HELP") {
  private val settings get() = ReaderModeSettings.instance(project)

  private val cdBreadcrumbs get() = CheckboxDescriptor(LangBundle.message("checkbox.breadcrumbs"), settings::showBreadcrumbs)
  private val cdInlays get() = CheckboxDescriptor(LangBundle.message("checkbox.inlays"), settings::showInlaysHints)
  private val cdRenderedDocs get() = CheckboxDescriptor(LangBundle.message("checkbox.rendered.docs"), settings::showRenderedDocs)
  private val cdLigatures get() = CheckboxDescriptor(LangBundle.message("checkbox.ligatures"), settings::showLigatures)
  private val cdLineSpacing get() = CheckboxDescriptor(LangBundle.message("checkbox.line.spacing"), settings::increaseLineSpacing)
  private val cdWarnings get() = CheckboxDescriptor(LangBundle.message("checkbox.hide.warnings"), settings::showWarnings)
  private val cdEnabled get() = CheckboxDescriptor(LangBundle.message("checkbox.reader.mode.toggle"), settings::enabled)

  override fun createPanel(): DialogPanel {
    return panel {
      lateinit var enabled: CellBuilder<JBCheckBox>
      row {
        enabled = checkBox(cdEnabled).comment("")
          .comment(LangBundle.message("checkbox.reader.mode.toggle.comment"))

        row(LangBundle.message("titled.border.reader.mode.settings")) {
          row {
            checkBox(cdRenderedDocs).enableIf(enabled.selected)
          }
          row {
            checkBox(cdBreadcrumbs).enableIf(enabled.selected)
          }
          row {
            checkBox(cdWarnings).enableIf(enabled.selected)
          }
          row {
            checkBox(cdLigatures).enableIf(enabled.selected)
          }
          row {
            checkBox(cdLineSpacing).enableIf(enabled.selected)
          }
          row {
            checkBox(cdInlays).enableIf(enabled.selected).visible(PlatformUtils.isIdeaCommunity() || PlatformUtils.isIdeaEducational() || PlatformUtils.isIdeaUltimate())
          }
        }.enableIf(enabled.selected)
      }
    }
  }

  override fun apply() {
    super.apply()
    project.messageBus.syncPublisher(READER_MODE_TOPIC).modeChanged(project)
  }
}

enum class ReaderMode {
  LIBRARIES, READ_ONLY, LIBRARIES_AND_READ_ONLY
}

@State(name = "ReaderModeSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ReaderModeSettings : PersistentStateComponentWithModificationTracker<ReaderModeSettings.State> {
  companion object {
    @JvmStatic
    fun instance(project: Project): ReaderModeSettings {
      return ServiceManager.getService(project, ReaderModeSettings::class.java)
    }
  }

  private var myState = State()

  class State : BaseState() {
    var showBreadcrumbs: Boolean = true
    var showLigatures: Boolean = true
    var increaseLineSpacing: Boolean = false
    var showRenderedDocs: Boolean = true
    var showInlayHints: Boolean = true
    var showWarnings: Boolean = false
    @get:ReportValue
    var enabled by property(true)
    var mode: ReaderMode = LIBRARIES_AND_READ_ONLY
  }

  var showBreadcrumbs: Boolean
    get() = state.showBreadcrumbs
    set(value) {
      state.showBreadcrumbs = value
    }

  var showLigatures: Boolean
    get() = state.showLigatures
    set(value) {
      state.showLigatures = value
    }

  var increaseLineSpacing: Boolean
    get() = state.increaseLineSpacing
    set(value) {
      state.increaseLineSpacing = value
    }

  var showInlaysHints: Boolean
    get() = state.showInlayHints
    set(value) {
      state.showInlayHints = value
    }

  var showRenderedDocs: Boolean
    get() = state.showRenderedDocs
    set(value) {
      state.showRenderedDocs = value
    }

  var showWarnings: Boolean
    get() = state.showWarnings
    set(value) {
      state.showWarnings = value
    }

  var enabled: Boolean
    get() = state.enabled
    set(value) {
      state.enabled = value
    }

  var mode: ReaderMode
    get() = state.mode
    set(value) {
      state.mode = value
    }

  override fun getState(): State = myState
  override fun loadState(state: State) {
    myState = state
  }

  override fun getStateModificationCount() = state.modificationCount
}