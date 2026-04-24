package io.miragon.common.test.assertions

import io.camunda.process.test.api.assertions.ProcessInstanceAssert
import io.github.emaarco.bpmn.runtime.ElementId

fun ProcessInstanceAssert.hasCompletedElements(vararg elements: ElementId): ProcessInstanceAssert =
    hasCompletedElements(*elements.map { it.value }.toTypedArray())
