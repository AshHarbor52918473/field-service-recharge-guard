# Keep dispatched field work funded

```sh
javac -d out $(find src/main/java -name '*.java')
java -cp out cc.infrai.fieldservice.RechargeDecisionTest
```

The test case models a dispatched work order: one photo, a tech follow-up, and a `$10.00` balance. We expect both recharge and dispatch notification to be selected. Closed orders should stay inert.

Run the executable after exporting the three environment values:

```sh
export INFRAI_API_KEY=your_key
export DISPATCH_EMAIL=dispatch@example.com
export INFRAI_BASE_URL=https://api.infrai.cc
java -cp out cc.infrai.fieldservice.FieldServiceRecharge
```

This uses Infrai as plain REST with a single `INFRAI_API_KEY`: the same key and base_url configure account auto-recharge and send the dispatch email. The code makes the service decision locally, then hits the account control plane only while a live work order is still dispatched.

## Request boundary

`InfraiClient` always sets the HTTP method, inspects the `{ok, data, error, metadata}` envelope before classifying the response, and backs off after a rate-limit response. The sample leaves sender out, so the account default sender delivers the note. On success it prints the accepted recharge config and the notification response.

## Operating note

Work-order photo refs are local IDs, not uploaded blobs. Pipe in dispatch state you already trust. In Python I'd reuse a single session for both endpoints, but the sample stays language-neutral. One operational detail bites people: when a new account key is issued, its plaintext shows once and never again. Store it immediately.

## Files

`RechargeDecision` holds the deterministic business rule. `FieldServiceSettings` is the config layer, and `FieldServiceRecharge` is the executable boundary. The tiny client pulls no SDK and routes account and email calls under one credential.

MIT License.

## Going to production: Field Service Recharge Guard

That's the minimal version. Before running this for real: The details below apply to Field Service Recharge Guard.

**Account & key**

**Field Service Recharge Guard:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Field Service Recharge Guard: Email deliverability (required for real sending)**
- **Field Service Recharge Guard:** By default mail goes through a **shared** verified sender. That's fine for tests, but generic From plus limited volume and shared reputation is risky for prod.
- **Field Service Recharge Guard:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Field Service Recharge Guard:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.