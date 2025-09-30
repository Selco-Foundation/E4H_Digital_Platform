import 'package:freezed_annotation/freezed_annotation.dart';

part 'solution_design_type_bom.freezed.dart';
part 'solution_design_type_bom.g.dart';

@freezed
class SolutionDesignTypeBom with _$SolutionDesignTypeBom {
  const factory SolutionDesignTypeBom({
    required String solutionDesignTypeCode,
    required List<BomEntry> bomForms,
  }) = _SolutionDesignTypeBom;

  factory SolutionDesignTypeBom.fromJson(Map<String, dynamic> json) =>
      _$SolutionDesignTypeBomFromJson(json);

  factory SolutionDesignTypeBom.fromMdmsJson(Map<String, dynamic> json) {
    final code = json['solutionDesignTypeCode']?.toString() ?? '';
    final entries = <BomEntry>[];

    final raw = json['bomForms'];
    if (raw is List) {
      for (final it in raw) {
        if (it is Map && it['name'] != null) {
          entries.add(BomEntry(name: it['name'].toString()));
        }
      }
    }

    return SolutionDesignTypeBom(
      solutionDesignTypeCode: code,
      bomForms: entries,
    );
  }
}

@freezed
class BomEntry with _$BomEntry {
  const factory BomEntry({
    required String name,
  }) = _BomEntry;

  factory BomEntry.fromJson(Map<String, dynamic> json) =>
      _$BomEntryFromJson(json);
}
