import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'asset_type.freezed.dart';

// states
@freezed
class AssetTypeState with _$AssetTypeState {
  const factory AssetTypeState.initial() = AssetTypeInitial;
  const factory AssetTypeState.inverter() = AssetTypeInverter;
  const factory AssetTypeState.battery() = AssetTypeBattery;
  const factory AssetTypeState.panel() = AssetTypePanel;
}

// events
@freezed
class AssetTypeEvent with _$AssetTypeEvent {
  const factory AssetTypeEvent.typeSelected(String assetType) =
      AssetTypeSelected;
}

// bloc
class AssetTypeBloc extends Bloc<AssetTypeEvent, AssetTypeState> {
  AssetTypeBloc() : super(const AssetTypeState.initial()) {
    on<AssetTypeSelected>(_onTypeSelected);
  }

  Future<void> _onTypeSelected(
    AssetTypeSelected event,
    Emitter<AssetTypeState> emit,
  ) async {
    switch (event.assetType.toLowerCase()) {
      case 'inverter':
        emit(const AssetTypeState.inverter());
        break;
      case 'battery':
        emit(const AssetTypeState.battery());
        break;
      case 'panel':
        emit(const AssetTypeState.panel());
        break;
      default:
        emit(const AssetTypeState.initial());
    }
  }
}
