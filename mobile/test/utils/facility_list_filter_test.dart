import 'package:flutter_test/flutter_test.dart';
import '../../lib/utils/facility_list_filter.dart';

void main() {
  final records = [
    (id: 'a', name: 'Kanur 7', date: DateTime(2026, 1, 1)),
    (id: 'b', name: 'Maldare', date: DateTime(2026, 1, 2)),
    (id: 'c', name: 'KANUR North', date: DateTime(2026, 1, 3)),
  ];
  List<String> search(String query, String? filter,
          {List<String> bookmarks = const []}) =>
      filterFacilityList(records,
              query: query,
              filter: filter,
              id: (record) => record.id,
              name: (record) => record.name,
              date: (record) => record.date,
              bookmarkedIds: bookmarks)
          .map((record) => record.id)
          .toList();

  test(
      'matches partial names and single letters or digits without changing the source',
      () {
    expect(search(' kAnUr ', null), ['a', 'c']);
    expect(search('k', null), ['a', 'c']);
    expect(search('7', null), ['a']);
    expect(search('missing', null), isEmpty);
    expect(search('   ', null), ['a', 'b', 'c']);
    expect(records.map((record) => record.id), ['a', 'b', 'c']);
  });
  test(
      'sorts before slicing and intersects eligible records with bookmark order',
      () {
    expect(search('kanur', 'DESC'), ['c', 'a']);
    expect(search('kanur', 'ASC'), ['a', 'c']);
    expect(search('', 'BOOKMARKED', bookmarks: ['unrelated', 'c', 'a']),
        ['c', 'a']);
    expect(search('7', 'BOOKMARKED', bookmarks: ['c', 'a']), ['a']);
    expect(search('', 'BOOKMARKED', bookmarks: ['c']), ['c']);
    expect(search('', null), ['a', 'b', 'c']);
  });
}
