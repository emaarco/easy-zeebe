package io.miragon.common.test.assertions

import io.camunda.process.test.api.assertions.ProcessInstanceAssert
import io.miragon.bpmn.runtime.path.ProcessPath

fun ProcessInstanceAssert.hasCompletedElementsInOrder(path: ProcessPath<*>): ProcessInstanceAssert =
    hasCompletedElementsInOrder(*path.ids.toTypedArray())
