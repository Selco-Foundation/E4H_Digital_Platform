import 'package:isar/isar.dart';
import 'package:selco/data/nosql/cache_add_new_asset.dart';
import 'package:selco/data/nosql/cache_asset_count.dart';

class AssetStore implements Isar {
  final counts = <CacheAssetCount>[];
  final assets = <CacheAddNewAsset>[];
  @override
  IsarCollection<T> collection<T>() =>
      AssetCollection<T>((T == CacheAssetCount ? counts : assets).cast<T>());
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected database mutation');
}

class AssetCollection<T> extends IsarCollection<T> {
  final List<T> records;
  AssetCollection(this.records);
  @override
  Query<R> buildQuery<R>(
      {List<WhereClause> whereClauses = const [],
      bool whereDistinct = false,
      Sort whereSort = Sort.asc,
      FilterOperation? filter,
      List<SortProperty> sortBy = const [],
      List<DistinctProperty> distinctBy = const [],
      int? offset,
      int? limit,
      String? property}) {
    Object? value(T record, String property) {
      if (record is CacheAssetCount) {
        return property == 'assetType'
            ? record.assetType
            : record.activityFacilityId;
      }
      final asset = record as CacheAddNewAsset;
      return property == 'assetType'
          ? asset.assetType
          : asset.activityFacilityId;
    }

    bool matches(T record, FilterOperation? operation) {
      if (operation is FilterCondition) {
        return value(record, operation.property) == operation.value1;
      }
      if (operation is FilterGroup) {
        return operation.filters.every((f) => matches(record, f));
      }
      return true;
    }

    return AssetQuery<R>(records
        .where((record) =>
            matches(record, filter) &&
            whereClauses.whereType<IndexWhereClause>().every((w) =>
                w.lower == null ||
                value(record, w.indexName) == w.lower!.first))
        .cast<R>()
        .toList());
  }

  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected collection operation');
}

class AssetQuery<T> implements Query<T> {
  final List<T> records;
  AssetQuery(this.records);
  @override
  Future<List<T>> findAll() async => records;
  @override
  Future<int> count() async => records.length;
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected query operation');
}

CacheAddNewAsset asset(String type, {String facility = 'facility'}) =>
    CacheAddNewAsset(
        activityFacilityId: facility,
        assetType: type,
        itemNumber: '1',
        serialNumber: 'serial',
        photoPath: '',
        latitude: '',
        longitude: '');
CacheAssetCount count(String type,
        {int quantity = 1, String facility = 'facility'}) =>
    CacheAssetCount(
        activityFacilityId: facility, assetType: type, count: quantity);
