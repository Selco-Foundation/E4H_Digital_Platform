String canonicalBomFormName(Object? value) {
  const assetFormPrefix = 'assetform.';
  final normalized = (value ?? '').toString().trim().toLowerCase();

  return normalized.startsWith(assetFormPrefix)
      ? normalized.substring(assetFormPrefix.length)
      : normalized;
}

Map<String, dynamic> selectBomFormConfig(
  Iterable<Map<String, dynamic>> documents,
  String requestedName,
) {
  final canonicalRequestedName = canonicalBomFormName(requestedName);
  if (canonicalRequestedName.isEmpty) {
    throw StateError('BOM FormConfig name cannot be empty.');
  }

  final matches = documents.where((document) {
    final data = document['data'];
    final dataName = data is Map ? data['name'] : null;
    final uniqueIdentifier = document['uniqueIdentifier'];

    return canonicalBomFormName(dataName) == canonicalRequestedName ||
        canonicalBomFormName(uniqueIdentifier) == canonicalRequestedName;
  }).toList();

  if (matches.isEmpty) {
    throw StateError(
      'No BOM FormConfig canonically matches "$requestedName".',
    );
  }
  if (matches.length > 1) {
    final identifiers = matches
        .map((document) => document['uniqueIdentifier']?.toString() ?? '')
        .join(', ');
    throw StateError(
      'Multiple BOM FormConfigs canonically match "$requestedName": '
      '$identifiers.',
    );
  }

  return matches.single;
}
