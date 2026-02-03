package io.miragon.common.zeebe.context

/**
 * Marks ProcessEngineApi methods that provide strong consistency guarantees.
 * When marked with it, operations always reflect the current status of the system
 * Used to communicate consistency characteristics in distributed Zeebe environments.
 */
annotation class StronglyConsistent()