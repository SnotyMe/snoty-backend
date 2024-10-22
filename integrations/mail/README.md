# integrations/mail
## Included Nodes
### Global Mail (`integration:mail`)
Sends mails over an administrator configured mailserver.
Be aware: without proper external ratelimiting, users could spam
your mailserver! Additionally, spammy mails can result in your mailserver getting blocked. Only configure if you are 100% sure you won't face any abuse issues!!

Currently, just SMTP is supported, but we may expand later (just open an issue).

Config:
```yml
globalMail:
  type: Smtp
  host: mail.example.com
  port: 25
  startTls: true
  username: myusername
  password: mypassword
  from: snoty@example.com
```
