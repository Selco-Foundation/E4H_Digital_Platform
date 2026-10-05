import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../blocs/auth/authbloc.dart';
import '../../blocs/user_type/user_type.dart';
import '../../repositories/report_bookmark_repo.dart';
import '../../utils/envConfig.dart';
import '../../utils/extensions.dart';
import '../../utils/i18_key_constants.dart' as i18;
import '../../utils/utils.dart';
import '../cards/report_card.dart';

ReportBookmarkRepository<T> reportBookmarksFor<T>(BuildContext context,
    {required bool amc}) {
  final user = context.read<AuthBloc>().state.maybeWhen(
        authenticated: (_, __, user) => user,
        orElse: () => null,
      );
  final userType = amc
      ? USER_TYPES.AMC.name
      : context.read<UserTypeBloc>().state.maybeWhen(
            supervisor: () => USER_TYPES.SUPERVISOR.name,
            orElse: () => USER_TYPES.FIELD_STAFF.name,
          );
  final userId = user?.uuid ?? user?.userName ?? '';
  final tenantId = envConfig.variables.tenantId;
  return (amc
      ? AmcBookmarkRepository(
          tenantId: tenantId, userId: userId, userType: userType)
      : InstallationBookmarkRepository(
          tenantId: tenantId,
          userId: userId,
          userType: userType)) as ReportBookmarkRepository<T>;
}

mixin ReportBookmarksState<W extends StatefulWidget, T> on State<W> {
  late ReportBookmarkRepository<T> bookmarks;
  late String bookmarkSaveFailedKey;
  Set<String> bookmarkIds = {};
  final Set<String> savingBookmarks = {};
  List<T> bookmarkedItems = [];
  bool bookmarksLoaded = false;
  bool bookmarksLoading = true;
  bool bookmarksFailed = false;
  int _generation = 0;

  Future<void> loadBookmarks(
      {bool only = false, String query = '', String sortOrder = 'DESC'}) async {
    final generation = ++_generation;
    if (mounted) {
      setState(() {
        bookmarksLoading = true;
        bookmarksFailed = false;
      });
    }
    try {
      final ids = await bookmarks.ids();
      final items = only
          ? await bookmarks.list(query: query, sortOrder: sortOrder)
          : <T>[];
      if (!mounted || generation != _generation) return;
      setState(() {
        bookmarkIds = ids;
        bookmarkedItems = items;
        bookmarksLoaded = true;
        bookmarksLoading = false;
      });
    } catch (_) {
      if (!mounted || generation != _generation) return;
      setState(() {
        bookmarksLoading = false;
        bookmarksFailed = true;
      });
    }
  }

  Future<void> toggleBookmark(T item,
      {required Future<void> Function() reload}) async {
    final id = bookmarks.identity(item).trim();
    if (id.isEmpty || !bookmarksLoaded || savingBookmarks.contains(id)) return;
    setState(() => savingBookmarks.add(id));
    ++_generation;
    try {
      if (bookmarkIds.contains(id)) {
        await bookmarks.remove(id);
      } else {
        await bookmarks.save(item);
      }
      await reload();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(context.translate(bookmarkSaveFailedKey))),
        );
      }
    } finally {
      if (mounted) setState(() => savingBookmarks.remove(id));
    }
  }

  Widget bookmarkLoadError(VoidCallback retry) => Column(children: [
        Text(context.translate(bookmarkSaveFailedKey)),
        TextButton(
            onPressed: retry, child: Text(context.translate(i18.common.retry))),
      ]);
}

class ReportBookmarkButton extends StatelessWidget {
  final bool selected;
  final bool saving;
  final VoidCallback? onPressed;
  final String addLabelKey;
  final String removeLabelKey;
  const ReportBookmarkButton(
      {super.key,
      required this.selected,
      required this.saving,
      required this.onPressed,
      required this.addLabelKey,
      required this.removeLabelKey});
  @override
  Widget build(BuildContext context) => Semantics(
        toggled: selected,
        child: IconButton(
          tooltip: context.translate(selected ? removeLabelKey : addLabelKey),
          onPressed: saving ? null : onPressed,
          color: Theme.of(context).colorTheme.primary.primary1,
          icon: saving
              ? const SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2))
              : Icon(selected ? Icons.bookmark : Icons.bookmark_border),
        ),
      );
}

class ReportBookmarksHomeCard<T> extends StatefulWidget {
  final bool amc;
  final int refreshToken;
  final Future<void> Function() onOpen;
  const ReportBookmarksHomeCard(
      {super.key,
      required this.amc,
      required this.onOpen,
      this.refreshToken = 0});
  @override
  State<ReportBookmarksHomeCard<T>> createState() =>
      _ReportBookmarksHomeCardState<T>();
}

class _ReportBookmarksHomeCardState<T>
    extends State<ReportBookmarksHomeCard<T>> {
  int _count = 0;
  bool _failed = false;
  Future<void> _refresh() async {
    try {
      final count =
          await reportBookmarksFor<T>(context, amc: widget.amc).count();
      if (mounted) {
        setState(() {
          _count = count;
          _failed = false;
        });
      }
    } catch (_) {
      if (mounted) setState(() => _failed = true);
    }
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) _refresh();
    });
  }

  @override
  void didUpdateWidget(covariant ReportBookmarksHomeCard<T> oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.refreshToken != widget.refreshToken) {
      _refresh();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(children: [
      ReportCard(
          badgeCount: _count,
          icon: Icons.bookmark,
          heading: context.translate(widget.amc
              ? i18.amcBookmarks.title
              : i18.installationBookmarks.title),
          description: context.translate(widget.amc
              ? i18.amcBookmarks.description
              : i18.installationBookmarks.description),
          onPress: () async {
            await widget.onOpen();
            if (mounted) await _refresh();
          }),
      if (_failed)
        TextButton(
            onPressed: _refresh,
            child: Text(context.translate(i18.common.retry))),
    ]);
  }
}
