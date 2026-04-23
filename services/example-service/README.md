# 🌟 example-service

This service is a showcase that demonstrates how to integrate and use **Zeebe** within a Spring service.
It leverages both the **common-zeebe** and **common-zeebe-test** modules to orchestrate
and test BPMN processes seamlessly.

This service provides a hands-on example with the **MiraVelo Inner Circle membership process**
and various REST endpoints to interact with it.

## 🧩 Process overview

The `miravelo-membership` process is triggered by publishing the `miravelo.membershipRequested`
message (the `POST /api/memberships` endpoint does this via the process adapter).
Key steps:

1. **Claim Membership** — reserves one of the 1000 spots; sets `hasEmptySpots`.
2. **No spots** → **Send Rejection Mail** → `Membership rejected`.
3. **Spot available** → opens the **Confirm Membership** sub-process:
    - **Send Confirmation Mail** → wait for the user via a **user task**.
    - A **non-interrupting daily timer** fires `Re-Send Confirmation Mail` to nudge the user.
    - An **interrupting 3½-day timer** aborts the sub-process and flows into `Revoke Membership Request`.
    - A **`miravelo.confirmationRejected` message boundary event** aborts the sub-process on user rejection,
      also flowing into `Revoke Membership Request`.
    - On abort/rejection, **compensation** triggers `Revoke Claim` so the spot is released back to the pool,
      and the process ends on `Membership declined`.
4. **Happy path** (user confirmed) → **Send Welcome Mail** → `Membership activated` (throws the
   `miravelo.membershipActivated` signal).

## 🧪 REST endpoints

- `POST /api/memberships` — submit a registration (publishes the start message).
- `POST /api/memberships/confirm/{membershipId}` — complete the user task for this member.
- `POST /api/memberships/reject-confirmation/{membershipId}` — publish `miravelo.confirmationRejected`.

## 🔧 Key Features

- **Zeebe Integration**: Utilizes `common-zeebe` for connecting and interacting with the Zeebe process engine.
- **Process Testing**: Incorporates `common-zeebe-test` to provide robust, integrated process testing that
  covers timers, compensation, and the signal end event.
- **REST API**: Offers multiple endpoints to interact with the MiraVelo membership process.
