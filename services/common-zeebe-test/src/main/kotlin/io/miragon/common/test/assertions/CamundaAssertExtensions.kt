package io.miragon.common.test.assertions

import io.camunda.process.test.api.assertions.ProcessInstanceAssert
import io.miragon.bpmn.runtime.ElementId
import io.miragon.bpmn.runtime.path.ProcessPath

fun ProcessInstanceAssert.hasCompletedElements(vararg elements: ElementId): ProcessInstanceAssert =
    hasCompletedElements(*elements.map { it.value }.toTypedArray())

fun ProcessInstanceAssert.hasCompletedElementsInOrder(path: ProcessPath<*>): ProcessInstanceAssert =
    hasCompletedElementsInOrder(*path.ids.toTypedArray())
