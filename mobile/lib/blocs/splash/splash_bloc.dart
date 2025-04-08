import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'splash_bloc.freezed.dart';

@freezed
class SplashState with _$SplashState {
  const factory SplashState.initial() = SplashInitial;
  const factory SplashState.loading() = SplashLoading;
  const factory SplashState.loaded() = SplashLoaded;
}

@freezed
class SplashEvent with _$SplashEvent {
  const factory SplashEvent.loadingSplash() = LoadingSplashRequested;
}

class SplashBloc extends Bloc<SplashEvent, SplashState> {
  SplashBloc() : super(const SplashState.initial()) {
    on<LoadingSplashRequested>(_onLoadingSplash);
  }

  Future<void> _onLoadingSplash(
    LoadingSplashRequested event,
    Emitter<SplashState> emit,
  ) async {
    emit(const SplashState.loading());

    await Future.delayed(const Duration(seconds: 3));

    emit(const SplashState.loaded());
  }
}
