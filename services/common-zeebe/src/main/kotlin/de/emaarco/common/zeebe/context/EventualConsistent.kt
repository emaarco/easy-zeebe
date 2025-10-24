package de.emaarco.common.zeebe.context

/**
 * Marks ProcessEngineApi methods that provide eventual consistency guarantees.
 * Results may not reflect recent changes due to distributed system propagation delays.
 * Used to communicate consistency characteristics in distributed Zeebe environments.
 */
annotation class EventualConsistent()
