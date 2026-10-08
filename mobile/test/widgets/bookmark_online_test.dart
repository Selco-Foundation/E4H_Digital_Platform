import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/data/network_manager.dart';
import '../../lib/model/activity_facility/activity_facility.dart';
import '../../lib/model/activity_facility_workflow/activity_facility_workflow.dart';
import '../../lib/model/scheduled_visit/scheduled_visit.dart';
import '../../lib/repositories/report_bookmark_repo.dart';
import '../../lib/widgets/bookmarks/report_bookmarks.dart';

class BookmarkHarness<T> extends StatefulWidget {
  const BookmarkHarness(
      {super.key,
      required this.repository,
      required this.item,
      required this.ensureOnline});
  final ReportBookmarkRepository<T> repository;
  final T item;
  final Future<void> Function() ensureOnline;
  @override
  State<BookmarkHarness<T>> createState() => _BookmarkHarnessState<T>();
}

class _BookmarkHarnessState<T> extends State<BookmarkHarness<T>>
    with ReportBookmarksState<BookmarkHarness<T>, T> {
  @override
  void initState() {
    super.initState();
    bookmarks = widget.repository;
    bookmarkSaveFailedKey = 'SAVE_FAILED';
    loadBookmarks(only: true);
  }

  @override
  Widget build(BuildContext context) => TextButton(
        onPressed: !bookmarksLoaded || savingBookmarks.isNotEmpty
            ? null
            : () => toggleBookmark(widget.item,
                reload: () => loadBookmarks(only: true),
                ensureOnline: widget.ensureOnline),
        child: Text(savingBookmarks.isNotEmpty
            ? 'Saving'
            : bookmarkIds.isEmpty
                ? 'Add'
                : 'Remove'),
      );
}

void main() {
  setUp(() => FlutterSecureStorage.setMockInitialValues({}));
  for (final amc in [false, true]) {
    testWidgets(
        '${amc ? 'AMC' : 'installation'} requires internet only for new bookmarks',
        (tester) async {
      final repository = (amc
          ? AmcBookmarkRepository(
              tenantId: 'tenant', userId: 'user', userType: 'AMC')
          : InstallationBookmarkRepository(
              tenantId: 'tenant',
              userId: 'user',
              userType: 'FIELD_STAFF')) as ReportBookmarkRepository<dynamic>;
      final dynamic item = amc
          ? const ScheduledVisit(id: 'one')
          : ActivityFacilityWorkflow(
              activityFacility: ActivityFacility(id: 'one'));
      var checks = 0;
      var offline = true;
      final waiting = Completer<void>();
      Future<void> online() async {
        checks++;
        if (offline) throw const NetworkException('No internet access');
        await waiting.future;
      }

      await tester.pumpWidget(MaterialApp(
          home: Scaffold(
              body: BookmarkHarness(
                  repository: repository, item: item, ensureOnline: online))));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Add'));
      await tester.pumpAndSettle();
      expect(await repository.ids(), isEmpty);
      expect(
          find.text(amc
              ? 'AMC_BOOKMARKS_ONLINE_REQUIRED'
              : 'INSTALLATION_BOOKMARKS_ONLINE_REQUIRED'),
          findsOneWidget);
      expect(tester.widget<TextButton>(find.byType(TextButton)).onPressed,
          isNotNull);
      offline = false;
      await tester.tap(find.text('Add'));
      await tester.pump();
      expect(find.text('Saving'), findsOneWidget);
      expect(
          tester.widget<TextButton>(find.byType(TextButton)).onPressed, isNull);
      waiting.complete();
      await tester.pumpAndSettle();
      expect(await repository.ids(), {'one'});
      offline = true;
      await tester.tap(find.text('Remove'));
      await tester.pumpAndSettle();
      expect(await repository.ids(), isEmpty);
      expect(checks, 2);
      expect(tester.takeException(), isNull);
    });
  }
}
