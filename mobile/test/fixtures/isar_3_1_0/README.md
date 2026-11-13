# Original Isar upgrade fixture

`E4H.isar.gz` contains the gzip-compressed original `E4H.isar`, created using `isar` and `isar_flutter_libs` **3.1.0+1**,
Flutter 3.22.2, and the application's original generated schemas at commit
`490c5db4d88ee4c33f48316f1fac1660952c01af`.

It contains synthetic records only: assessment draft ID 41, queued submission
ID 42, media reference ID 43, and successful checkpoint ID 44. The database name
is `E4H`. No production data or credentials are included.

Uncompressed database SHA-256: `f6a94b176b4d6b7ca8a5659dfde13555bc0a0752dbc1c7b4fc8eaa2861bd4d67`.

`test/repositories/isar_upgrade_test.dart` decompresses a copy before opening it
with the community runtime, checks its indexed queries and original IDs, writes
new data, and closes/reopens the database. Keep this original file unchanged;
regenerating it with the new runtime would invalidate the upgrade test.
