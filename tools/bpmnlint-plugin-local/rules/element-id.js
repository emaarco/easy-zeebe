'use strict';

/**
 * Styleguide "Element IDs": every automation-relevant element carries an id of the form
 * `<type>_<name>`, both `<type>` and `<name>` in lowerCamelCase — so the whole id reads as one
 * camelCase token with an underscore separating the type from the name (e.g. `flow_noSpots`,
 * `serviceTask_sendWelcomeMail`). See docs/bpmn-styleguide/styleguide.md.
 *
 * Coverage is the executable set: every event, task and gateway kind, sub-processes, call
 * activities, transactions, ad-hoc sub-processes and sequence flows. Non-executable elements
 * Zeebe ignores — text annotations, groups, associations, data objects/stores, pools/lanes —
 * are deliberately skipped: their ids are auto-generated and carry no automation meaning, so the
 * rule never fires on an element the styleguide has no opinion about.
 *
 * A type may accept MORE THAN ONE base prefix — a sensible specific one and the normal generic
 * one, both valid. A boundary event may read `boundary_` or `event_`; a transaction or ad-hoc
 * sub-process may read its specific prefix or the generic `subProcess_`.
 *
 * Events may additionally qualify a prefix with their event definition (`timerStartEvent_`,
 * `messageBoundary_`, ...). That qualifier is optional, but one that IS present must be true: a
 * `timerStartEvent_` on a message start event is a finding, because an id that lies about the
 * model is worse than one that stays silent about it.
 */
const PREFIX_BY_TYPE = {
  'bpmn:StartEvent': 'startEvent',
  'bpmn:IntermediateCatchEvent': 'event',
  'bpmn:IntermediateThrowEvent': 'event',
  'bpmn:EndEvent': 'endEvent',
  'bpmn:BoundaryEvent': ['event', 'boundary'],
  'bpmn:Task': 'task',
  'bpmn:ServiceTask': 'serviceTask',
  'bpmn:UserTask': 'userTask',
  'bpmn:SendTask': 'sendTask',
  'bpmn:ReceiveTask': 'receiveTask',
  'bpmn:ManualTask': 'manualTask',
  'bpmn:ScriptTask': 'scriptTask',
  'bpmn:BusinessRuleTask': 'businessRuleTask',
  'bpmn:ExclusiveGateway': 'gateway',
  'bpmn:ParallelGateway': 'gateway',
  'bpmn:InclusiveGateway': 'gateway',
  'bpmn:EventBasedGateway': 'gateway',
  'bpmn:ComplexGateway': 'gateway',
  'bpmn:SubProcess': 'subProcess',
  'bpmn:Transaction': ['subProcess', 'transaction'],
  'bpmn:AdHocSubProcess': ['subProcess', 'adHocSubProcess'],
  'bpmn:CallActivity': 'callActivity',
  'bpmn:SequenceFlow': 'flow',
};

/**
 * Every qualifier the styleguide knows, so that a wrong one is reported as a contradiction
 * rather than silently accepted as part of the Name.
 */
const QUALIFIERS = [
  'message',
  'timer',
  'signal',
  'conditional',
  'escalation',
  'error',
  'link',
  'terminate',
  'compensate',
];

function qualifiersOf(node) {
  return (node.eventDefinitions || [])
    .map((definition) => /^bpmn:(\w+)EventDefinition$/.exec(definition.$type))
    .filter(Boolean)
    .map((match) => match[1].toLowerCase());
}

function qualify(qualifier, prefix) {
  return qualifier + prefix.charAt(0).toUpperCase() + prefix.slice(1);
}

/**
 * @type { import('bpmnlint').RuleFactory }
 */
module.exports = function () {
  function check(node, reporter) {
    const mapping = PREFIX_BY_TYPE[node.$type];

    if (!mapping || !node.id) {
      return;
    }

    const bases = [].concat(mapping);
    const qualifiers = qualifiersOf(node);
    const allowed = bases.flatMap((base) => [base].concat(qualifiers.map((qualifier) => qualify(qualifier, base))));

    if (allowed.some((candidate) => new RegExp(`^${candidate}_[a-z][A-Za-z0-9]*$`).test(node.id))) {
      return;
    }

    const claimed = QUALIFIERS.find((qualifier) =>
      bases.some((base) => node.id.startsWith(qualify(qualifier, base) + '_')),
    );

    if (claimed) {
      reporter.report(
        node.id,
        `Element id ${node.id} claims to be a ${claimed} event, but the element has no ${claimed} event definition`,
      );
      return;
    }

    reporter.report(node.id, `Element id ${node.id} should read <${allowed.join('|')}>_<camelCaseName>`);
  }

  return { check };
};
