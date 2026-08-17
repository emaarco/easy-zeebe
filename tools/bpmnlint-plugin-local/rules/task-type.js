'use strict';

const { is } = require('bpmnlint-utils');

/**
 * Styleguide "Worker IDs" (a.k.a. type ids / job types): a service task's Zeebe task type reads
 * `<serviceName>.<elementIdWithoutTypePrefix>`, both parts in camelCase (see
 * docs/bpmn-styleguide/styleguide.md).
 *
 * The type lives on `<zeebe:taskDefinition type="...">` inside the task's extensionElements.
 * bpmn-moddle parses that unknown element generically, exposing the attribute as `.type` (with
 * a `$attrs` fallback for safety). A service task with no task definition is left alone.
 */
const TASK_TYPE = /^[a-z][A-Za-z0-9]*\.[a-z][A-Za-z0-9]*$/;

function taskTypeOf(node) {
  const extension = node.extensionElements;
  const definition =
    extension && extension.values && extension.values.find((value) => value.$type === 'zeebe:taskDefinition');

  if (!definition) {
    return null;
  }

  return definition.type || (definition.$attrs || {})['type'] || null;
}

/**
 * @type { import('bpmnlint').RuleFactory }
 */
module.exports = function () {
  function check(node, reporter) {
    if (!is(node, 'bpmn:ServiceTask')) {
      return;
    }

    const type = taskTypeOf(node);

    if (!type) {
      return;
    }

    if (!TASK_TYPE.test(type)) {
      reporter.report(node.id, `Task type <${type}> should read <serviceName.myCamelCaseTopic> (styleguide: Worker IDs)`);
    }
  }

  return { check };
};
