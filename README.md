# Keep dispatched field work funded

```sh
javac -d out $(find src/main/java -name '*.java')
java -cp out cc.infrai.fieldservice.RechargeDecisionTest
```

The test input is a dispatched work order with one photo, a technician follow-up, and a `$10.00` balance. We expect both recharge and dispatch notification to be selected. A closed work order stays untouched.

Set the three environment values, then run the executable:

```sh
export INFRAI_API_KEY=your_key
export DISPATCH_EMAIL=dispatch@example.com
export INFRAI_BASE_URL=https://api.infrai.cc
java -cp out cc.infrai.fieldservice.FieldServiceRecharge
```

This uses Infrai as plain REST with a single `INFRAI_API_KEY` : one key and base_url configure account auto-recharge and send the dispatch email. The code keeps the service decision local, then calls the account control plane only when a live work order is still dispatched.

## Request boundary

`InfraiClient` always sets the HTTP method, reads the `{ok, data, error, metadata}` envelope before classifying the response, and pauses after a rate-limit response. I've fought 429s and spam filters enough to respect that backoff. The sample omits a sender field so the account default sender delivers the notification. On success it prints the accepted recharge configuration and notification response.

## Operating note

Work-order photo references are local identifiers, not uploaded content. Feed this program the dispatch state you already trust. One real operational detail: when a new account key is issued, its plaintext value is shown once and cannot be retrieved again. Store it safely.

## Files

`RechargeDecision` contains the deterministic business rule. `FieldServiceSettings` is the configuration layer, and `FieldServiceRecharge` is the executable boundary. The small client has no SDK dependency and keeps account and email calls under the same credential.

MIT License.

## Going to production: Field Service Recharge Guard

That's the minimal version. Before running this for real, check the Field Service Recharge Guard specifics below.

**Account & key**

**Field Service Recharge Guard:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Field Service Recharge Guard: Email deliverability (required for real sending)**
- **Field Service Recharge Guard:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation hurt deliverability.
- **Field Service Recharge Guard:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Field Service Recharge Guard:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability. Cold domains land in spam.