import '../repositories/cache_fallback.dart';

List<T> filterFacilityList<T>(
  Iterable<T> records, {
  required String query,
  required String? filter,
  required String Function(T) id,
  required String? Function(T) name,
  required DateTime Function(T) date,
  List<String> bookmarkedIds = const [],
}) {
  final ranks = {
    for (var i = 0; i < bookmarkedIds.length; i++) bookmarkedIds[i]: i
  };
  final result = records
      .where((record) =>
          matchesFacilityName(name(record), query) &&
          (filter != 'BOOKMARKED' || ranks.containsKey(id(record))))
      .toList();
  if (filter == 'BOOKMARKED') {
    result.sort((a, b) => ranks[id(a)]!.compareTo(ranks[id(b)]!));
  } else if (filter == 'ASC' || filter == 'DESC') {
    result.sort((a, b) {
      final order = date(a).compareTo(date(b));
      if (order == 0) return id(a).compareTo(id(b));
      return filter == 'ASC' ? order : -order;
    });
  }
  return result;
}
