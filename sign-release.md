## Sign release
1) `gpg --full-generate-key`
2) `gpg --list-keys` (will show key with a long Key Id)
3) `gpg --export-secret-keys [Your-Key-ID] > secring.gpg`
4) Copy `gpg --export-secret-keys --armor [Your-Key-ID]` and use it in `signing.key` property
5) Take last 8 chars from Key Id and set to `signing.keyId`
6) Set `signing.password` as password for the key
7) Set `signing.secretKeyRingFile` as path to `secring.gpg` file
8) `gpg --export --armor [Your-Key-ID] > mypublickey.asc`
9) Go to https://keys.openpgp.org and upload `mypublickey.asc` then follow the steps from this site (ex. verification from email)
10) Ready to do `gradle publish` command