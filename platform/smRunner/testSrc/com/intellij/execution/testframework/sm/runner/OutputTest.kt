// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.execution.testframework.sm.runner

import com.intellij.execution.testframework.sm.runner.ui.MockPrinter
import com.intellij.openapi.util.Disposer

class OutputTest : BaseSMTRunnerTestCase() {
  fun testBeforeAfterOrder() {
    val suite = createTestProxy("parent")
    suite.setTreeBuildBeforeStart()
    val child = createTestProxy("child", suite)
    child.setTreeBuildBeforeStart()

    suite.addStdOutput("before test started\n")
    child.setStarted()
    child.addStdOutput("inside test\n")
    child.setFinished()
    suite.addStdOutput("after test finished\n")

    val printer = MockPrinter(true)
    suite.printOn(printer)

    assertEquals("before test started\ninside test\nafter test finished\n", printer.stdOut)
    printer.resetIfNecessary()

    child.printOn(printer)
    assertEquals("inside test\n", printer.stdOut)
  }

  fun testBeforeAfterFailedOrder() {
    val suite = createTestProxy("parent")
    suite.setTreeBuildBeforeStart()
    val child = createTestProxy("child", suite)
    child.setTreeBuildBeforeStart()

    suite.addStdOutput("before test started\n")
    child.setStarted()
    child.addStdOutput("inside test\n")
    child.setTestFailed("fail", null, false)
    suite.addStdOutput("after test finished\n")

    val printer = MockPrinter(true)
    suite.printOn(printer)

    assertEquals("before test started\ninside test\nafter test finished\n", printer.stdOut)
    printer.resetIfNecessary()

    child.printOn(printer)
    assertEquals("inside test\n", printer.stdOut)
  }

  fun testStdErrOutput() {
    val suite = createTestProxy("parent")
    suite.setTreeBuildBeforeStart()
    val child = createTestProxy("child", suite)
    child.setTreeBuildBeforeStart()

    child.setStarted()
    suite.addStdOutput("std out\n")
    child.addStdErr("std error\n")
    child.setFinished()

    val child2 = createTestProxy("child2", suite)
    child2.setTreeBuildBeforeStart()

    child2.setStarted()
    suite.addStdOutput("std out\n")
    child2.addStdErr("std error\n")
    child2.setFinished()

    val printer = MockPrinter(true)
    suite.printOn(printer)

    assertEquals("std out\nstd error\nstd out\nstd error\n", printer.allOut)
    printer.resetIfNecessary()

    child.printOn(printer)
    assertEquals("std out\nstd error\n", printer.allOut)

    child2.printOn(printer)
    assertEquals("std out\nstd error\n", printer.allOut)
  }

  fun testBeforeAfterOrderWhenFlushed() {
    val suite = createTestProxy("parent")
    suite.setTreeBuildBeforeStart()
    val child = createTestProxy("child", suite)
    child.setTreeBuildBeforeStart()

    try {
      suite.addStdOutput("before test started\n")
      child.setStarted()
      child.addStdOutput("inside test\n")
      child.setFinished()
      suite.flush()
      suite.addStdOutput("after test finished\n")

      val printer = MockPrinter(true)
      suite.printOn(printer)

      assertEquals("before test started\ninside test\nafter test finished\n", printer.stdOut)
      printer.resetIfNecessary()

      child.printOn(printer)
      assertEquals("inside test\n", printer.stdOut)
    }
    finally {
      Disposer.dispose(child)
      Disposer.dispose(suite)
    }
  }
}