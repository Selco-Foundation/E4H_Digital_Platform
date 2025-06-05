import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:isar/isar.dart';

import '../../data/nosql/cache_add_new_asset.dart';

part 'overall_asset_summary.freezed.dart';

@freezed
class OverallAssetSummaryEvent with _$OverallAssetSummaryEvent {
  const factory OverallAssetSummaryEvent.loadCounts({
    required String projectId,
  }) = OverallAssetSummaryEventLoadCounts;
}

@freezed
class OverallAssetSummaryState with _$OverallAssetSummaryState {
  const factory OverallAssetSummaryState.initial() = _Initial;
  const factory OverallAssetSummaryState.loading() = _Loading;
  const factory OverallAssetSummaryState.error(String message) = _Error;
  const factory OverallAssetSummaryState.loaded({
    required int batteryCount,
    required int inverterCount,
    required int panelCount,
  }) = _Loaded;
}

class OverallAssetSummaryBloc
    extends Bloc<OverallAssetSummaryEvent, OverallAssetSummaryState> {
  final Isar _isar;

  OverallAssetSummaryBloc(this._isar)
      : super(const OverallAssetSummaryState.initial()) {
    on<OverallAssetSummaryEventLoadCounts>(_onLoadCounts);
  }

  Future<void> _onLoadCounts(
    OverallAssetSummaryEventLoadCounts event,
    Emitter<OverallAssetSummaryState> emit,
  ) async {
    emit(const OverallAssetSummaryState.loading());
    try {
      final batteryCount = await _isar.cacheAddNewAssets
          .where()
          .projectIdEqualTo(event.projectId)
          .filter()
          .assetTypeEqualTo('battery')
          .count();

      final inverterCount = await _isar.cacheAddNewAssets
          .where()
          .projectIdEqualTo(event.projectId)
          .filter()
          .assetTypeEqualTo('inverter')
          .count();

      final panelCount = await _isar.cacheAddNewAssets
          .where()
          .projectIdEqualTo(event.projectId)
          .filter()
          .assetTypeEqualTo('panel')
          .count();

      emit(OverallAssetSummaryState.loaded(
        batteryCount: batteryCount,
        inverterCount: inverterCount,
        panelCount: panelCount,
      ));
    } catch (e) {
      emit(OverallAssetSummaryState.error(e.toString()));
    }
  }
}
