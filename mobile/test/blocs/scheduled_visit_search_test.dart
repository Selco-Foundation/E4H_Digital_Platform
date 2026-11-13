import 'dart:async';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:isar/isar.dart';

import '../../lib/blocs/scheduled_visit/scheduled_visit.dart';
import '../../lib/model/scheduled_visit/scheduled_visit.dart';
import '../../lib/repositories/scheduled_visit_repo.dart';

class UnusedIsar implements Isar {
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected database access');
}

class DelayedVisits extends ScheduledVisitRepository {
  final requests =
      StreamController<Completer<PaginatedScheduledVisits>>.broadcast();
  DelayedVisits(super.isar)
      : super(remote: ScheduledVisitRemoteRepository(dio: Dio()));
  @override
  Future<PaginatedScheduledVisits> fetchByWorkflowStatus(
      {required List<String> statuses,
      String? facilityName,
      String? sortDirection,
      int limit = 10,
      int offset = 0}) {
    final response = Completer<PaginatedScheduledVisits>();
    requests.add(response);
    return response.future;
  }
}

PaginatedScheduledVisits result(String id, {int total = 1}) =>
    PaginatedScheduledVisits(
        items: [ScheduledVisit(id: id)], totalCount: total, fromCache: true);

void main() {
  for (final loadMore in [false, true]) {
    test(
        'a new search ignores an earlier ${loadMore ? 'pagination' : 'search'} response',
        () async {
      final isar = UnusedIsar();
      final repository = DelayedVisits(isar);
      final bloc = ScheduledVisitBloc(isar, repository: repository);
      addTearDown(() async {
        await bloc.close();
        await repository.requests.close();
      });
      var next = repository.requests.stream.first;
      bloc.add(const ScheduledVisitEvent.loadInitial(
          statuses: ['PENDING_APPROVAL'], query: 'k'));
      var stale = await next;
      if (loadMore) {
        final loaded = bloc.stream.firstWhere((state) => state.maybeWhen(
            loaded: (_, __, ___, ____, _____) => true, orElse: () => false));
        stale.complete(result('first', total: 3));
        await loaded;
        next = repository.requests.stream.first;
        bloc.add(const ScheduledVisitEvent.loadMore(
            statuses: ['PENDING_APPROVAL'], query: 'k'));
        stale = await next;
      }
      next = repository.requests.stream.first;
      bloc.add(const ScheduledVisitEvent.loadInitial(
          statuses: ['PENDING_OTP_APPROVAL'], query: '7'));
      final current = await next;
      final loaded = bloc.stream.firstWhere((state) => state.maybeWhen(
          loaded: (_, __, ___, ____, _____) => true, orElse: () => false));
      current.complete(result('current'));
      await loaded;
      stale.complete(result('stale', total: 3));
      await Future<void>.delayed(Duration.zero);
      expect(
          bloc.state.maybeWhen(
              loaded: (items, _, __, ___, ____) =>
                  items.map((item) => item.id).toList(),
              orElse: () => <String?>[]),
          ['current']);
    });
  }
}
