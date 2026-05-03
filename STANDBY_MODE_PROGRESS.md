# Alerting Standby Mode Progress

## Current branch

- Repository: `/Users/craigperkins/Projects/OpenSearch/alerting`
- Branch: `standby`

## What changed

- Added a temporary plugin-local dynamic setting:
  - `cluster.standby_mode`
  - This should be replaced with the core OpenSearch setting constant once this repo builds against the core change.
- Wired the setting into `AlertingPlugin` with an `AtomicBoolean` update consumer.
- Added `StandbyModeActionFilter` to reject mutating alerting transport actions with `403 FORBIDDEN` while standby mode is enabled.
- Updated the local alerting scheduler and sweeper to avoid scheduling monitor/workflow executions while standby mode is enabled.
- Updated background maintenance paths to avoid local standby writes:
  - alert history rollover/deletion
  - finding history rollover/deletion
  - comments history rollover/deletion
  - external scheduler queue polling
- Added focused unit tests and a REST integration test for monitor write rejection and recovery after disabling standby mode.

## Why this shape

Alerting stores monitor/workflow configuration in `.opendistro-alerting-config` and also writes alerts, findings, comments, and history indices. In a standby follower, the replicated state should come from the active leader. The follower should not execute monitors locally or accept local alerting mutations because that can create divergent alerts, findings, comments, schedules, or monitor definitions.

## Validation

Focused unit and integration tests:

```bash
./gradlew :alerting-core:test --tests 'org.opensearch.alerting.core.schedule.JobSchedulerStandbyModeTests' :alerting:test --tests 'org.opensearch.alerting.standby.StandbyModeActionFilterTests' :alerting:integTest --tests 'org.opensearch.alerting.resthandler.MonitorRestApiIT.test standby mode rejects monitor writes'
```

Passed locally.

## Follow-up

- Replace the plugin-local `cluster.standby_mode` setting with the core OpenSearch setting constant after the core standby setting is available to this repo.
- Consider whether alert/finding/comment history indices should be part of the CCR replication scope for the disaster-recovery story, or whether only monitor/workflow configuration should be replicated initially.
