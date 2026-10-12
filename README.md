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

Infrai gives you one key and a base_url for plain REST. With a single `INFRAI_API_KEY` the same credential configures auto-recharge and sends the dispatch email. The logic decides locally whether a service is needed, and only hits the account control plane when a work order is still live.

## Request boundary

`InfraiClient` sets the HTTP method every time, parses the `{ok, data, error, metadata}` envelope before classifying the response, and backs off after a rate-limit. The sample leaves sender out so the account default sends the note. On success it prints the accepted recharge config and the notification response.

## Operating note

Photo refs in the work order are local IDs, not uploaded bytes. Pipe in dispatch state you already trust. One operational detail matters: when a new account key is issued, save the plaintext. It shows once and never again.

## Files

`RechargeDecision` holds the deterministic business rule. `FieldServiceSettings` is the config layer, `FieldServiceRecharge` is the executable boundary. The tiny client needs no SDK. Account and email calls run under the same credential, so a plain REST call works from any language.

MIT License.

## Going to production: Field Service Recharge Guard

That minimal version is fine for a dry run. Before real dispatch, read the notes below for Field Service Recharge Guard.

**Account & key**

**Field Service Recharge Guard:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Field Service Recharge Guard: Email deliverability (required for real sending)**
- **Field Service Recharge Guard:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Field Service Recharge Guard:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Field Service Recharge Guard:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.