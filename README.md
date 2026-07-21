# meter-status

Tracks whether smart meters are reporting.

Meters at customer sites heartbeat every 30 seconds over MQTT. A scheduled sweeper marks a
meter offline if we have not heard from it within 90 seconds, and publishes an event that
pages the on-call rota.

## Running

```bash
mvn test          # test suite
mvn spring-boot:run
```

Requires JDK 21 and Maven 3.8+.

## Layout

| File | What it does |
|---|---|
| `MeterStatusService` | Heartbeat recording, status reads, and the offline sweeper |
| `MeterRepository` / `InMemoryMeterRepository` | Persistence. The production impl talks to MySQL with the same semantics |
| `MeterStatus` | Immutable status snapshot: online flag + last-seen timestamp |
| `MeterWentOffline` / `EventPublisher` | The event that pages on-call |

Time is injected (`java.time.Clock`, Guava `Ticker`) so the tests can move it by hand
instead of sleeping. See `MutableClock` in the test sources.

---

## Your ticket: SUP-2841

> **Meters flapping online/offline at Northfield plant**
>
> Priority: High · Reported by: Support
>
> Since Tuesday, meters at the Northfield site are flapping - going offline, then straight
> back online, over and over. Each transition pages the on-call engineer. We have had 40+
> pages overnight.
>
> The meters are fine. Field techs confirmed they are powered, on the network, and
> publishing heartbeats normally. Northfield is our largest site (~900 meters) so it
> generates the most pages, which is probably why it got reported first - we have not
> checked whether other sites are affected too.
>
> One of the failing cases is reproduced by a test in this repo.

`mvn test` - one test fails.
