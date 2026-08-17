'use strict';

const { is } = require('bpmnlint-utils');

/**
 * Styleguide "Message IDs": `<serviceName>.<state>`, both parts in camelCase (see
 * docs/bpmn-styleguide/styleguide.md).
 *
 * The correlation key is the message *name*, not the `Message_<hash>` id the modeler
 * generates — so that is what this rule checks. A message without a name is left alone.
 */
const MESSAGE_NAME = /^[a-z][A-Za-z0-9]*\.[a-z][A-Za-z0-9]*$/;

/**
 * @type { import('bpmnlint').RuleFactory }
 */
module.exports = function () {
  function check(node, reporter) {
    if (!is(node, 'bpmn:Message') || !node.name) {
      return;
    }

    if (!MESSAGE_NAME.test(node.name)) {
      reporter.report(node.id, `Message name <${node.name}> should read <serviceName.myCamelCaseState>`);
    }
  }

  return { check };
};
