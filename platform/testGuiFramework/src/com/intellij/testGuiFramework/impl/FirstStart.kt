/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.testGuiFramework.impl

import com.intellij.ide.PrivacyPolicy
import com.intellij.openapi.diagnostic.Logger
import com.intellij.testGuiFramework.fixtures.JDialogFixture
import com.intellij.testGuiFramework.impl.FirstStart.Utils.button
import com.intellij.testGuiFramework.impl.FirstStart.Utils.dialog
import com.intellij.testGuiFramework.impl.FirstStart.Utils.radioButton
import com.intellij.testGuiFramework.launcher.ide.IdeType
import org.fest.swing.core.GenericTypeMatcher
import org.fest.swing.core.Robot
import org.fest.swing.core.SmartWaitRobot
import org.fest.swing.exception.WaitTimedOutError
import org.fest.swing.fixture.JButtonFixture
import org.fest.swing.fixture.JRadioButtonFixture
import org.fest.swing.timing.Condition
import org.fest.swing.timing.Pause
import org.fest.swing.timing.Timeout
import java.awt.Component
import java.awt.Container
import java.awt.Frame
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JRadioButton
import kotlin.concurrent.thread

/**
 * could be used only to initialize IDEA configuration for the first start. Please do not start it with GuiTestCase runner.
 */
abstract class FirstStart(val ideType: IdeType) {

  private val FIRST_START_ROBOT_THREAD = "First Start Robot Thread"

  protected val LOG = Logger.getInstance(this.javaClass.name)

  val myRobot: Robot

  val robotThread: Thread = thread(start = false, name = FIRST_START_ROBOT_THREAD) {
    completeFirstStart()
  }

  init {
    myRobot = SmartWaitRobot()
    LOG.info("Starting separated thread: '$FIRST_START_ROBOT_THREAD' to complete initial installation")
    robotThread.start()
  }

  companion object {
    fun guessIdeAndStartRobot() {
      val firstStartClass = System.getProperty("idea.gui.test.first.start.class")
      val firstStart = Class.forName(firstStartClass).newInstance() as FirstStart
      firstStart.completeInstallation()
    }
  }

  protected abstract fun completeFirstStart()

  private fun awtIsNotStarted()
    = !(Thread.getAllStackTraces().keys.any { thread -> thread.name.toLowerCase().contains("awt") })

  private val checkIsFrameFunction: (Frame) -> Boolean
    get() {
      val checkIsFrame: (Frame) -> Boolean = { frame -> frame.javaClass.simpleName == "FlatWelcomeFrame" }
      return checkIsFrame
    }

  protected fun waitWelcomeFrameAndClose() {
    waitWelcomeFrame()
    LOG.info("Closing Welcome Frame")
    val welcomeFrame = Frame.getFrames().find(checkIsFrameFunction)
    myRobot.close(welcomeFrame!!)
  }

  protected fun waitWelcomeFrame() {
    LOG.info("Waiting for a Welcome Frame")
    Pause.pause(object : Condition("Welcome Frame to show up") {
      override fun test() = Frame.getFrames().any { checkIsFrameFunction(it) }
    }, Timeout.timeout(180, TimeUnit.SECONDS))
  }

  protected fun acceptAgreement() {
    with(myRobot) {
      val policyAgreementTitle = "Privacy Policy Agreement"
      val policy = PrivacyPolicy.getContent()
      val showPrivacyPolicyAgreement = !PrivacyPolicy.isVersionAccepted(policy.getFirst())
      if (!showPrivacyPolicyAgreement) return
      try {
        LOG.info("Waiting for '$policyAgreementTitle' dialog")
        with(JDialogFixture.findByPartOfTitle(myRobot, policyAgreementTitle, Timeout.timeout(2, TimeUnit.MINUTES))) {
          LOG.info("Accept '$policyAgreementTitle' dialog")
          button("Accept", 120)
        }
      }
      catch (e: WaitTimedOutError) {
        LOG.error("'$policyAgreementTitle' dialog hasn't been shown. Check registry...")
      }
    }
  }

  protected fun completeInstallation() {
    with(myRobot) {
      val title = "Complete Installation"
      LOG.info("Waiting for '$title' dialog")
      dialog(title, 120)
      LOG.info("Click OK on 'Do not import settings'")
      radioButton("Do not import settings", 120)
      button("OK", 120)
    }
  }

  protected fun customizeIntellijIdea() {
    with(myRobot) {
      val title = "Customize IntelliJ IDEA"
      LOG.info("Waiting for '$title' dialog")
      dialog(title, 120)
      val buttonText = "Skip All and Set Defaults"
      LOG.info("Click '$buttonText'")
      button(buttonText, 120)
    }
  }

  object Utils {

    fun Robot.dialog(title: String? = null, timeoutSeconds: Long) {
      waitUntilFound(this, null, JDialog::class.java, timeoutSeconds) { jDialog ->
        if (title != null) jDialog.title == title else true
      }
    }

    fun Robot.radioButton(text: String, timeoutSeconds: Long) {
      val rb = waitUntilFound(this, null, JRadioButton::class.java, timeoutSeconds) { radioButton ->
        radioButton.text == text && radioButton.isShowing && radioButton.isEnabled
      }
      val jRadioButtonFixture = JRadioButtonFixture(this, rb)
      jRadioButtonFixture.select()
    }

    fun Robot.button(text: String, timeoutSeconds: Long) {
      val jButton = waitUntilFound(this, null, JButton::class.java, timeoutSeconds) { jButton -> jButton.isShowing && jButton.text == text }
      JButtonFixture(this, jButton).click()
    }

    fun <ComponentType : Component> waitUntilFound(myRobot: Robot,
                                                           container: Container?,
                                                           componentClass: Class<ComponentType>,
                                                           timeoutSeconds: Long,
                                                           matcher: (ComponentType) -> Boolean): ComponentType {
      return waitUntilFound(myRobot, container, object : GenericTypeMatcher<ComponentType>(componentClass) {
        override fun isMatching(cmp: ComponentType): Boolean = matcher(cmp)
      }, Timeout.timeout(timeoutSeconds, TimeUnit.SECONDS))
    }

    fun <T : Component> waitUntilFound(robot: Robot,
                                       root: Container?,
                                       matcher: GenericTypeMatcher<T>,
                                       timeout: Timeout): T {
      val reference = AtomicReference<T>()
      Pause.pause(object : Condition("Find component using " + matcher.toString()) {
        override fun test(): Boolean {
          val finder = robot.finder()
          val allFound = if (root != null) finder.findAll(root, matcher) else finder.findAll(matcher)
          if (allFound.size == 1) {
            reference.set(allFound.first())
          }
          else if (allFound.size > 1) {
            // Only allow a single component to be found, otherwise you can get some really confusing
            // test failures; the matcher should pick a specific enough instance
            throw Exception("Found more than one " + matcher.supportedType().simpleName + " which matches the criteria: " + allFound)
          }
          return allFound.size == 1
        }
      }, timeout)

      return reference.get()
    }
  }

}
